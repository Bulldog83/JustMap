package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.PoseStack;
import net.minecraft.util.math.Matrix4f;

import ru.bulldog.justmap.client.render.WaypointRenderer;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
	
	@Inject(method = "render", at = @At(value = "RETURN", ordinal = 0))
	public void renderBeam(PoseStack matrixStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		if (!Minecraft.isFabulousGraphicsOrBetter()) WaypointRenderer.renderWaypoints(matrixStack, camera, f);
	}
	
	@Inject(method = "render", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/systems/RenderSystem;pushMatrix()V", shift = Shift.BEFORE, ordinal = 1))
	public void renderBeamFabulous(PoseStack matrixStack, float f, long l, boolean bl, Camera camera, GameRenderer gameRenderer, LightmapTextureManager lightmapTextureManager, Matrix4f matrix4f, CallbackInfo ci) {
		if (Minecraft.isFabulousGraphicsOrBetter()) WaypointRenderer.renderWaypoints(matrixStack, camera, f);
	}
}
