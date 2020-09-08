package ru.bulldog.justmap.mixins.server;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.poi.PointOfInterestType;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {
	protected ServerWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryKey,
			DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean debugWorld, long l) {
		super(properties, registryKey, dimensionType, supplier, bl, debugWorld, l);
	}

	@Inject(method = "onBlockChanged", at = @At("TAIL"))
	public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock) {
		Optional<PointOfInterestType> oldOptBlock = PointOfInterestType.from(oldBlock);
		Optional<PointOfInterestType> newOptBlock = PointOfInterestType.from(newBlock);
		if (!Objects.equals(oldOptBlock, newOptBlock)) {
			ChunkPos chunkPos = new ChunkPos(pos);
			WorldChunk worldChunk = (WorldChunk) this.getChunk(chunkPos.x, chunkPos.z, ChunkStatus.FULL, false);
			if (worldChunk != null) {
				
			}
		}
	}
}
