package ru.bulldog.justmap.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;
import net.minecraft.state.property.Properties;
import net.minecraft.tag.FluidTags;

public class BlockStateUtil {
	public static final BlockState AIR = Blocks.AIR.getDefaultState();
	public static final BlockState CAVE_AIR = Blocks.CAVE_AIR.getDefaultState();
	public static final BlockState VOID_AIR = Blocks.VOID_AIR.getDefaultState();

	// if "includePlants" is true, plants will count as a full block, and will hence *not* be skipped
	public static boolean isSkippedBlockState(BlockState state, boolean includeLiquids, boolean includePlants) {
		return BlockStateUtil.isAir(state) || (!includeLiquids && isLiquid(state, false)) || (!includePlants && isPlant(state));
	}

	public static boolean isAir(BlockState state) {
		return state.isAir() || state == AIR || state == CAVE_AIR || state == VOID_AIR;
	}

	public static boolean isLiquid(BlockState state, boolean lava) {
		Material material = state.getMaterial();
		return material.isLiquid() && (lava || material != Material.LAVA);
	}

	public static boolean isWater(BlockState state) {
		return !isSeaweed(state) && state.getFluidState().isIn(FluidTags.WATER);
	}

	public static boolean isPlant(BlockState state) {
		Material material = state.getMaterial();
		return material == Material.PLANT || material == Material.REPLACEABLE_PLANT ||
			   isSeaweed(state);
	}

	public static boolean isSeaweed(BlockState state) {
		Material material = state.getMaterial();
		return material == Material.UNDERWATER_PLANT || material == Material.REPLACEABLE_UNDERWATER_PLANT;
	}

	public static boolean isWaterlogged(BlockState state) {
		if (state.contains(Properties.WATERLOGGED))
			return state.get(Properties.WATERLOGGED);

		return isSeaweed(state);
	}
}
