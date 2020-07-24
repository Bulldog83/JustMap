package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.block.BlockState;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.ChunkData;
import ru.bulldog.justmap.map.data.DimensionData;
import ru.bulldog.justmap.map.data.DimensionManager;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.util.DataUtil;

@Mixin(ClientWorld.class)
public abstract class ClientWorldMixin {
	
	@Inject(method = "setBlockStateWithoutNeighborUpdates", at = @At("TAIL"))
	public void onSetBlockState(BlockPos pos, BlockState state, CallbackInfo info) {
		World world = DataUtil.getClientWorld();
		WorldChunk worldChunk = world.getWorldChunk(pos);
		if (!worldChunk.isEmpty()) {
			IMap map = DataUtil.getMap();
			Layer layer = DataUtil.getLayer(world, pos);
			int level = DataUtil.getLevel(layer, pos.getY());
			if (layer.equals(map.getLayer()) && level == map.getLevel()) {
				DimensionData mapData = DimensionManager.getData();
				ChunkData mapChunk = mapData.getChunk(worldChunk.getPos());
				ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level));
			}
		}
	}
}
