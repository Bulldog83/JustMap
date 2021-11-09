package ru.bulldog.justmap.map.data.fast;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.lwjgl.opengl.GL11;
import ru.bulldog.justmap.util.render.GLC;
import ru.bulldog.justmap.util.render.RenderUtil;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

public class MapRegionLayer {
    public final static int WIDTH = 512;
    public final static int HEIGHT = 512;
    public final static int SIZE = 4 * WIDTH * HEIGHT;

    private final byte[] bytes = new byte[SIZE];
    private final ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());

    private int glId = -1;
    private volatile boolean isModified;

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

            GL11.glTexImage2D(GLC.GL_TEXTURE_2D, 0, GL11.GL_RGB, 512, 512, 0, GL11.GL_RGBA, GLC.GL_UNSIGNED_INT_8_8_8_8, this.buffer);

            isModified = false;
        }

        // Draw texture
        RenderUtil.bindTexture(this.glId);
        RenderUtil.startDraw();
        RenderUtil.addQuad(matrices, x, y, w, h, u1, v1, u2, v2);
        RenderUtil.endDraw();
    }

    private void refillBuffer() {
        this.buffer.clear();
        this.buffer.put(this.bytes);
        this.buffer.position(0).limit(bytes.length);
    }

    public void updateChunk(WorldChunk worldChunk) {
        // FIXME: This is where the magic happens...
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) (i % 256);
        }
        this.refillBuffer();

        isModified = true;
    }

    public void updateBlock(BlockPos pos, BlockState state) {
        // FIXME: This is where the magic happens...
        this.refillBuffer();

        isModified = true;
    }
}
