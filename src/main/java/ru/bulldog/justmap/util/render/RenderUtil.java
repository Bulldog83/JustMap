package ru.bulldog.justmap.util.render;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import org.lwjgl.opengl.GL11;

import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.map.minimap.skin.MapSkin.RenderData;
import ru.bulldog.justmap.util.colors.ColorUtil;

public class RenderUtil extends DrawableHelper {

	private RenderUtil() {}

	public final static RenderUtil DRAWER = new RenderUtil();

	private final static VertexFormat VF_POS_TEX_NORMAL = new VertexFormat(ImmutableMap.of("postition", VertexFormats.POSITION_ELEMENT, "texture", VertexFormats.TEXTURE_0_ELEMENT, "normal", VertexFormats.NORMAL_ELEMENT, "padding", VertexFormats.PADDING_ELEMENT));
	private final static Tessellator tessellator = Tessellator.getInstance();
	private final static BufferBuilder vertexBuffer = tessellator.getBuffer();
	private final static TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
	private final static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();

	public static int getWidth(Text text) {
		return textRenderer.getWidth(text);
	}

	public static int getWidth(String string) {
		return textRenderer.getWidth(string);
	}

	public static void drawCenteredString(MatrixStack matrices, String string, double x, double y, int color) {
		textRenderer.drawWithShadow(matrices, string, (float) (x - textRenderer.getWidth(string) / 2), (float) y, color);
	}

	public static void drawCenteredText(MatrixStack matrices, Text text, double x, double y, int color) {
		textRenderer.drawWithShadow(matrices, text, (float) (x - textRenderer.getWidth(text) / 2), (float) y, color);
	}

	public static void drawBoundedString(String string, int x, int y, int leftBound, int rightBound, int color) {
		MatrixStack matrices = new MatrixStack();
		drawBoundedString(matrices, string, x, y, leftBound, rightBound, color);
	}

	public static void drawBoundedString(MatrixStack matrices, String string, int x, int y, int leftBound, int rightBound, int color) {
		if (string == null) return;

		int stringWidth = textRenderer.getWidth(string);
		int drawX = x - stringWidth / 2;
		if (drawX < leftBound) {
			drawX = leftBound;
		} else if (drawX + stringWidth > rightBound) {
			drawX = rightBound - stringWidth;
		}

		drawStringWithShadow(matrices, textRenderer, string, drawX, y, color);
	}

