package ru.bulldog.justmap.map.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.GameRulesUtil;

public class Layer {
	public final static Layer SURFACE = new Layer("surface", 256);
	public final static Layer CAVES = new Layer("caves", 8);
	public final static Layer NETHER = new Layer("nether", 16);

	public final String name;
	public final int height;

	private Layer(String name, int height) {
		this.name = name;
		this.height = height;
	}

	public static Layer getLayer(World world, BlockPos pos) {
		if (Dimension.isNether(world)) {
			return NETHER;
		} else if (GameRulesUtil.allowCaves() && shouldRenderCaves(world, pos)) {
			return CAVES;
		}
		return SURFACE;
	}

	public static int getLevel(Layer layer, int y) {
		if (SURFACE.equals(layer)) return 0;
		return y / layer.height;
	}

	private static boolean shouldRenderCaves(World world, BlockPos pos) {
		if (Dimension.isEnd(world)) {
			return false;
		}

		DimensionType dimType = world.getDimension();
		if (dimType.hasCeiling() || !dimType.hasSkyLight()) {
			return true;
		}

		return (!world.isSkyVisibleAllowingSea(pos) && !hasSkyLight(world, pos) ||
				world.getRegistryKey().getValue().equals(DimensionType.OVERWORLD_CAVES_REGISTRY_KEY.getValue()));
	}

	private static boolean hasSkyLight(World world, BlockPos pos) {
		// FIXME: this is a bit expensive for repeating use...
		if (world.getLightLevel(LightType.SKY, pos) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.up()) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.north()) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.east()) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.south()) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.west()) > 0) return true;

		return false;
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + height;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Layer)) return false;

		Layer layer = (Layer) obj;
		return this.name == layer.name &&
			   this.height == layer.height;
	}

	@Override
	public String toString() {
		return this.name.toUpperCase();
	}
}
