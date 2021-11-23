package ru.bulldog.justmap.mixins.client;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.bulldog.justmap.map.data.MapDataProvider;
import ru.bulldog.justmap.util.DataUtil;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {

	@Inject(method = "joinWorld", at = @At("TAIL"))
	public void onJoinWorld(ClientWorld world, CallbackInfo cinfo) {
		MapDataProvider.getManager().onWorldChanged(world);
		DataUtil.updateWorld(world);
	}
}
