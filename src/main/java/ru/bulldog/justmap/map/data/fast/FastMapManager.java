package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.map.data.MapDataManager;
import ru.bulldog.justmap.map.data.MapDataProvider;
import ru.bulldog.justmap.map.data.WorldMapper;

public class FastMapManager implements MapDataManager {
	public static final FastMapManager MANAGER = new FastMapManager();

	private FastWorldMapper createWorldMapper() {
		return new FastWorldMapper(MapDataProvider.getMultiworldManager().getCurrentWorld());
	}

	@Override
	public WorldMapper getWorldMapper() {
		return MapDataProvider.getMultiworldManager().getOrCreateWorldMapper(this::createWorldMapper);
	}

	public FastWorldMapper getFastWorldMapper() {
		return (FastWorldMapper) getWorldMapper();
	}

	@Override
	public void onChunkLoad(World world, WorldChunk worldChunk) {
		assert(world == getFastWorldMapper().getWorld());
		getFastWorldMapper().updateChunk(worldChunk);
	}

	@Override
	public void onSetBlockState(BlockPos pos, BlockState state, World world) {
		assert(world == getFastWorldMapper().getWorld());
		getFastWorldMapper().updateBlock(pos);
	}

	@Override
	public void onTick(boolean isServer) {
		// do nothing
	}

	@Override
	public void onWorldStop() {
		// do nothing
	}
}
