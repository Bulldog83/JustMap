package ru.bulldog.justmap.map.data;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public interface MapDataManager {

	// World mapper management

	WorldMapper getWorldMapper();

	// Event callbacks

	void onChunkLoad(World world, WorldChunk worldChunk);

	void onSetBlockState(BlockPos pos, BlockState state, World world);

	void onTick(boolean isServer);

	void onWorldStop();
}
