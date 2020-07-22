package ru.bulldog.justmap.util;

import com.google.common.collect.ImmutableList;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.AffineTransformation;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.text.Text;

import ru.bulldog.justmap.client.render.Image;
import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.map.minimap.skin.MapSkin.RenderData;

import org.lwjgl.opengl.GL11;

public class RenderUtil extends DrawableHelper {	
	public static enum TextAlignment {
		LEFT,
		CENTER,
		RIGHT
	}
	
	private RenderUtil() {}
	
	public final static RenderUtil DRAWER = new RenderUtil();
	
	private final static VertexFormat VF_POS_TEX_NORMAL = new VertexFormat(ImmutableList.of(VertexFormats.POSITION_ELEMENT, VertexFormats.TEXTURE_ELEMENT, VertexFormats.NORMAL_ELEMENT, VertexFormats.PADDING_ELEMENT));
	private final static Tessellator tessellator = Tessellator.getInstance();
	private final static BufferBuilder vertexBuffer = tessellator.getBuffer();
	private final static TextRenderer textRenderer = DataUtil.getMinecraft().textRenderer;
	private final static TextureManager textureManager = DataUtil.getMinecraft().getTextureManager();

	public static int getWidth(Text text) {
		return textRenderer.getWidth(text);
	}

	public static int getWidth(String string) {
		return textRenderer.getWidth(string);
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

	public static void drawRightAlignedString(MatrixStack matrix, String string, int x, int y, int color) {
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
	
	public static void bindTexture(Identifier id) {
    	textureManager.bindTexture(id);
    }
    
	public static void bindTexture(int id) {
		RenderSystem.bindTexture(id);
	}
	
	public static void applyFilter() {
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR_MIPMAP_LINEAR);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
	}
	
    public static void startDraw() {
    	startDraw(VertexFormats.POSITION_TEXTURE);
    }
    
    public static void startDrawNormal() {
    	startDraw(VF_POS_TEX_NORMAL);
    }
    
    public static void startDraw(VertexFormat vertexFormat) {
    	startDraw(GL11.GL_QUADS, vertexFormat);
    }
    
    public static void startDraw(int mode, VertexFormat vertexFormat) {
    	vertexBuffer.begin(mode, vertexFormat);
    }
    
    public static void endDraw() {
    	tessellator.draw();
    }
	
	public static void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		startDraw(GL11.GL_TRIANGLES, VertexFormats.POSITION);
		vertexBuffer.vertex(x1, y1, 0).next();
		vertexBuffer.vertex(x2, y2, 0).next();
		vertexBuffer.vertex(x3, y3, 0).next();
		endDraw();
		RenderSystem.enableTexture();
	}
	
	public static void drawLine(double x1, double y1, double x2, double y2, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		startDraw(GL11.GL_LINES, VertexFormats.POSITION);
		vertexBuffer.vertex(x1, y1, 0).next();
		vertexBuffer.vertex(x2, y2, 0).next();
		endDraw();
		RenderSystem.enableTexture();
	}	
	
