package ru.bulldog.justmap.mixins;

import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import ru.bulldog.justmap.minimap.waypoint.WaypointRenderer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(GameRenderer.class)
public abstract class GameRenderMixin {
	
	@Shadow
	protected Camera camera;
	
	@Shadow
	protected abstract double getFov(Camera camera, float f, boolean bl);
	
	@Inject(method = "render", at = @At("RETURN"))
	public void renderHUD(float f, long l, boolean bl, CallbackInfo ci) {
		WaypointRenderer.renderHUD(f, (float) this.getFov(camera, f, true));
	}
}
