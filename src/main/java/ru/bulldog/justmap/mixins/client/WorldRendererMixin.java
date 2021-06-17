package ru.bulldog.justmap.mixins.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bulldog.justmap.client.render.WaypointRenderer;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
	
	@Inject(method = "renderLevel", at = @At(value = "RETURN", ordinal = 0))
	public void renderBeam(PoseStack matrixStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		//if (!Minecraft.useShaderTransparency())
			WaypointRenderer.renderWaypoints(matrixStack, camera, f);
	}
	
//	@Inject(method = "renderLevel", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;popPose()V", shift = Shift.AFTER, ordinal = 1))
//	public void renderBeamFabulous(PoseStack matrixStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightTexture lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
//		if (Minecraft.useShaderTransparency()) WaypointRenderer.renderWaypoints(matrixStack, camera, f);
//	}
}
