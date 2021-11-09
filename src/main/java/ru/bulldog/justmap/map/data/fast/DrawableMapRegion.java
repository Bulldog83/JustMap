package ru.bulldog.justmap.map.data.fast;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.util.math.MatrixStack;
import org.lwjgl.opengl.GL11;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.RegionPos;
import ru.bulldog.justmap.util.render.GLC;
import ru.bulldog.justmap.util.render.MapTexture;
import ru.bulldog.justmap.util.render.RenderUtil;

import java.nio.ByteBuffer;

public class DrawableMapRegion implements MapRegion {
    private MapTexture image;
    private MapTexture texture;
    private ByteBuffer buffer;
    private byte[] bytes;
    private int glId = -1;

    @Override
    public void drawLayer(MatrixStack matrices, Layer layer, int level, double x, double y, double width, double height, int imgX, int imgY, int imgW, int imgH) {
        if (width <= 0 || height <= 0) return;

        float u1 = imgX / 512F;
        float v1 = imgY / 512F;
        float u2 = (imgX + imgW) / 512F;
        float v2 = (imgY + imgH) / 512F;

        this.drawTexture(matrices, x, y, width, height, u1, v1, u2, v2);
    }

    private void drawTexture(MatrixStack matrices, double x, double y, double w, double h, float u1, float v1, float u2, float v2) {
        if (texture != null && texture.changed) {
            this.texture.upload();
        } else if (texture == null && image.changed) {
            this.image.upload();
        }
        int id = texture != null ? texture.getId() : image.getId();
        RenderUtil.bindTexture(id);
        RenderUtil.applyFilter(false);

        RenderUtil.startDraw();
        RenderUtil.addQuad(matrices, x, y, w, h, u1, v1, u2, v2);
        RenderUtil.endDraw();
    }

    private void refillBuffer() {
        this.buffer.clear();
        this.buffer.put(this.bytes);
        this.buffer.position(0).limit(bytes.length);
    }

    public void upload() {
        if (bytes == null) return;

        if (this.glId == -1) {
            this.glId = TextureUtil.generateTextureId();
        }

        this.refillBuffer();

        RenderSystem.bindTexture(this.glId);
        RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_NEAREST);
        RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_NEAREST);
        RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP_TO_EDGE);
        RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_GENERATE_MIPMAP, GLC.GL_TRUE);
        RenderSystem.pixelStore(GLC.GL_UNPACK_ROW_LENGTH, 0);
        RenderSystem.pixelStore(GLC.GL_UNPACK_SKIP_PIXELS, 0);
        RenderSystem.pixelStore(GLC.GL_UNPACK_SKIP_ROWS, 0);

        GL11.glTexImage2D(GLC.GL_TEXTURE_2D, 0, GLC.GL_RGBA, 512, 512, 0, GLC.GL_RGBA, GLC.GL_UNSIGNED_INT_8_8_8_8, this.buffer);
    }

    @Override
    public RegionPos getPos() {
        return null;
    }

}
