package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;

import ru.bulldog.justmap.map.data.WorldManager;
import ru.bulldog.justmap.util.DataUtil;

@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {
	
	@Inject(method = "joinWorld", at = @At("TAIL"))
	public void onJoinWorld(ClientWorld world, CallbackInfo cinfo) {
		WorldManager.onWorldChanged(world);
		DataUtil.updateWorld(world);
	}
}
