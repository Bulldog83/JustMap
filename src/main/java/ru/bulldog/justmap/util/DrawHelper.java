package ru.bulldog.justmap.util;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;

import org.lwjgl.opengl.GL11;

public class DrawHelper extends DrawableHelper {	
	public static enum TextAlignment {
		LEFT,
		CENTER,
		RIGHT
	}
	
	private DrawHelper() {}
	
	public final static DrawHelper DRAWER = new DrawHelper();
	
	public void fillNoDepth(int x, int y, int right, int left, int color) {
		RenderSystem.disableDepthTest();
		fill(x, y, right, left, color);
		RenderSystem.enableDepthTest();
	}
	
	public static void drawRightAlignedString(TextRenderer textRenderer, String string, int i, int j, int k) {
		textRenderer.drawWithShadow(string, (float)(i - textRenderer.getStringWidth(string)), (float)j, k);
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
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		builder.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION);
		builder.vertex(x1, y1, 0).next();
		builder.vertex(x2, y2, 0).next();
		builder.vertex(x3, y3, 0).next();
		tessellator.draw();
		RenderSystem.enableTexture();
	}
	
	public static void drawLine(int x1, int y1, int x2, int y2, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		builder.begin(GL11.GL_LINES, VertexFormats.POSITION);
		builder.vertex(x1, y1, 0).next();
		builder.vertex(x2, y2, 0).next();
		tessellator.draw();
		RenderSystem.enableTexture();
	}	
	
	public static void drawOutlineCircle(int x, int y, double radius, double outline, int color) {
		int darken = ColorUtil.colorBrigtness(color, -3);
		DrawHelper.drawCircle(x, y, radius + outline, darken);
		DrawHelper.drawCircle(x, y, radius, color);
	}
	
	public static void drawCircle(int x, int y, double radius, int color) {
		double pi2 = Math.PI * 2;
		
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		
	    RenderSystem.enableBlend();
	    RenderSystem.disableTexture();
	    RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(r, g, b, a);
		
		builder.begin(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION);		
		builder.vertex(x, y, 0).next();		
		
		int sides = 50;
		for (int i = 0; i <= sides; i++) {
			double angle = (pi2 * i / sides) + Math.toRadians(180);
			double vx = x + Math.sin(angle) * radius;
			double vy = y + Math.cos(angle) * radius;
			builder.vertex(vx, vy, 0).next();
		}
		tessellator.draw();
		
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
}
