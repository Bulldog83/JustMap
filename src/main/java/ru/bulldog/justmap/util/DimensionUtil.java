package ru.bulldog.justmap.util;

import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

public class DimensionUtil {
	public static Identifier fromRawId(int id) {
		switch(id) {
			case -1: return DimensionType.getId(DimensionType.THE_NETHER);
			case 0: return DimensionType.getId(DimensionType.OVERWORLD);
			case 1: return DimensionType.getId(DimensionType.THE_END);
		}
		return new Identifier("unknown");
	}
	
	public static Identifier getId(Dimension dimension) {
		return DimensionType.getId(dimension.getType());
	}
	
	public static Identifier getId(World world) {
		return getId(world.dimension);
	}
	
	public static boolean isEnd(Identifier dimId) {
		return dimId.equals(DimensionType.getId(DimensionType.THE_END));
	}
	
	public static boolean isNether(Identifier dimId) {
		return dimId.equals(DimensionType.getId(DimensionType.THE_NETHER));
	}
	
	public static boolean isOverworld(Identifier dimId) {
		return dimId.equals(DimensionType.getId(DimensionType.OVERWORLD));
	}
	
	public static boolean isEnd(Dimension dimension) {
		return isEnd(getId(dimension));
	}
	
	public static boolean isNether(Dimension dimension) {
		return isNether(getId(dimension));
	}
	
	public static boolean isOverworld(Dimension dimension) {
		return isOverworld(getId(dimension));
	}
}
