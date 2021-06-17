package ru.bulldog.justmap.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.SpriteAtlas;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public class DirectionArrow extends TextureAtlasSprite {
	private static DirectionArrow ARROW;
	
	private DirectionArrow(ResourceLocation texture, int w, int h) {
		super(SpriteAtlas.MAP_ICONS, new TextureAtlasSprite.Info(texture, w, h, AnimationMetadataSection.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	}
	
	public static void draw(int x, int y, float rotation) {
		draw(x, y, 14, rotation);
	}
	
	public static void draw(double x, double y, int size, float rotation) {		
		if (!ClientSettings.simpleArrow) {
			if (ARROW == null) {
				ARROW = new DirectionArrow(new ResourceLocation(JustMap.MODID, "textures/icon/player_arrow.png"), 20, 20);
			}
			
			PoseStack matrix = new PoseStack();
			Tesselator tessellator = Tesselator.getInstance();
			BufferBuilder builder = tessellator.getBuilder();
			
			builder.begin(VertexFormat.Mode.QUADS, RenderUtil.VF_POS_TEX_NORMAL);
			
			VertexConsumer vertexConsumer = ARROW.wrap(builder);
			
			RenderUtil.bindTexture(ARROW.getName());
			
			RenderSystem.enableCull();
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			
			matrix.pushPose();
			matrix.translate(x, y, 0);
			matrix.mulPose(Vector3f.ZP.rotationDegrees(rotation + 180));
			
			Matrix4f m4f = matrix.last().pose();
			Matrix3f m3f = matrix.last().normal();
			
			addVertices(m4f, m3f, vertexConsumer, size);
			tessellator.end();
			
			matrix.popPose();
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
			
			RenderUtil.drawTriangle(x1, y1, x2, y2, x3, y3, Colors.RED);
		}
	}
	
	private static void addVertices(Matrix4f m4f, Matrix3f m3f, VertexConsumer vertexConsumer, int size) {
		float half = size / 2;		
		
		vertexConsumer.vertex(m4f, -half, -half, 0.0F).uv(0.0F, 0.0F).normal(m3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(m4f, -half, half, 0.0F).uv(0.0F, 1.0F).normal(m3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(m4f, half, half, 0.0F).uv(1.0F, 1.0F).normal(m3f, 0.0F, 1.0F, 0.0F).endVertex();
		vertexConsumer.vertex(m4f, half, -half, 0.0F).uv(1.0F, 0.0F).normal(m3f, 0.0F, 1.0F, 0.0F).endVertex();
	}
	
}
