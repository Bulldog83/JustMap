package ru.bulldog.justmap.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;

public class Dimension {
	public static int getId(Level world) {
		if (isNether(world)) return -1;
		if (isOverworld(world)) return 0;
		if (isEnd(world)) return 1;
		
		return Integer.MIN_VALUE;
	}
	
	public static ResourceLocation fromId(int id) {
		switch(id) {
			case -1: return DimensionType.NETHER_LOCATION.location();
			case 0: return DimensionType.OVERWORLD_LOCATION.location();
			case 1: return DimensionType.END_LOCATION.location();
		}
		
		return new ResourceLocation("unknown");
	}
	
	public static boolean isEnd(Level world) {
		return isEnd(world.dimension().location());
	}

	public static boolean isNether(Level world) {
		return isNether(world.dimension().location());
	}
	
	public static boolean isOverworld(Level world) {
		return isOverworld(world.dimension().location());
	}
	
	public static boolean isEnd(ResourceLocation dimId) {
		return dimId.equals(Level.END.location());
	}
	
	public static boolean isNether(ResourceLocation dimId) {
		return dimId.equals(Level.NETHER.location());
	}
	
	public static boolean isOverworld(ResourceLocation dimId) {
		return dimId.equals(Level.OVERWORLD.location());
	}
}
