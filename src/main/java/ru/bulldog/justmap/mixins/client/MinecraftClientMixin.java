package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import ru.bulldog.justmap.client.network.SpigotAgent;
import ru.bulldog.justmap.util.DataUtil;

@Mixin(MinecraftClient.class)
public abstract class MinecraftClientMixin {
	
	@Inject(method = "joinWorld", at = @At("TAIL"))
	public void onJoinWorld(ClientWorld world, CallbackInfo cinfo) {
		System.out.println("Join world: " + world.getRegistryKey());
		SpigotAgent.onWorldChanged();
		DataUtil.updateWorld(world);
	}
}
