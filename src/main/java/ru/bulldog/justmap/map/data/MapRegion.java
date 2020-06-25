package ru.bulldog.justmap.map.data;

import java.io.File;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.MapTexture;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.StorageUtil;
import ru.bulldog.justmap.util.TaskManager;

public class MapRegion {
	
	private static Tessellator tessellator = Tessellator.getInstance();
	private static BufferBuilder builder = tessellator.getBuffer();
	private static TaskManager worker = TaskManager.getManager("region-data");
	
	private final RegionPos pos;
	private final MapTexture image;
	private final MapTexture overlay;
	
	private Layer.Type layer;
	private int level;

	private boolean hideWater = false;
	private boolean waterTint = true;
	private boolean alternateRender = true;
	private boolean needUpdate = false;
	private boolean changed = false;
	private boolean updating = false;
	public boolean surfaceOnly = false;
	
	public long updated = 0;
	
	public MapRegion(BlockPos blockPos, Layer.Type layer, int level) {
		this.pos = new RegionPos(blockPos);
		this.image = new MapTexture(512, 512);
		this.overlay = new MapTexture(512, 512);
		this.image.fill(Colors.BLACK);
		this.overlay.fill(Colors.TRANSPARENT);
		this.layer = layer;
		this.level = level;
		this.loadImage();
		this.updateTexture();
	}
	
	public int getX() {
		return this.pos.x;
	}
	
	public int getZ() {
		return this.pos.z;
	}
	
	public void updateTexture() {
		if (updating) return;
		this.updateMapParams();
		worker.execute(this::updateImage);
		this.updating = true;
	}
	
	private void updateMapParams() {
		this.needUpdate = ClientParams.forceUpdate;
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
		MapCache mapData = MapCache.get();
		
		int regX = this.pos.x << 9;
		int regZ = this.pos.z << 9;		
		for (int x = 0; x < 512; x += 16) {
			int chunkX = (regX + x) >> 4;
			for (int y = 0; y < 512; y += 16) {
				int chunkZ = (regZ + y) >> 4;
				
				MapChunk mapChunk;
				boolean updated = false;
				if (surfaceOnly) {
					mapChunk = mapData.getChunk(Layer.Type.SURFACE, 0, chunkX, chunkZ);
					if (MapCache.currentLayer() == Layer.Type.SURFACE) {
						updated = mapChunk.update(needUpdate);
					}
				} else {
					mapChunk = mapData.getCurrentChunk(chunkX, chunkZ);
					updated = mapChunk.update(needUpdate);
				}
				if (mapChunk.isChunkLoaded()) {
					this.overlay.fill(x, y, 16, 16, Colors.PURPLE);
				} else {
					this.overlay.fill(x, y, 16, 16, Colors.TRANSPARENT);
				}
				if (updated) {
					this.image.writeChunkData(x, y, mapChunk.getColorData());
					this.changed = updated;
				}
			}
		}
		if (changed) this.saveImage();
		this.updated = System.currentTimeMillis();
		this.needUpdate = false;
		this.updating = false;
	}
	
	public Layer.Type getLayer() {
		return this.layer != null ? this.layer : null;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public void swapLayer(Layer.Type layer, int level) {
		this.layer = layer;
		this.level = level;
		this.loadImage();
		this.updateTexture();
	}
	
	private void saveImage() {
		File imgFile = this.imageFile();
		JustMap.WORKER.execute(() -> this.image.saveImage(imgFile));
	}
	
	private void loadImage() {
		File imgFile = this.imageFile();
		this.image.loadImage(imgFile);
		this.changed = true;
	}
	
	private File imageFile() {
		File dir = StorageUtil.cacheDir();
		if (surfaceOnly || Layer.Type.SURFACE == layer) {
			dir = new File(dir, "surface/");
		} else {
			dir = new File(dir, String.format("%s/%d/", layer.value.name, level));
		}
		
		if (!dir.exists()) {
			dir.mkdirs();
		}
		
		return new File(dir, String.format("r%d.%d.png", pos.x, pos.z));
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
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		
		builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		builder.vertex(x, y, 0.0).texture(u1, v1).next();
		builder.vertex(x, y + scH, 0.0).texture(u1, v2).next();
		builder.vertex(x + scW, y + scH, 0.0).texture(u2, v2).next();
		builder.vertex(x + scW, y, 0.0).texture(u2, v1).next();
		
		tessellator.draw();
		
		this.overlay.upload();
		builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE);
		builder.vertex(x, y, 0.0).texture(u1, v1).next();
		builder.vertex(x, y + scH, 0.0).texture(u1, v2).next();
		builder.vertex(x + scW, y + scH, 0.0).texture(u2, v2).next();
		builder.vertex(x + scW, y, 0.0).texture(u2, v1).next();
		
		tessellator.draw();
	}
	
	public void close() {
		this.image.close();
		this.overlay.close();
	}
}
