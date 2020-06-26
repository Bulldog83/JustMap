package ru.bulldog.justmap.util;

import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.dimension.DimensionType;

public class Dimension {
	public static int getId(RegistryKey<DimensionType> dimType) {
		if (isNether(dimType)) return -1;
		if (isOverworld(dimType)) return 0;
		if (isEnd(dimType)) return 1;
		
		return Integer.MIN_VALUE;
	}
	
	public static Identifier fromId(int id) {
		switch(id) {
			case -1: return DimensionType.THE_NETHER_REGISTRY_KEY.getValue();
			case 0: return DimensionType.OVERWORLD_REGISTRY_KEY.getValue();
			case 1: return DimensionType.THE_END_REGISTRY_KEY.getValue();
		}
		
		return new Identifier("unknown");
	}
	
	public static boolean isEnd(RegistryKey<DimensionType> dimType) {
		return dimType.equals(DimensionType.THE_END_REGISTRY_KEY);
	}

	public static boolean isNether(RegistryKey<DimensionType> dimType) {
		return dimType.equals(DimensionType.THE_NETHER_REGISTRY_KEY);
	}
	
	public static boolean isOverworld(RegistryKey<DimensionType> dimType) {
		return dimType.equals(DimensionType.OVERWORLD_REGISTRY_KEY);
	}
}
