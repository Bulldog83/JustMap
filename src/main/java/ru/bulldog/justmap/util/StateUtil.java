package ru.bulldog.justmap.util;

import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.Material;

public class StateUtil {
	public static final BlockState AIR_STATE = Blocks.AIR.getDefaultState();
	public static final BlockState CAVE_AIR_STATE = Blocks.CAVE_AIR.getDefaultState();
	public static final BlockState VOID_AIR_STATE = Blocks.VOID_AIR.getDefaultState();
	
	public static boolean isAir(BlockState state) {
		return state.isAir() || state == AIR_STATE || state == CAVE_AIR_STATE || state == VOID_AIR_STATE;
	}
	
	public static boolean isLiquid(BlockState state, boolean lava) {
		return state.getMaterial().isLiquid() && (lava || state.getMaterial() != Material.LAVA);
	}
	
	public static boolean isUnderwater(BlockState state) {
		return isLiquid(state, false) || state.getMaterial() == Material.UNDERWATER_PLANT;
	}
	
	public static boolean isPlant(BlockState state) {
		Material material = state.getMaterial();
		return material == Material.PLANT || material == Material.REPLACEABLE_PLANT ||
			   material == Material.UNUSED_PLANT;
	}
}
