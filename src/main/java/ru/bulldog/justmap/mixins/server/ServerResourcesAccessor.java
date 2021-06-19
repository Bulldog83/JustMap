package ru.bulldog.justmap.mixins.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.ServerResources;

@Mixin(MinecraftServer.class)
public interface ServerResourcesAccessor {
	@Accessor
	ServerResources getResources();
}