	public static void drawRightAlignedString(MatrixStack matrices, String string, int x, int y, int color) {
		textRenderer.drawWithShadow(matrices, string, x - textRenderer.getWidth(string), y, color);
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
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, id);
	}

	public static void bindTexture(int id) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		RenderSystem.setShaderTexture(0, id);
	}

	public static void applyFilter(boolean force) {
		// This is not working properly. Is it even needed?
		/*
		if (force || ClientSettings.textureFilter) {
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_LINEAR_MIPMAP_LINEAR);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_LINEAR);
		} else {
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_LINEAR_MIPMAP_NEAREST);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_NEAREST);
		}
		 */
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
		// Crashes, and does not seem to be needed?
		// GL11.glTexEnvi(GLC.GL_TEXTURE_ENV, GLC.GL_TEXTURE_ENV_MODE, mode);
	}

	public static void startDraw() {
		startDraw(VertexFormats.POSITION_TEXTURE);
	}

	public static void startDrawNormal() {
		startDraw(VF_POS_TEX_NORMAL);
	}

	public static void startDraw(VertexFormat vertexFormat) {
		startDraw(VertexFormat.DrawMode.QUADS, vertexFormat);
	}

	public static void startDraw(VertexFormat.DrawMode mode, VertexFormat vertexFormat) {
		vertexBuffer.begin(mode, vertexFormat);
	}

	public static void endDraw() {
		tessellator.draw();
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
		startDraw(VertexFormat.DrawMode.TRIANGLES, VertexFormats.POSITION);
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
		RenderSystem.setShaderColor(r, g, b, a);
		RenderSystem.setShader(GameRenderer::getPositionShader);
		startDraw(VertexFormat.DrawMode.LINES, VertexFormats.POSITION);
		vertexBuffer.vertex(x1, y1, 0).next();
		vertexBuffer.vertex(x2, y2, 0).next();
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
		startDraw(VertexFormat.DrawMode.TRIANGLE_FAN, VertexFormats.POSITION);
		vertexBuffer.vertex(x, y, 0).next();
		int sides = 50;
		for (int i = 0; i <= sides; i++) {
			double angle = (pi2 * i / sides) + Math.toRadians(180);
			double vx = x + Math.sin(angle) * radius;
			double vy = y + Math.cos(angle) * radius;
			vertexBuffer.vertex(vx, vy, 0).next();
		}
		endDraw();
	}

	public static void fill(double x, double y, double w, double h, int color) {
		fill(AffineTransformation.identity().getMatrix(), x, y, w, h, color);
	}

	public static void fill(MatrixStack matrices, double x, double y, double w, double h, int color) {
		fill(matrices.peek().getModel(), x, y, w, h, color);
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
		startDraw(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
		vertexBuffer.vertex(matrix4f, (float) x, (float) (y + h), 0.0F).color(r, g, b, a).next();
		vertexBuffer.vertex(matrix4f, (float) (x + w), (float) (y + h), 0.0F).color(r, g, b, a).next();
		vertexBuffer.vertex(matrix4f, (float) (x + w), (float) y, 0.0F).color(r, g, b, a).next();
		vertexBuffer.vertex(matrix4f, (float) x, (float) y, 0.0F).color(r, g, b, a).next();
		endDraw();
		RenderSystem.enableTexture();
		RenderSystem.disableBlend();
	}

	public static void draw(MatrixStack matrices, double x, double y, float w, float h) {
		startDrawNormal();
		draw(matrices, vertexBuffer, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
		endDraw();
	}

	public static void drawPlayerHead(MatrixStack matrices, double x, double y, int w, int h) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		startDrawNormal();
		draw(matrices, x, y, w, h, 0.125F, 0.125F, 0.25F, 0.25F);
		draw(matrices, x, y, w, h, 0.625F, 0.125F, 0.75F, 0.25F);
		endDraw();
	}

	public static void draw(MatrixStack matrices, double x, double y, int w, int h, int ix, int iy, int iw, int ih, int tw, int th) {
		float minU = (float) ix / tw;
		float minV = (float) iy / th;
		float maxU = (float) (ix + iw) / tw;
		float maxV = (float) (iy + ih) / th;

		startDrawNormal();
		draw(matrices, vertexBuffer, x, y, w, h, minU, minV, maxU, maxV);
		endDraw();
	}

	public static void drawSkin(MatrixStack matrices, MapSkin skin, double x, double y, float w, float h) {
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

		draw(matrices, vertexBuffer, x, y, scaledBrd, scaledBrd, sMinU, sMinV, leftU, topV);
		draw(matrices, vertexBuffer, rightC, y, scaledBrd, scaledBrd, rightU, sMinV, sMaxU, topV);
		draw(matrices, vertexBuffer, x, bottomC, scaledBrd, scaledBrd, sMinU, bottomV, leftU, sMaxV);
		draw(matrices, vertexBuffer, rightC, bottomC, scaledBrd, scaledBrd, rightU, bottomV, sMaxU, sMaxV);

		if (skin.resizable) {
			draw(matrices, vertexBuffer, rightC, topC, scaledBrd, vSide, rightU, topV, sMaxU, bottomV);
			draw(matrices, vertexBuffer, x, topC, scaledBrd, vSide, sMinU, topV, leftU, bottomV);
			draw(matrices, vertexBuffer, leftC, topC, hSide, vSide, leftU, topV, rightU, bottomV);
			if (skin.repeating) {
				float tail = renderData.tail;
				float tailU = renderData.tailU;
				hSide = vSide;

				draw(matrices, vertexBuffer, leftC + hSide, y, tail, scaledBrd, leftU, sMinV, tailU, topV);
				draw(matrices, vertexBuffer, leftC + hSide, bottomC, tail, scaledBrd, leftU, bottomV, tailU, sMaxV);
			}

			draw(matrices, vertexBuffer, leftC, y, hSide, scaledBrd, leftU, sMinV, rightU, topV);
			draw(matrices, vertexBuffer, leftC, bottomC, hSide, scaledBrd, leftU, bottomV, rightU, sMaxV);
		} else {
			double left = leftC;
			int segments = renderData.hSegments;
			for (int i = 0; i < segments; i++) {
				draw(matrices, vertexBuffer, left, y, hSide, scaledBrd, leftU, sMinV, rightU, topV);
				draw(matrices, vertexBuffer, left, bottomC, hSide, scaledBrd, leftU, bottomV, rightU, sMaxV);
				left += hSide;
			}
			double top = topC;
			segments = renderData.vSegments;
			for (int i = 0; i < segments; i++) {
				draw(matrices, vertexBuffer, x, top, scaledBrd, vSide, sMinU, topV, leftU, bottomV);
				draw(matrices, vertexBuffer, rightC, top, scaledBrd, vSide, rightU, topV, sMaxU, bottomV);
				top += vSide;
			}

			float hTail = renderData.hTail;
			float vTail = renderData.vTail;
			float hTailU = renderData.hTailU;
			float vTailV = renderData.vTailV;

			draw(matrices, vertexBuffer, left, y, hTail, scaledBrd, leftU, sMinV, hTailU, topV);
			draw(matrices, vertexBuffer, left, bottomC, hTail, scaledBrd, leftU, bottomV, hTailU, sMaxV);
			draw(matrices, vertexBuffer, x, top, scaledBrd, vTail, sMinU, topV, leftU, vTailV);
			draw(matrices, vertexBuffer, rightC, top, scaledBrd, vTail, rightU, topV, sMaxU, vTailV);
		}

		endDraw();
	}

	public static void drawImage(MatrixStack matrices, Image image, double x, double y, float w, float h) {
		RenderSystem.setShader(GameRenderer::getPositionTexShader);
		image.bindTexture();
		startDrawNormal();
		draw(matrices, vertexBuffer, x, y, w, h, 0.0F, 0.0F, 1.0F, 1.0F);
		endDraw();
	}

	private static void draw(MatrixStack matrixStack, VertexConsumer vertexConsumer, double x, double y, float w, float h, float minU, float minV, float maxU, float maxV) {
		RenderSystem.enableBlend();
		RenderSystem.enableCull();

		matrixStack.push();
		matrixStack.translate(x, y, 0);

		Matrix4f m4f = matrixStack.peek().getModel();
		Matrix3f m3f = matrixStack.peek().getNormal();

		addVertices(m4f, m3f, vertexConsumer, w, h, minU, minV, maxU, maxV);

		matrixStack.pop();
	}

	private static void draw(MatrixStack matrices, double x, double y, float w, float h, float minU, float minV, float maxU, float maxV) {
		draw(matrices, vertexBuffer, x, y, w, h, minU, minV, maxU, maxV);
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
		vertex(x, y + h, 0.0, minU, maxV);
		vertex(x + w, y + h, 0.0, maxU, maxV);
		vertex(x + w, y, 0.0, maxU, minV);
		vertex(x, y, 0.0, minU, minV);
	}

	public static void addQuad(MatrixStack matrices, double x, double y, double w, double h, float minU, float minV, float maxU, float maxV) {
		Matrix4f m4f = matrices.peek().getModel();
		Matrix3f m3f = matrices.peek().getNormal();

		vertex(m4f, m3f, vertexBuffer, (float) x, (float) (y + h), 1.0F, minU, maxV);
		vertex(m4f, m3f, vertexBuffer, (float) (x + w), (float) (y + h), 1.0F, maxU, maxV);
		vertex(m4f, m3f, vertexBuffer, (float) (x + w), (float) y, 1.0F, maxU, minV);
		vertex(m4f, m3f, vertexBuffer, (float) x, (float) y, 1.0F, minU, minV);
	}

	private static void vertex(Matrix4f m4f, Matrix3f m3f, VertexConsumer vertexConsumer, float x, float y, float z, float u, float v) {
		vertexConsumer.vertex(m4f, x, y, z).texture(u, v).normal(m3f, 0.0F, 1.0F, 0.0F).next();
	}

	private static void vertex(double x, double y, double z, float u, float v) {
		vertexBuffer.vertex(x, y, z).texture(u, v).next();
	}
}
