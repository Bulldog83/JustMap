package ru.bulldog.justmap.util.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Transformation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.opengl.GL11;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.map.minimap.skin.MapSkin.RenderData;
import ru.bulldog.justmap.util.colors.ColorUtil;

public class RenderUtil extends GuiComponent {	
	
	private RenderUtil() {}
	
	public final static RenderUtil DRAWER = new RenderUtil();

	public final static VertexFormat VF_POS_TEX_NORMAL;
	private final static Tesselator tessellator = Tesselator.getInstance();
	private final static BufferBuilder vertexBuffer = tessellator.getBuilder();
	private final static Font textRenderer = Minecraft.getInstance().font;
	private final static TextureManager textureManager = Minecraft.getInstance().getTextureManager();

	public static int getWidth(Component text) {
		return textRenderer.width(text);
	}

	public static int getWidth(String string) {
		return textRenderer.width(string);
	}
	
	public static void drawCenteredString(String string, double x, double y, int color) {
		PoseStack matrices = new PoseStack();
		drawCenteredString(matrices, string, x, y, color);
	}
	
	public static void drawCenteredString(PoseStack matrices, String string, double x, double y, int color) {
		textRenderer.drawShadow(matrices, string, (float) (x - textRenderer.width(string) / 2), (float) y, color);
	}
	
	public static void drawCenteredText(PoseStack matrices, Component text, double x, double y, int color) {
		textRenderer.drawShadow(matrices, text, (float) (x - textRenderer.width(text) / 2), (float) y, color);
	}
	
	public static void drawBoundedString(String string, int x, int y, int leftBound, int rightBound, int color) {
		PoseStack matrices = new PoseStack();
		drawBoundedString(matrices, string, x, y, leftBound, rightBound, color);
	}
	
	public static void drawBoundedString(PoseStack matrices, String string, int x, int y, int leftBound, int rightBound, int color) {
		if (string == null) return;
		
		int stringWidth = textRenderer.width(string);
		int drawX = x - stringWidth / 2;
		if (drawX < leftBound) {
			drawX = leftBound;
		} else if (drawX + stringWidth > rightBound) {
			drawX = rightBound - stringWidth;
		}

		drawString(matrices, textRenderer, string, drawX, y, color);
	}

	public static void drawRightAlignedString(PoseStack matrices, String string, int x, int y, int color) {
		textRenderer.drawShadow(matrices, string, x - textRenderer.width(string), y, color);
	}
	
	public static void drawDiamond(double x, double y, int width, int height, int color) {
		drawTriangle(x, y + (double) height / 2,
				 x + width, y + (double) height / 2,
				 x + (double) width / 2, y,
				 color);		
		drawTriangle(x, y + (double) height / 2,
				 x + (double) width / 2, y + height,
				 x + width, y + (double) height / 2,
				 color);
	}
	
