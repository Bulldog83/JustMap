package ru.bulldog.justmap.util;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.BufferRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.Sprite;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import ru.bulldog.justmap.map.minimap.MapSkin;
import ru.bulldog.justmap.map.minimap.MapSkin.RenderData;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;

import org.lwjgl.opengl.GL11;

public class DrawHelper extends DrawableHelper {	
	public static enum TextAlignment {
		LEFT,
		CENTER,
		RIGHT
	}
	
	private DrawHelper() {}
	
	public final static DrawHelper DRAWER = new DrawHelper();
	
	private final static VertexFormat VF_POS_TEX_NORMAL = new VertexFormat(ImmutableList.of(VertexFormats.POSITION_ELEMENT, VertexFormats.TEXTURE_ELEMENT, VertexFormats.NORMAL_ELEMENT, VertexFormats.PADDING_ELEMENT));
	private final static Tessellator tessellator = Tessellator.getInstance();
	private final static BufferBuilder builder = tessellator.getBuffer();
	private final static MinecraftClient client = MinecraftClient.getInstance();
	private final static TextRenderer textRenderer = client.textRenderer;

	public static int getWidth(Text text) {
		return textRenderer.getWidth(text);
	}

	public static int getWidth(String string) {
		return textRenderer.getWidth(string);
	}
	
	public void fillNoDepth(int x, int y, int right, int left, int color) {
		RenderSystem.disableDepthTest();
		fill(x, y, right, left, color);
		RenderSystem.enableDepthTest();
	}
	
	public static void drawCenteredString(String string, double x, double y, int color) {
		MatrixStack matrix = new MatrixStack();
		drawCenteredString(matrix, string, x, y, color);
	}
	
	public static void drawCenteredString(MatrixStack matrix, String string, double x, double y, int color) {
		textRenderer.drawWithShadow(matrix, string, (float) (x - textRenderer.getWidth(string) / 2), (float) y, color);
	}
	
	public static void drawCenteredText(MatrixStack matrix, Text text, double x, double y, int color) {
		textRenderer.drawWithShadow(matrix, text, (float) (x - textRenderer.getWidth(text) / 2), (float) y, color);
	}
	
	public static void drawBoundedString(String string, int x, int y, int leftBound, int rightBound, int color) {
		MatrixStack matrix = new MatrixStack();
		drawBoundedString(matrix, string, x, y, leftBound, rightBound, color);
	}
	
	public static void drawBoundedString(MatrixStack matrix, String string, int x, int y, int leftBound, int rightBound, int color) {
		if (string == null) return;
		
		int stringWidth = textRenderer.getWidth(string);
		int drawX = x - stringWidth / 2;
		if (drawX < leftBound) {
			drawX = leftBound;
		} else if (drawX + stringWidth > rightBound) {
			drawX = rightBound - stringWidth;
		}

		DRAWER.drawStringWithShadow(matrix, textRenderer, string, drawX, y, color);
	}

	public static void drawRightAlignedString(String string, int x, int y, int color) {
		MatrixStack matrix = new MatrixStack();
		textRenderer.drawWithShadow(matrix, string, x - textRenderer.getWidth(string), y, color);
	}
	
	public static void drawDiamond(double x, double y, int width, int height, int color) {
		drawTriangle(x, y + height / 2,
				 x + width, y + height / 2,
				 x + width / 2, y,
				 color);		
		drawTriangle(x, y + height / 2,
				 x + width / 2, y + height,
				 x + width, y + height / 2,
				 color);
	}
	
