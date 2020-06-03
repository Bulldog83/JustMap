package ru.bulldog.justmap.map.data;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.MapTexture;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.TaskManager;

public class MapRegion {
	
	private static Tessellator tessellator = Tessellator.getInstance();
	private static BufferBuilder builder = tessellator.getBuffer();
	private static TaskManager worker = TaskManager.getManager("region-data");
	private static MapCache mapData;
	
	private final RegionPos pos;
	private final MapTexture image;

	private boolean hideWater = false;
	private boolean waterTint = true;
	private boolean alternateRender = true;
	private boolean needUpdate = false;
	private boolean changed = false;
	public boolean surfaceOnly = false;
	
	public long updated = 0;
	
	public MapRegion(BlockPos blockPos) {
		this.pos = new RegionPos(blockPos);
		this.image = new MapTexture(512, 512);
		this.image.fill(Colors.BLACK);
		this.updateMapData();
		this.updateImage();
	}
	
	public int getX() {
		return this.pos.x;
	}
	
	public int getZ() {
		return this.pos.z;
	}
	
	public void updateTexture() {
		this.updateMapData();
		worker.execute(this::updateImage);
	}
	
	private void updateMapData() {
		mapData = MapCache.get();
		
		if (ClientParams.hideWater != hideWater) {
			this.hideWater = ClientParams.hideWater;
			this.needUpdate = true;
		}
		boolean waterTint = ClientParams.alternateColorRender && ClientParams.waterTint;
		if (this.waterTint != waterTint) {
			this.waterTint = waterTint;
			this.needUpdate = true;
		}
		if (ClientParams.alternateColorRender != alternateRender) {
			this.alternateRender = ClientParams.alternateColorRender;
			this.needUpdate = true;
		}
	}
	
	private void updateImage() {
		int regX = this.pos.x << 9;
		int regZ = this.pos.z << 9;		
		for (int x = 0; x < 512; x += 16) {
			int chunkX = (regX + x) >> 4;
			for (int y = 0; y < 512; y += 16) {
				int chunkZ = (regZ + y) >> 4;
				
				MapChunk mapChunk;
				if (surfaceOnly) {
					mapChunk = mapData.getChunk(Layer.Type.SURFACE, 0, chunkX, chunkZ);
					if (MapCache.currentLayer() == Layer.Type.SURFACE) {
						mapChunk.update(needUpdate);
					}
				} else {
					mapChunk = mapData.getCurrentChunk(chunkX, chunkZ).update(needUpdate);
				}				
				this.image.writeChunkData(x, y, mapChunk.getColorData());
			}
		}		
		this.updated = System.currentTimeMillis();
		this.needUpdate = false;
		this.changed = true;
	}
	
	public void draw(double x, double y, int imgX, int imgY, int width, int height, float scale) {
		if (width <= 0 || height <= 0) return;
		
		if (changed) {
			this.image.upload();
			this.changed = false;
		}
		RenderSystem.bindTexture(image.getId());
		if (ClientParams.textureFilter) {
			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
			RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		}
		
		float u1 = imgX / 512F;
		float v1 = imgY / 512F;
		float u2 = (imgX + width) / 512F;
		float v2 = (imgY + height) / 512F;
		
		double scW = (double) width / scale;
		double scH = (double) height / scale;
		
		builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		builder.vertex(x, y, 0.0).texture(u1, v1).next();
		builder.vertex(x, y + scH, 0.0).texture(u1, v2).next();
		builder.vertex(x + scW, y + scH, 0.0).texture(u2, v2).next();
		builder.vertex(x + scW, y, 0.0).texture(u2, v1).next();
		
		tessellator.draw();
	}
	
	public void close() {
		this.image.close();
	}
}
