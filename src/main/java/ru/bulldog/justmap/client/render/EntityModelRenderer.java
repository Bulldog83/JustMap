package ru.bulldog.justmap.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.monster.Ghast;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.math.MathUtil;

@SuppressWarnings("ConstantConditions")
public class EntityModelRenderer {

	private static final Minecraft minecraft = Minecraft.getInstance();
	private static final EntityRenderDispatcher renderDispatcher = minecraft.getEntityRenderDispatcher();
	
	public static void renderModel(PoseStack matrices, MultiBufferSource consumerProvider, Entity entity, double x, double y) {
		
		LivingEntity livingEntity = (LivingEntity) entity;
		
		float headYaw = livingEntity.yHeadRot;
		float bodyYaw = livingEntity.yBodyRot;
		float prevHeadYaw = livingEntity.yHeadRotO;
		float prevBodyYaw = livingEntity.yBodyRotO;
		float pitch = livingEntity.getXRot();
		float prevPitch = livingEntity.xRotO;
		
		setPitchAndYaw(livingEntity);
		
		float scale = (float) getScale(livingEntity);
		int modelSize = ClientSettings.entityModelSize;
		
		matrices.pushPose();
		matrices.translate(x, y, 0);
		matrices.translate(modelSize / 4, modelSize / 2, 0);
		if (ClientSettings.rotateMap) {
			float rotation = (float) MathUtil.correctAngle(minecraft.player.yHeadRot);
			matrices.mulPose(Vector3f.ZP.rotationDegrees(rotation));
		} else {
			matrices.mulPose(Vector3f.XP.rotationDegrees(180.0F));
		}
		matrices.pushPose();
		matrices.scale(scale, scale, scale);
		renderDispatcher.setRenderShadow(false);
		renderDispatcher.render(livingEntity, 0.0, 0.0, 0.0, 0.0F, 1.0F, matrices, consumerProvider, 240);
		renderDispatcher.setRenderShadow(true);
		matrices.popPose();
		matrices.popPose();
		
		livingEntity.setXRot(pitch);
		livingEntity.yHeadRot = headYaw;
		livingEntity.yBodyRot = bodyYaw;
		livingEntity.xRotO = prevPitch;
		livingEntity.yHeadRotO = prevHeadYaw;
		livingEntity.yBodyRotO = prevBodyYaw;
	}
	
	private static double getScale(LivingEntity livingEntity) {
		int modelSize = ClientSettings.entityModelSize;
		double mapScale = JustMapClient.getMap().getScale();
		
		modelSize = (int) Math.min(modelSize, modelSize / mapScale);
		
		double scaleX = modelSize / Math.max(livingEntity.getBbWidth(), 1.0F);
		double scaleY = modelSize / Math.max(livingEntity.getBbHeight(), 1.0F);
		
		double scale = Math.max(Math.min(scaleX, scaleY), modelSize);
		
		if (livingEntity instanceof Ghast || livingEntity instanceof EnderDragon) {
			scale = modelSize / 3.0F;
		}
		if (livingEntity instanceof WaterAnimal) {
			scale = modelSize / 1.35F;
		}	
		if (livingEntity.isSleeping()) {
			scale = modelSize;
		}
		
		return scale;
	}
	
	private static void setPitchAndYaw(LivingEntity livingEntity) {
		livingEntity.setXRot(0.0F);
		livingEntity.xRotO = 0.0F;

		switch (livingEntity.getMotionDirection()) {
			case NORTH -> {
				livingEntity.yHeadRot = 0.0F;
				livingEntity.yBodyRot = 0.0F;
				livingEntity.yHeadRotO = 0.0F;
				livingEntity.yBodyRotO = 0.0F;
			}
			case WEST -> {
				livingEntity.yHeadRot = 135.0F;
				livingEntity.yBodyRot = 135.0F;
				livingEntity.yHeadRotO = 135.0F;
				livingEntity.yBodyRotO = 135.0F;
			}
			case EAST -> {
				livingEntity.yHeadRot = 225.0F;
				livingEntity.yBodyRot = 225.0F;
				livingEntity.yHeadRotO = 225.0F;
				livingEntity.yBodyRotO = 225.0F;
			}
			default -> {
				livingEntity.yHeadRot = 180.0F;
				livingEntity.yBodyRot = 180.0F;
				livingEntity.yHeadRotO = 180.0F;
				livingEntity.yBodyRotO = 180.0F;
			}
		}
	}
}