	public static void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		builder.begin(GL11.GL_TRIANGLES, VertexFormats.POSITION);
		builder.vertex(x1, y1, 0).next();
		builder.vertex(x2, y2, 0).next();
		builder.vertex(x3, y3, 0).next();
		tessellator.draw();
		RenderSystem.enableTexture();
	}
	
	public static void drawLine(double x1, double y1, double x2, double y2, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		builder.begin(GL11.GL_LINES, VertexFormats.POSITION);
		builder.vertex(x1, y1, 0).next();
		builder.vertex(x2, y2, 0).next();
		tessellator.draw();
		RenderSystem.enableTexture();
	}	
	
	public static void drawOutlineCircle(double x, double y, double radius, double outline, int color) {
		int darken = ColorUtil.colorBrigtness(color, -3);
		DrawHelper.drawCircle(x, y, radius + outline, darken);
		DrawHelper.drawCircle(x, y, radius, color);
	}
	
	public static void drawCircle(double x, double y, double radius, int color) {
		double pi2 = Math.PI * 2;
		
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		
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

	public static void fill(double x, double y, double w, double h, int color) {
		fill(AffineTransformation.identity().getMatrix(), x, y, w, h, color);
	}
	
	public static void fill(MatrixStack matrix, double x, double y, double w, double h, int color) {
		fill(matrix.peek().getModel(), x, y, w, h, color);
	}

	public static void fill(Matrix4f matrix4f, double x, double y, double w, double h, int color) {
		double o;
		if (x < w) {
			o = x;
			x = w;
			w = o;
		}
		if (y < h) {
			o = y;
			y = h;
			h = o;
		}

		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
      
		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
		builder.vertex(matrix4f, (float) x, (float) h, 0.0F).color(r, g, b, a).next();
		builder.vertex(matrix4f, (float) w, (float) h, 0.0F).color(r, g, b, a).next();
		builder.vertex(matrix4f, (float) w, (float) y, 0.0F).color(r, g, b, a).next();
		builder.vertex(matrix4f, (float) x, (float) y, 0.0F).color(r, g, b, a).next();
		builder.end();
		BufferRenderer.draw(builder);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
	
	public static void draw(double x, double y, float w, float h) {
		MatrixStack matrix = new MatrixStack();		
		builder.begin(GL11.GL_QUADS, VF_POS_TEX_NORMAL);		
		draw(matrix, builder, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
		tessellator.draw();		
	}
	
	public static void drawPlayerHead(MatrixStack matrix, double x, double y, int w, int h) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		builder.begin(GL11.GL_QUADS, VF_POS_TEX_NORMAL);
		draw(matrix, builder, x, y, w, h, 0.125F, 0.125F, 0.25F, 0.25F);
		draw(matrix, builder, x, y, w, h, 0.625F, 0.125F, 0.75F, 0.25F);
		tessellator.draw();
	}
	
	public static void draw(MatrixStack matrix, double x, double y, int size, int isize, int ix, int iy, int tw, int th) {
		draw(matrix, x, y, size, size, ix, iy, isize, isize, tw, th);
	}
	
	public static void draw(MatrixStack matrix, double x, double y, int w, int h, int ix, int iy, int iw, int ih, int tw, int th) {
		float minU = (float) ix / tw;
		float minV = (float) iy / th;
		float maxU = (float) (ix + iw) / tw;
		float maxV = (float) (iy + ih) / th;
		
		builder.begin(GL11.GL_QUADS, VF_POS_TEX_NORMAL);
		draw(matrix, builder, x, y, w, h, minU, minV, maxU, maxV);
		tessellator.draw();
	}
	
	public static void drawSkin(MatrixStack matrix, MapSkin skin, double x, double y, float w, float h) {
		RenderData renderData = skin.getRenderData();
		
		double scale = client.getWindow().getScaleFactor();
		if (renderData.x != x || renderData.y != y || renderData.scaleFactor != scale ||
			renderData.width != w || renderData.height != h) {
			
			renderData.calculate(x, y, w, h, scale);
		}

		float sMinU = skin.getMinU();
		float sMaxU = skin.getMaxU();
		float sMinV = skin.getMinV();
		float sMaxV = skin.getMaxV();
		float scaledBrd = renderData.scaledBorder;
		float hSide = renderData.hSide;
		float vSide = renderData.vSide;		
		double leftC = renderData.leftC;
		double rightC = renderData.rightC;
		double topC = renderData.topC;
		double bottomC = renderData.bottomC;		
		float leftU = renderData.leftU;
		float rightU = renderData.rightU;
		float topV = renderData.topV;
		float bottomV = renderData.bottomV;
		
		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		builder.begin(GL11.GL_QUADS, VF_POS_TEX_NORMAL);
		
		VertexConsumer vertexConsumer = skin.getTextureSpecificVertexConsumer(builder);
		
		draw(matrix, vertexConsumer, x, y, scaledBrd, scaledBrd, sMinU, sMinV, leftU, topV);
		draw(matrix, vertexConsumer, rightC, y, scaledBrd, scaledBrd, rightU, sMinV, sMaxU, topV);
		draw(matrix, vertexConsumer, x, topC, scaledBrd, vSide, sMinU, topV, leftU, bottomV);
		draw(matrix, vertexConsumer, rightC, topC, scaledBrd, vSide, rightU, topV, sMaxU, bottomV);
		draw(matrix, vertexConsumer, x, bottomC, scaledBrd, scaledBrd, sMinU, bottomV, leftU, sMaxV);
		draw(matrix, vertexConsumer, rightC, bottomC, scaledBrd, scaledBrd, rightU, bottomV, sMaxU, sMaxV);
		draw(matrix, vertexConsumer, leftC, topC, hSide, vSide, leftU, topV, rightU, bottomV);
		
		if (skin.repeating) {
			float tail = renderData.tail;
			float tailU = renderData.tailU;
			hSide = vSide;
			
			draw(matrix, vertexConsumer, leftC + hSide, y, tail, scaledBrd, leftU, sMinV, tailU, topV);
			draw(matrix, vertexConsumer, leftC + hSide, bottomC, tail, scaledBrd, leftU, bottomV, tailU, sMaxV);
		}
		
		draw(matrix, vertexConsumer, leftC, y, hSide, scaledBrd, leftU, sMinV, rightU, topV);
		draw(matrix, vertexConsumer, leftC, bottomC, hSide, scaledBrd, leftU, bottomV, rightU, sMaxV);
		
		tessellator.draw();
	}
	
	public static void drawSprite(MatrixStack matrix, Sprite sprite, double x, double y, float w, float h) {
		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		builder.begin(GL11.GL_QUADS, VF_POS_TEX_NORMAL);
		
		VertexConsumer vertexConsumer = sprite.getTextureSpecificVertexConsumer(builder);
		
		draw(matrix, vertexConsumer, x, y, w, h, sprite.getMinU(), sprite.getMinV(), sprite.getMaxU(), sprite.getMaxV());
		tessellator.draw();
	}
	
	private static void draw(MatrixStack matrixStack, VertexConsumer vertexConsumer, double x, double y, float w, float h, float minU, float minV, float maxU, float maxV) {
		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		matrixStack.push();
		matrixStack.translate(x, y, 0);
		
		Matrix4f m4f = matrixStack.peek().getModel();
		Matrix3f m3f = matrixStack.peek().getNormal();
		
		addVertices(m4f, m3f, vertexConsumer, w, h, minU, minV, maxU, maxV);
		
		matrixStack.pop();
	}
	
	private static void addVertices(Matrix4f m4f, Matrix3f m3f, VertexConsumer vertexConsumer, float w, float h, float minU, float minV, float maxU, float maxV) {
		addVertices(m4f, m3f, vertexConsumer, 0, w, 0, h, minU, minV, maxU, maxV);
	}
	
	private static void addVertices(Matrix4f m4f, Matrix3f m3f, VertexConsumer vertexConsumer, float minX, float maxX, float minY, float maxY, float minU, float minV, float maxU, float maxV) {
		addVertex(m4f, m3f, vertexConsumer, minX, minY, 1.0F, minU, minV);
		addVertex(m4f, m3f, vertexConsumer, minX, maxY, 1.0F, minU, maxV);
		addVertex(m4f, m3f, vertexConsumer, maxX, maxY, 1.0F, maxU, maxV);
		addVertex(m4f, m3f, vertexConsumer, maxX, minY, 1.0F, maxU, minV);
	}
	
	private static void addVertex(Matrix4f m4f, Matrix3f m3f, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v) {
		vertexConsumer.vertex(m4f, x, y, z).texture(u, v).normal(m3f, 0.0F, 1.0F, 0.0F).next();
	}
}
