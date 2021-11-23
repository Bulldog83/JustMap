package ru.bulldog.justmap.mixins.client;

import java.util.function.Supplier;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.bulldog.justmap.map.data.MapDataProvider;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin extends World {

	protected ClientWorldMixin(MutableWorldProperties properties, RegistryKey<World> registryKey,
			DimensionType dimensionType, Supplier<Profiler> supplier, boolean bl, boolean debugWorld, long l) {
		super(properties, registryKey, dimensionType, supplier, bl, debugWorld, l);
	}

	@Inject(method = "setBlockStateWithoutNeighborUpdates", at = @At("TAIL"))
	public void onSetBlockState(BlockPos pos, BlockState state, CallbackInfo info) {
		MapDataProvider.getManager().onSetBlockState(pos, state, this);
	}
}
