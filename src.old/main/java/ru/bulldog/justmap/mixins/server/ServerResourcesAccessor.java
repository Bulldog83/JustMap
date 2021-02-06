package ru.bulldog.justmap.mixins.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.resource.ServerResourceManager;
import net.minecraft.server.MinecraftServer;

@Mixin(MinecraftServer.class)
public interface ServerResourcesAccessor {
	@Accessor
	ServerResourceManager getServerResourceManager();
}
