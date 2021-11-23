package ru.bulldog.justmap.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.dragon.EnderDragonEntity;
import net.minecraft.entity.mob.GhastEntity;
import net.minecraft.entity.mob.WaterCreatureEntity;
import net.minecraft.util.math.Vec3f;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.math.MathUtil;

public class EntityModelRenderer {

	private static final MinecraftClient minecraft = MinecraftClient.getInstance();
	private static final EntityRenderDispatcher renderDispatcher = minecraft.getEntityRenderDispatcher();

	public static void renderModel(MatrixStack matrices, VertexConsumerProvider consumerProvider, Entity entity, double x, double y) {

		LivingEntity livingEntity = (LivingEntity) entity;

		float headYaw = livingEntity.headYaw;
		float bodyYaw = livingEntity.bodyYaw;
		float prevHeadYaw = livingEntity.prevHeadYaw;
		float prevBodyYaw = livingEntity.prevBodyYaw;
		float pitch = livingEntity.getPitch();
		float prevPitch = livingEntity.prevPitch;

		setPitchAndYaw(livingEntity);

		float scale = (float) getScale(livingEntity);
		int modelSize = ClientSettings.entityModelSize;

		matrices.push();
		matrices.translate(x, y, 0);
		matrices.translate(modelSize / 4, modelSize / 2, 0);
		if (ClientSettings.rotateMap) {
			float rotation = (float) MathUtil.correctAngle(minecraft.player.headYaw);
			matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(rotation));
		} else {
			matrices.multiply(Vec3f.POSITIVE_X.getDegreesQuaternion(180.0F));
		}
		matrices.push();
		matrices.scale(scale, scale, scale);
		renderDispatcher.setRenderShadows(false);
		renderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrices, consumerProvider, 240);
		renderDispatcher.setRenderShadows(true);
		matrices.pop();
		matrices.pop();

		livingEntity.setPitch(pitch);
		livingEntity.headYaw = headYaw;
		livingEntity.bodyYaw = bodyYaw;
		livingEntity.prevPitch = prevPitch;
		livingEntity.prevHeadYaw = prevHeadYaw;
		livingEntity.prevBodyYaw = prevBodyYaw;
	}

	private static double getScale(LivingEntity livingEntity) {
		int modelSize = ClientSettings.entityModelSize;
		double mapScale = JustMapClient.getMiniMap().getScale();

		modelSize = (int) Math.min(modelSize, modelSize / mapScale);

		double scaleX = modelSize / Math.max(livingEntity.getWidth(), 1.0F);
		double scaleY = modelSize / Math.max(livingEntity.getHeight(), 1.0F);

		double scale = Math.max(Math.min(scaleX, scaleY), modelSize);

		if (livingEntity instanceof GhastEntity || livingEntity instanceof EnderDragonEntity) {
			scale = modelSize / 3.0F;
		}
		if (livingEntity instanceof WaterCreatureEntity) {
			scale = modelSize / 1.35F;
		}
		if (livingEntity.isSleeping()) {
			scale = modelSize;
		}

		return scale;
	}

	private static void setPitchAndYaw(LivingEntity livingEntity) {
		livingEntity.setPitch(0.0F);
		livingEntity.prevPitch = 0.0F;

		switch(livingEntity.getMovementDirection()) {
			case NORTH:
				livingEntity.headYaw = 0.0F;
				livingEntity.bodyYaw = 0.0F;
				livingEntity.prevHeadYaw = 0.0F;
				livingEntity.prevBodyYaw = 0.0F;
				break;
			case WEST:
				livingEntity.headYaw = 135.0F;
				livingEntity.bodyYaw = 135.0F;
				livingEntity.prevHeadYaw = 135.0F;
				livingEntity.prevBodyYaw = 135.0F;
				break;
			case EAST:
				livingEntity.headYaw = 225.0F;
				livingEntity.bodyYaw = 225.0F;
				livingEntity.prevHeadYaw = 225.0F;
				livingEntity.prevBodyYaw = 225.0F;
				break;
			default:
				livingEntity.headYaw = 180.0F;
				livingEntity.bodyYaw = 180.0F;
				livingEntity.prevHeadYaw = 180.0F;
				livingEntity.prevBodyYaw = 180.0F;
			break;
		}
	}
}
