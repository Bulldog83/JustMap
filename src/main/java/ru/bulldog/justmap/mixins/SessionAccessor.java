package ru.bulldog.justmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.storage.LevelStorage;

@Mixin(MinecraftServer.class)
public interface SessionAccessor {
	@Accessor(value = "session")
	LevelStorage.Session getServerSession();
}
