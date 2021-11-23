package ru.bulldog.justmap.util.colors;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public interface ColorProvider {
	int getColor(BlockState state, World world, BlockPos pos);
}
