package ru.bulldog.justmap.util;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

public class Dimension {
	public static int getId(World world) {
		if (isNether(world)) return -1;
		if (isOverworld(world)) return 0;
		if (isEnd(world)) return 1;

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

	public static boolean isEnd(World world) {
		return isEnd(world.getRegistryKey().getValue());
	}

	public static boolean isNether(World world) {
		return isNether(world.getRegistryKey().getValue());
	}

	public static boolean isOverworld(World world) {
		return isOverworld(world.getRegistryKey().getValue());
	}

	public static boolean isEnd(Identifier dimId) {
		return dimId.equals(World.END.getValue());
	}

	public static boolean isNether(Identifier dimId) {
		return dimId.equals(World.NETHER.getValue());
	}

	public static boolean isOverworld(Identifier dimId) {
		return dimId.equals(World.OVERWORLD.getValue());
	}
}
