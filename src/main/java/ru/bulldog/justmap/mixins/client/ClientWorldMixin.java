package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
	@Inject(method = "setBlockStateWithoutNeighborUpdates", at = @At("TAIL"))
	public void onSetBlockState(BlockPos pos, BlockState state, CallbackInfo info) {
		MinecraftClient minecraft = JustMapClient.MINECRAFT;
		World world = JustMapClient.MINECRAFT.world;
		if (world != null) {
			if (minecraft.isIntegratedServerRunning()) {
				world = minecraft.getServer().getWorld(world.getRegistryKey());
			}
			ChunkUpdateListener.accept(new ChunkUpdateEvent(world, new ChunkPos(pos)));
		}
	}
}
