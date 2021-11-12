package ru.bulldog.justmap.map.data;

import java.util.List;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.map.multiworld.WorldKey;

public interface MapDataManager {
	// WorldKey management

	List<WorldKey> registeredWorlds();

	// World map management

	MapRegionProvider getMapRegionProvider();

	int getMapHeight(Layer mapLayer, int mapLevel, int posX, int posZ);

	// Callbacks

	void onChunkLoad(World world, WorldChunk worldChunk);

	void onConfigUpdate();

	void onSetBlockState(BlockPos pos, BlockState state, World world);

	void onTick(boolean isServer);

	void onWorldStop();
}
