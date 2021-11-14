package ru.bulldog.justmap.map.data.fast;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;
import org.lwjgl.opengl.GL11;

import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.util.render.GLC;
import ru.bulldog.justmap.util.render.RenderUtil;

public class MapRegionLayer {
	public final static int REGION_SIZE = 512;
	public final static int CHUNK_SIZE = 16;
	public final static int BYTES_PER_PIXEL = 4;

	private final static int BUFFER_SIZE = REGION_SIZE * REGION_SIZE * BYTES_PER_PIXEL;

	private final MapChunk[][] chunks = new MapChunk[32][32];

	private final ByteBuffer buffer = ByteBuffer.allocateDirect(BUFFER_SIZE).order(ByteOrder.nativeOrder());

	private int glId = -1;
	private volatile boolean isModified;
	private final Layer layer;
	private final int level;

	public MapRegionLayer(Layer layer, int level) {
		this.layer = layer;
		this.level = level;
	}

	public void updateChunk(WorldChunk worldChunk) {
		// FIXME: verify/assert that chunkpos is inside region?
		MapChunk mapChunk = getMapChunk(worldChunk.getPos());

		mapChunk.onChunkUpdate(worldChunk);
		mapChunk.writeToTextureBuffer(buffer);
		isModified = true;
	}

	public void updateBlock(BlockPos pos) {
		ChunkPos chunkPos = new ChunkPos(pos);
		MapChunk mapChunk = getMapChunk(chunkPos);

		mapChunk.onBlockUpdate(pos);
		mapChunk.writeToTextureBuffer(buffer);
		isModified = true;
	}

	@NotNull
	private MapChunk getMapChunk(ChunkPos chunkPos) {
		int relRegX = chunkPos.getRegionRelativeX();
		int relRegZ = chunkPos.getRegionRelativeZ();

		MapChunk mapChunk = chunks[relRegX][relRegZ];
		if (mapChunk == null) {
			mapChunk = new MapChunk(relRegX, relRegZ, layer, level, chunkPos);
			chunks[relRegX][relRegZ] = mapChunk;
		}
		return mapChunk;
	}

	public void draw(MatrixStack matrices, double x, double y, double width, double height, int imgX, int imgY, int imgW, int imgH) {
		if (width <= 0 || height <= 0) return;

		float u1 = imgX / 512F;
		float v1 = imgY / 512F;
		float u2 = (imgX + imgW) / 512F;
		float v2 = (imgY + imgH) / 512F;

		this.drawTexture(matrices, x, y, width, height, u1, v1, u2, v2);
	}

	private void drawTexture(MatrixStack matrices, double x, double y, double w, double h, float u1, float v1, float u2, float v2) {
		// FIXME: Should be done in an init function..?
		if (this.glId == -1) {
			this.glId = TextureUtil.generateTextureId();
		}

		if (isModified) {
			// FIXME: might need cooldown..?
			// Need to update the texture with GL
			RenderSystem.bindTexture(this.glId);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_NEAREST);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_NEAREST);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP_TO_EDGE);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP_TO_EDGE);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_GENERATE_MIPMAP, GLC.GL_TRUE);
			RenderSystem.pixelStore(GLC.GL_UNPACK_ROW_LENGTH, 0);
			RenderSystem.pixelStore(GLC.GL_UNPACK_SKIP_PIXELS, 0);
			RenderSystem.pixelStore(GLC.GL_UNPACK_SKIP_ROWS, 0);

			GL11.glTexImage2D(GLC.GL_TEXTURE_2D, 0, GL11.GL_RGB, REGION_SIZE, REGION_SIZE, 0, GL11.GL_RGBA, GLC.GL_UNSIGNED_INT_8_8_8_8, this.buffer);

			isModified = false;
		}

		// Draw texture
		RenderUtil.bindTexture(this.glId);
		RenderUtil.startDraw();
		RenderUtil.addQuad(matrices, x, y, w, h, u1, v1, u2, v2);
		RenderUtil.endDraw();
	}
}