	public static void drawOutlineCircle(double x, double y, double radius, double outline, int color) {
		int darken = ColorUtil.colorBrigtness(color, -3);
		RenderUtil.drawCircle(x, y, radius + outline, darken);
		RenderUtil.drawCircle(x, y, radius, color);
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
		
		startDraw(GL11.GL_TRIANGLE_FAN, VertexFormats.POSITION);		
		vertexBuffer.vertex(x, y, 0).next();		
		
		int sides = 50;
		for (int i = 0; i <= sides; i++) {
			double angle = (pi2 * i / sides) + Math.toRadians(180);
			double vx = x + Math.sin(angle) * radius;
			double vy = y + Math.cos(angle) * radius;
			vertexBuffer.vertex(vx, vy, 0).next();
		}
		endDraw();
		
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
		startDraw(GL11.GL_QUADS, VertexFormats.POSITION_COLOR);
		vertexBuffer.vertex(matrix4f, (float) x, (float) (y + h), 0.0F).color(r, g, b, a).next();
		vertexBuffer.vertex(matrix4f, (float) (x + w), (float) (y + h), 0.0F).color(r, g, b, a).next();
		vertexBuffer.vertex(matrix4f, (float) (x + w), (float) y, 0.0F).color(r, g, b, a).next();
		vertexBuffer.vertex(matrix4f, (float) x, (float) y, 0.0F).color(r, g, b, a).next();
		endDraw();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
	
	public static void draw(double x, double y, float w, float h) {
		MatrixStack matrix = new MatrixStack();		
		startDrawNormal();
		draw(matrix, vertexBuffer, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
		endDraw();
	}
	
	public static void drawPlayerHead(MatrixStack matrix, double x, double y, int w, int h) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		startDrawNormal();
		draw(matrix, vertexBuffer, x, y, w, h, 0.125F, 0.125F, 0.25F, 0.25F);
		draw(matrix, vertexBuffer, x, y, w, h, 0.625F, 0.125F, 0.75F, 0.25F);
		endDraw();
	}
	
	public static void draw(MatrixStack matrix, double x, double y, int size, int isize, int ix, int iy, int tw, int th) {
		draw(matrix, x, y, size, size, ix, iy, isize, isize, tw, th);
	}
	
	public static void draw(MatrixStack matrix, double x, double y, int w, int h, int ix, int iy, int iw, int ih, int tw, int th) {
		float minU = (float) ix / tw;
		float minV = (float) iy / th;
		float maxU = (float) (ix + iw) / tw;
		float maxV = (float) (iy + ih) / th;
		
		startDrawNormal();
		draw(matrix, vertexBuffer, x, y, w, h, minU, minV, maxU, maxV);
		endDraw();
	}
	
	public static void drawSkin(MatrixStack matrix, MapSkin skin, double x, double y, float w, float h) {
		RenderData renderData = skin.getRenderData();
		
		if (renderData.scaleChanged || renderData.x != x || renderData.y != y ||
			renderData.width != w || renderData.height != h) {
			
			renderData.calculate(x, y, w, h);
		}

		float sMinU = 0.0F;
		float sMaxU = 1.0F;
		float sMinV = 0.0F;
		float sMaxV = 1.0F;
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
		
		skin.bindTexture();		
		startDrawNormal();
		
		draw(matrix, vertexBuffer, x, y, scaledBrd, scaledBrd, sMinU, sMinV, leftU, topV);
		draw(matrix, vertexBuffer, rightC, y, scaledBrd, scaledBrd, rightU, sMinV, sMaxU, topV);
		draw(matrix, vertexBuffer, x, bottomC, scaledBrd, scaledBrd, sMinU, bottomV, leftU, sMaxV);
		draw(matrix, vertexBuffer, rightC, bottomC, scaledBrd, scaledBrd, rightU, bottomV, sMaxU, sMaxV);
		
		if (skin.resizable) {
			draw(matrix, vertexBuffer, rightC, topC, scaledBrd, vSide, rightU, topV, sMaxU, bottomV);
			draw(matrix, vertexBuffer, x, topC, scaledBrd, vSide, sMinU, topV, leftU, bottomV);
			draw(matrix, vertexBuffer, leftC, topC, hSide, vSide, leftU, topV, rightU, bottomV);
			if (skin.repeating) {
				float tail = renderData.tail;
				float tailU = renderData.tailU;
				hSide = vSide;
				
				draw(matrix, vertexBuffer, leftC + hSide, y, tail, scaledBrd, leftU, sMinV, tailU, topV);
				draw(matrix, vertexBuffer, leftC + hSide, bottomC, tail, scaledBrd, leftU, bottomV, tailU, sMaxV);
			}
		
			draw(matrix, vertexBuffer, leftC, y, hSide, scaledBrd, leftU, sMinV, rightU, topV);
			draw(matrix, vertexBuffer, leftC, bottomC, hSide, scaledBrd, leftU, bottomV, rightU, sMaxV);
		} else {
			double left = leftC;
			int segments = renderData.hSegments;
			for (int i = 0; i < segments; i++) {
				draw(matrix, vertexBuffer, left, y, hSide, scaledBrd, leftU, sMinV, rightU, topV);
				draw(matrix, vertexBuffer, left, bottomC, hSide, scaledBrd, leftU, bottomV, rightU, sMaxV);
				left += hSide;
			}
			double top = topC;
			segments = renderData.vSegments;
			for (int i = 0; i < segments; i++) {
				draw(matrix, vertexBuffer, x, top, scaledBrd, vSide, sMinU, topV, leftU, bottomV);
				draw(matrix, vertexBuffer, rightC, top, scaledBrd, vSide, rightU, topV, sMaxU, bottomV);
				top += vSide;
			}
			
			float hTail = renderData.hTail;
			float vTail = renderData.vTail;
			float hTailU = renderData.hTailU;
			float vTailV = renderData.vTailV;
			
			draw(matrix, vertexBuffer, left, y, hTail, scaledBrd, leftU, sMinV, hTailU, topV);
			draw(matrix, vertexBuffer, left, bottomC, hTail, scaledBrd, leftU, bottomV, hTailU, sMaxV);
			draw(matrix, vertexBuffer, x, top, scaledBrd, vTail, sMinU, topV, leftU, vTailV);
			draw(matrix, vertexBuffer, rightC, top, scaledBrd, vTail, rightU, topV, sMaxU, vTailV);
		}
		
		endDraw();
	}
	
	public static void drawImage(MatrixStack matrix, Image image, double x, double y, float w, float h) {
		RenderSystem.enableBlend();
		RenderSystem.enableAlphaTest();		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		image.bindTexture();
		startDrawNormal();
		draw(matrix, vertexBuffer, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
		endDraw();
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
		vertex(m4f, m3f, vertexConsumer, minX, minY, 1.0F, minU, minV);
		vertex(m4f, m3f, vertexConsumer, minX, maxY, 1.0F, minU, maxV);
		vertex(m4f, m3f, vertexConsumer, maxX, maxY, 1.0F, maxU, maxV);
		vertex(m4f, m3f, vertexConsumer, maxX, minY, 1.0F, maxU, minV);
	}
	
	public static void addQuad(double x, double y, double w, double h) {
		addQuad(x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
	}
	
	public static void addQuad(double x, double y, double w, double h, float minU, float minV, float maxU, float maxV) {
		vertex(x, y, 0.0, minU, minV);
		vertex(x, y + h, 0.0, minU, maxV);
		vertex(x + w, y + h, 0.0, maxU, maxV);
		vertex(x + w, y, 0.0, maxU, minV);
	}
	
	private static void vertex(Matrix4f m4f, Matrix3f m3f, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v) {
		vertexConsumer.vertex(m4f, x, y, z).texture(u, v).normal(m3f, 0.0F, 1.0F, 0.0F).next();
	}
	
	private static void vertex(double x, double y, double z, float u, float v) {
    	vertexBuffer.vertex(x, y, z).texture(u, v).next();
    }
}
