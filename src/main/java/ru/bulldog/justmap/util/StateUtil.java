package ru.bulldog.justmap.util;

import net.minecraft.tags.FluidTags;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.Material;

public class StateUtil {
	public static final BlockState AIR = Blocks.AIR.defaultBlockState();
	public static final BlockState CAVE_AIR = Blocks.CAVE_AIR.defaultBlockState();
	public static final BlockState VOID_AIR = Blocks.VOID_AIR.defaultBlockState();
	
	public static boolean checkState(BlockState state, boolean liquids, boolean plants) {
		return StateUtil.isAir(state) || (!liquids && isLiquid(state, false)) || (!plants && isPlant(state));
	}
	
	public static boolean isAir(BlockState state) {
		return state.isAir() || state == AIR || state == CAVE_AIR || state == VOID_AIR;
	}
	
	public static boolean isLiquid(BlockState state, boolean lava) {
		Material material = state.getMaterial();
		return material.isLiquid() && (lava || material != Material.LAVA);
	}
	
	public static boolean isWater(BlockState state) {
		return !isSeaweed(state) && state.getFluidState().is(FluidTags.WATER);
	}
	
	public static boolean isPlant(BlockState state) {
		Material material = state.getMaterial();
		return material == Material.PLANT || material == Material.REPLACEABLE_PLANT ||
			   material == Material.WATER_PLANT || isSeaweed(state);
	}
	
	public static boolean isSeaweed(BlockState state) {
		Material material = state.getMaterial();
		return material == Material.WATER_PLANT || material == Material.REPLACEABLE_WATER_PLANT;
	}
	
	public static boolean isWaterlogged(BlockState state) {
		if (state.hasProperty(BlockStateProperties.WATERLOGGED))
			return state.getValue(BlockStateProperties.WATERLOGGED);
		
		return isSeaweed(state);
	}
}
