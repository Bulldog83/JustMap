package ru.bulldog.justmap.mixins;

import java.util.Iterator;
import java.util.List;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.render.entity.LivingEntityRenderer;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.EntityPose;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.MathHelper;
import ru.bulldog.justmap.config.Params;

@Mixin(LivingEntityRenderer.class)
public abstract class LivingEntityRendererMixin<T extends LivingEntity, M extends EntityModel<T>> extends EntityRenderer<T> implements FeatureRendererContext<T, M> {
	
	protected LivingEntityRendererMixin(EntityRenderDispatcher entityRenderDispatcher) {
		super(entityRenderDispatcher);
	}

	@Shadow
	protected M model;
	
	@Shadow
	@Final
	protected List<FeatureRenderer<T, M>> features;
	
	@Shadow
	private static int getOverlay(LivingEntity livingEntity, float f) { return 0; }
	
	@Shadow
	protected abstract float getAnimationProgress(T livingEntity, float f);
	
	@Shadow
	protected abstract void scale(T livingEntity, MatrixStack matrixStack, float f);
	
	@Shadow
	protected abstract float getWhiteOverlayProgress(T livingEntity, float f);
	
	@Inject(at = @At("HEAD"), method = "render", cancellable = true)
	public void renderStatic(T livingEntity, float f, float g, MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, int i, CallbackInfo ci) {
		if (Params.renderEntityModel && f == 0.0F && g == 0.0F) {
			matrixStack.push();
			this.model.child = livingEntity.isBaby();
			float h = MathHelper.lerpAngleDegrees(g, livingEntity.prevBodyYaw, livingEntity.bodyYaw);
			float j = MathHelper.lerpAngleDegrees(g, livingEntity.prevHeadYaw, livingEntity.headYaw);
			float k = j - h;
			float o;
			if (livingEntity.hasVehicle() && livingEntity.getVehicle() instanceof LivingEntity) {
				LivingEntity livingEntity2 = (LivingEntity) livingEntity.getVehicle();
				h = MathHelper.lerpAngleDegrees(g, livingEntity2.prevBodyYaw, livingEntity2.bodyYaw);
				k = j - h;
				o = MathHelper.wrapDegrees(k);
				if (o < -85.0F) {
					o = -85.0F;
				}
	
				if (o >= 85.0F) {
					o = 85.0F;
				}
	
				h = j - o;
				if (o * o > 2500.0F) {
					h += o * 0.2F;
				}
	
				k = j - h;
			}
	
			float m = MathHelper.lerp(g, livingEntity.prevPitch, livingEntity.pitch);
			float p = 0.0F;
			o = this.getAnimationProgress(livingEntity, g);
			this.applyTransforms(livingEntity, matrixStack, o, h, g);
			matrixStack.scale(-1.0F, -1.0F, 1.0F);
			this.scale(livingEntity, matrixStack, g);
			matrixStack.translate(0.0D, -1.5010000467300415D, 0.0D);
			
			float q = 0.0F;
			if (!livingEntity.hasVehicle() && livingEntity.isAlive()) {
				p = MathHelper.lerp(g, livingEntity.lastLimbDistance, livingEntity.limbDistance);
				q = livingEntity.limbAngle - livingEntity.limbDistance * (1.0F - g);
				if (livingEntity.isBaby()) {
					q *= 3.0F;
				}
	
				if (p > 1.0F) {
					p = 1.0F;
				}
			}
	
			this.model.setAngles(livingEntity, q, p, o, k, m);
			RenderLayer renderLayer = this.model.getLayer(this.getTexture(livingEntity));
			if (renderLayer != null) {
				VertexConsumer vertexConsumer = vertexConsumerProvider.getBuffer(renderLayer);
				int r = getOverlay(livingEntity, this.getWhiteOverlayProgress(livingEntity, g));
				this.model.render(matrixStack, vertexConsumer, i, r, 1.0F, 1.0F, 1.0F, 1.0F);					
			}
	
			if (!livingEntity.isSpectator()) {
				Iterator<?> var21 = this.features.iterator();
	
				while(var21.hasNext()) {
					@SuppressWarnings("unchecked")
					FeatureRenderer<T, M> featureRenderer = (FeatureRenderer<T, M>) var21.next();
					featureRenderer.render(matrixStack, vertexConsumerProvider, i, livingEntity, q, p, g, o, k, m);
				}
			}
			
			matrixStack.pop();
			
			ci.cancel();
		}
	}
	
	private void applyTransforms(T livingEntity, MatrixStack matrixStack, float f, float g, float h) {
		EntityPose entityPose = livingEntity.getPose();
	    if (entityPose != EntityPose.SLEEPING) {
	    	matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F - g));
	    }
	    if (livingEntity.isUsingRiptide()) {
	    	matrixStack.multiply(Vector3f.POSITIVE_X.getDegreesQuaternion(-90.0F - livingEntity.pitch));
	    	matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(((float)livingEntity.age + h) * -75.0F));
	    }
	}
}
