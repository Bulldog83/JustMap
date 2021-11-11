package ru.bulldog.justmap.map;

import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3f;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.SpriteAtlas;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public class DirectionArrow extends Sprite {
	private final static VertexFormat vertexFormat = new VertexFormat(ImmutableMap.of("postition", VertexFormats.POSITION_ELEMENT, "texture", VertexFormats.TEXTURE_0_ELEMENT, "normal", VertexFormats.NORMAL_ELEMENT, "padding", VertexFormats.PADDING_ELEMENT));
	private static DirectionArrow ARROW;

	private DirectionArrow(Identifier texture, int w, int h) {
		super(SpriteAtlas.MAP_ICONS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	}

	public static void draw(double x, double y, int size, float rotation) {
		if (!ClientSettings.simpleArrow) {
			if (ARROW == null) {
				ARROW = new DirectionArrow(new Identifier(JustMap.MODID, "textures/icon/player_arrow.png"), 20, 20);
			}

			MatrixStack matrix = new MatrixStack();
			Tessellator tessellator = Tessellator.getInstance();
			BufferBuilder builder = tessellator.getBuffer();

			// FIXME: is mode 7 really quads?
			builder.begin(VertexFormat.DrawMode.QUADS, vertexFormat);

			VertexConsumer vertexConsumer = ARROW.getTextureSpecificVertexConsumer(builder);

			RenderUtil.bindTexture(ARROW.getId());

			RenderSystem.enableCull();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);

			matrix.push();
			matrix.translate(x, y, 0);
			matrix.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotation + 180));

			Matrix4f m4f = matrix.peek().getModel();
			Matrix3f m3f = matrix.peek().getNormal();

			addVertices(m4f, m3f, vertexConsumer, size);
			tessellator.draw();

			matrix.pop();
		} else {
			int l = 6;
			double a1 = Math.toRadians((rotation + 90) % 360);
			double a2 = Math.toRadians((rotation - 45) % 360);
			double a3 = Math.toRadians((rotation + 225) % 360);

			double x1 = x + Math.cos(a1) * l;
			double y1 = y + Math.sin(a1) * l;
			double x2 = x + Math.cos(a2) * l;
			double y2 = y + Math.sin(a2) * l;
			double x3 = x + Math.cos(a3) * l;
			double y3 = y + Math.sin(a3) * l;

			RenderSystem.setShader(GameRenderer::getPositionColorShader);
			RenderUtil.drawTriangle(x1, y1, x2, y2, x3, y3, Colors.RED);
		}
	}

	private static void addVertices(Matrix4f m4f, Matrix3f m3f, VertexConsumer vertexConsumer, int size) {
		float half = size / 2;

		vertexConsumer.vertex(m4f, -half, -half, 0.0F).texture(0.0F, 0.0F).normal(m3f, 0.0F, 1.0F, 0.0F).next();
		vertexConsumer.vertex(m4f, -half, half, 0.0F).texture(0.0F, 1.0F).normal(m3f, 0.0F, 1.0F, 0.0F).next();
		vertexConsumer.vertex(m4f, half, half, 0.0F).texture(1.0F, 1.0F).normal(m3f, 0.0F, 1.0F, 0.0F).next();
		vertexConsumer.vertex(m4f, half, -half, 0.0F).texture(1.0F, 0.0F).normal(m3f, 0.0F, 1.0F, 0.0F).next();
	}

}