	public static void bindTexture(ResourceLocation id) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, id);
    }
    
	public static void bindTexture(int id) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, id);
	}
	
	public static void applyFilter(boolean force) {
		if (force || ClientSettings.textureFilter) {
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_LINEAR_MIPMAP_LINEAR);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_LINEAR);
		} else {
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_LINEAR_MIPMAP_NEAREST);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_NEAREST);
		}
	}
	
	public static void enable(int target) {
		GL11.glEnable(target);
	}
	
	public static void disable(int target) {
		GL11.glDisable(target);
	}
	
	public static void enableScissor() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		enable(GLC.GL_SCISSOR_TEST);
	}
	
	public static void disableScissor() {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		disable(GLC.GL_SCISSOR_TEST);
	}
	
	public static void applyScissor(int x, int y, int width, int height) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThread);
		GL11.glScissor(x, y, width, height);
	}
	
	public static void texEnvMode(int mode) {
		GL11.glTexEnvi(GLC.GL_TEXTURE_ENV, GLC.GL_TEXTURE_ENV_MODE, mode);
	}
	
    public static void startDraw() {
    	startDraw(DefaultVertexFormat.POSITION_TEX);
    }
    
    public static void startDrawNormal() {
    	startDraw(VF_POS_TEX_NORMAL);
    }
    
    public static void startDraw(VertexFormat vertexFormat) {
    	startDraw(VertexFormat.Mode.QUADS, vertexFormat);
    }
    
    public static void startDraw(VertexFormat.Mode mode, VertexFormat vertexFormat) {
    	vertexBuffer.begin(mode, vertexFormat);
    }
    
    public static void endDraw() {
    	tessellator.end();
    }
    
    public static void drawQuad(double x, double y, double w, double h) {
	    startDraw();
    	addQuad(x, y, w, h);
    	endDraw();
    }
    
    public static BufferBuilder getBuffer() {
    	return vertexBuffer;
    }
	
	public static void drawTriangle(double x1, double y1, double x2, double y2, double x3, double y3, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		RenderSystem.disableTexture();
		RenderSystem.setShaderColor(r, g, b, a);
		RenderSystem.setShader(GameRenderer::getPositionShader);
		startDraw(VertexFormat.Mode.TRIANGLES, DefaultVertexFormat.POSITION);
		vertexBuffer.vertex(x1, y1, 0).endVertex();
		vertexBuffer.vertex(x2, y2, 0).endVertex();
		vertexBuffer.vertex(x3, y3, 0).endVertex();
		endDraw();
		RenderSystem.enableTexture();
	}
	
	public static void drawLine(double x1, double y1, double x2, double y2, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
	
		RenderSystem.disableTexture();
		RenderSystem.setShaderColor(r, g, b, a);
		RenderSystem.setShader(GameRenderer::getPositionShader);
		startDraw(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
		vertexBuffer.vertex(x1, y1, 0).endVertex();
		vertexBuffer.vertex(x2, y2, 0).endVertex();
		endDraw();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableTexture();
	}	
	
	public static void drawOutlineCircle(double x, double y, double radius, double outline, int color) {
		int darken = ColorUtil.colorBrigtness(color, -3);
		RenderUtil.drawCircle(x, y, radius + outline, darken);
		RenderUtil.drawCircle(x, y, radius, color);
	}
	
	public static void drawCircle(double x, double y, double radius, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		
		RenderSystem.enableBlend();
	    RenderSystem.disableTexture();
	    RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(r, g, b, a);
		RenderSystem.setShader(GameRenderer::getPositionShader);
		drawCircleVertices(x, y, radius);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
	
	public static void drawCircleVertices(double x, double y, double radius) {
		double pi2 = Math.PI * 2;
		startDraw(VertexFormat.Mode.TRIANGLE_FAN, DefaultVertexFormat.POSITION);
		vertexBuffer.vertex(x, y, 0).endVertex();
		int sides = 50;
		for (int i = 0; i <= sides; i++) {
			double angle = (pi2 * i / sides) + Math.toRadians(180);
			double vx = x + Math.sin(angle) * radius;
			double vy = y + Math.cos(angle) * radius;
			vertexBuffer.vertex(vx, vy, 0).endVertex();
		}
		endDraw();
	}

	public static void fill(double x, double y, double w, double h, int color) {
		fill(Transformation.identity().getMatrix(), x, y, w, h, color);
	}
	
	public static void fill(PoseStack matrices, double x, double y, double w, double h, int color) {
		fill(matrices.last().pose(), x, y, w, h, color);
	}

	public static void fill(Matrix4f matrix4f, double x, double y, double w, double h, int color) {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;

		RenderSystem.enableBlend();
		RenderSystem.disableTexture();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShader(GameRenderer::getPositionColorShader);
		startDraw(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_COLOR);
		vertexBuffer.vertex(matrix4f, (float) x, (float) (y + h), 0.0F).color(r, g, b, a).endVertex();
		vertexBuffer.vertex(matrix4f, (float) (x + w), (float) (y + h), 0.0F).color(r, g, b, a).endVertex();
		vertexBuffer.vertex(matrix4f, (float) (x + w), (float) y, 0.0F).color(r, g, b, a).endVertex();
		vertexBuffer.vertex(matrix4f, (float) x, (float) y, 0.0F).color(r, g, b, a).endVertex();
		endDraw();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}
	
	public static void draw(PoseStack matrices, double x, double y, float w, float h) {
		startDrawNormal();
		draw(matrices, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
		endDraw();
	}
	
	public static void drawPlayerHead(PoseStack matrices, double x, double y, int w, int h) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		startDrawNormal();
		draw(matrices, x, y, w, h, 0.125F, 0.125F, 0.25F, 0.25F);
		draw(matrices, x, y, w, h, 0.625F, 0.125F, 0.75F, 0.25F);
		endDraw();
	}
	
	public static void draw(PoseStack matrices, double x, double y, int size, int isize, int ix, int iy, int tw, int th) {
		draw(matrices, x, y, size, size, ix, iy, isize, isize, tw, th);
	}
	
	public static void draw(PoseStack matrices, double x, double y, int w, int h, int ix, int iy, int iw, int ih, int tw, int th) {
		float minU = (float) ix / tw;
		float minV = (float) iy / th;
		float maxU = (float) (ix + iw) / tw;
		float maxV = (float) (iy + ih) / th;
		
		startDrawNormal();
		draw(matrices, x, y, w, h, minU, minV, maxU, maxV);
		endDraw();
	}
	
	public static void drawSkin(PoseStack matrices, MapSkin skin, double x, double y, float w, float h) {
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
		RenderSystem.enableCull();		
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		skin.bindTexture();
		startDrawNormal();
		draw(matrices, x, y, scaledBrd, scaledBrd, sMinU, sMinV, leftU, topV);
		draw(matrices, rightC, y, scaledBrd, scaledBrd, rightU, sMinV, sMaxU, topV);
		draw(matrices, x, bottomC, scaledBrd, scaledBrd, sMinU, bottomV, leftU, sMaxV);
		draw(matrices, rightC, bottomC, scaledBrd, scaledBrd, rightU, bottomV, sMaxU, sMaxV);
		
		if (skin.resizable) {
			draw(matrices, rightC, topC, scaledBrd, vSide, rightU, topV, sMaxU, bottomV);
			draw(matrices, x, topC, scaledBrd, vSide, sMinU, topV, leftU, bottomV);
			draw(matrices, leftC, topC, hSide, vSide, leftU, topV, rightU, bottomV);
			if (skin.repeating) {
				float tail = renderData.tail;
				float tailU = renderData.tailU;
				hSide = vSide;
				
				draw(matrices, leftC + hSide, y, tail, scaledBrd, leftU, sMinV, tailU, topV);
				draw(matrices, leftC + hSide, bottomC, tail, scaledBrd, leftU, bottomV, tailU, sMaxV);
			}
		
			draw(matrices, leftC, y, hSide, scaledBrd, leftU, sMinV, rightU, topV);
			draw(matrices, leftC, bottomC, hSide, scaledBrd, leftU, bottomV, rightU, sMaxV);
		} else {
			double left = leftC;
			int segments = renderData.hSegments;
			for (int i = 0; i < segments; i++) {
				draw(matrices, left, y, hSide, scaledBrd, leftU, sMinV, rightU, topV);
				draw(matrices, left, bottomC, hSide, scaledBrd, leftU, bottomV, rightU, sMaxV);
				left += hSide;
			}
			double top = topC;
			segments = renderData.vSegments;
			for (int i = 0; i < segments; i++) {
				draw(matrices, x, top, scaledBrd, vSide, sMinU, topV, leftU, bottomV);
				draw(matrices, rightC, top, scaledBrd, vSide, rightU, topV, sMaxU, bottomV);
				top += vSide;
			}
			
			float hTail = renderData.hTail;
			float vTail = renderData.vTail;
			float hTailU = renderData.hTailU;
			float vTailV = renderData.vTailV;
			
			draw(matrices, left, y, hTail, scaledBrd, leftU, sMinV, hTailU, topV);
			draw(matrices, left, bottomC, hTail, scaledBrd, leftU, bottomV, hTailU, sMaxV);
			draw(matrices, x, top, scaledBrd, vTail, sMinU, topV, leftU, vTailV);
			draw(matrices, rightC, top, scaledBrd, vTail, rightU, topV, sMaxU, vTailV);
		}
		endDraw();
	}
	
	public static void drawImage(PoseStack matrices, Image image, double x, double y, float w, float h) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		image.bindTexture();
		startDrawNormal();
		draw(matrices, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
		endDraw();
	}
	
	private static void draw(PoseStack matrices, double x, double y, float w, float h, float minU, float minV, float maxU, float maxV) {
		RenderSystem.enableBlend();
		RenderSystem.enableCull();

		matrices.pushPose();
		matrices.translate(x, y, 0);

		Matrix4f m4f = matrices.last().pose();
		Matrix3f m3f = matrices.last().normal();

		addVertices(m4f, m3f, w, h, minU, minV, maxU, maxV);

		matrices.popPose();
	}
	
	private static void addVertices(Matrix4f m4f, Matrix3f m3f, float maxX, float maxY, float minU, float minV, float maxU, float maxV) {
		vertex(m4f, m3f, 0.0F, 0.0F, minU, minV);
		vertex(m4f, m3f, 0.0F, maxY, minU, maxV);
		vertex(m4f, m3f, maxX, maxY, maxU, maxV);
		vertex(m4f, m3f, maxX, 0.0F, maxU, minV);
	}
	
	public static void addQuad(double x, double y, double w, double h) {
		addQuad(x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
	}
	
	public static void addQuad(double x, double y, double w, double h, float minU, float minV, float maxU, float maxV) {
		vertex(x, y + h, minU, maxV);
		vertex(x + w, y + h, maxU, maxV);
		vertex(x + w, y, maxU, minV);
		vertex(x, y, minU, minV);
	}

	public static void addQuad(PoseStack matrices, double x, double y, double w, double h, float minU, float minV, float maxU, float maxV) {
		Matrix4f m4f = matrices.last().pose();
		Matrix3f m3f = matrices.last().normal();

		vertex(m4f, m3f, x, y + h, minU, maxV);
		vertex(m4f, m3f, x + w, y + h, maxU, maxV);
		vertex(m4f, m3f, x + w, y, maxU, minV);
		vertex(m4f, m3f, x, y, minU, minV);
	}
	
	private static void vertex(Matrix4f m4f, Matrix3f m3f, double x, double y, float u, float v) {
		vertexBuffer.vertex(m4f, (float) x, (float) y, 1.0F).uv(u, v).normal(m3f, 0.0F, 1.0F, 0.0F).endVertex();
	}
	
	private static void vertex(double x, double y, float u, float v) {
    	vertexBuffer.vertex(x, y, 0.0).uv(u, v).endVertex();
    }

    static {
	    ImmutableMap.Builder<String, VertexFormatElement> immutableMapBuilder = ImmutableMap.builder();
	    immutableMapBuilder.put("Position", DefaultVertexFormat.ELEMENT_POSITION).put("UV0", DefaultVertexFormat.ELEMENT_UV0).put("Normal", DefaultVertexFormat.ELEMENT_NORMAL);

	    VF_POS_TEX_NORMAL = new VertexFormat(immutableMapBuilder.build());
    }
}
