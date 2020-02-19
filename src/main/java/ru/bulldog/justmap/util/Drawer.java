package ru.bulldog.justmap.util;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import org.lwjgl.opengl.GL11;

public class Drawer extends DrawableHelper {	
	public static enum TextAlignment {
		LEFT,
		CENTER,
		RIGHT
	}
	
	private Drawer() {}
	
	public final static Drawer DRAWER = new Drawer();
	
	public void fillNoDepth(int x, int y, int right, int left, int color) {
		RenderSystem.disableDepthTest();
		fill(x, y, right, left, color);
		RenderSystem.enableDepthTest();
	}
	
	public static void drawBoundedString(TextRenderer textRenderer, String string, int x, int y, int leftBound, int rightBound, int color) {
		if (string == null) {
			return;
		}
		
		int stringWidth = textRenderer.getStringWidth(string);
		int drawX = x - stringWidth / 2;
		if (drawX < leftBound) {
			drawX = leftBound;
		} else if (drawX + stringWidth > rightBound) {
			drawX = rightBound - stringWidth;
		}

		DRAWER.drawString(textRenderer, string, drawX, y, color);
	}
	
	public static void drawDiamond(int x, int y, int width, int height, int color) {
		drawTriangle(x, y + height / 2,
				 x + width, y + height / 2,
				 x + width / 2, y,
				 color);		
		drawTriangle(x, y + height / 2,
				 x + width / 2, y + height,
				 x + width, y + height / 2,
				 color);
	}
	
	public static void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA.value, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value, GlStateManager.SrcFactor.ONE.value, GlStateManager.DstFactor.ZERO.value);
		RenderSystem.color4f(r, g, b, a);
		builder.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION);
		builder.vertex(x1, y1, 0).next();
		builder.vertex(x2, y2, 0).next();
		builder.vertex(x3, y3, 0).next();
		tessellator.draw();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
	
	public static void drawLine(int x1, int y1, int x2, int y2, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA.value, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA.value, GlStateManager.SrcFactor.ONE.value, GlStateManager.DstFactor.ZERO.value);
		RenderSystem.color4f(r, g, b, a);
		builder.begin(GL11.GL_LINES, VertexFormats.POSITION);
		builder.vertex(x1, y1, 0).next();
		builder.vertex(x2, y2, 0).next();
		tessellator.draw();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}	
}
