package ru.bulldog.justmap.map.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.GameRulesUtil;

public enum Layer {
	SURFACE("surface", 256),
	CAVES("caves", 8),
	NETHER("nether", 16);

	private static final int WORLD_HEIGHT = 256;

	private final String name;
	private final int height;

	Layer(String name, int height) {
		this.name = name;
		this.height = height;
	}

	public String getName() {
		return name;
	}

	public int getHeight() {
		return height;
	}

	public int getLevels() {
		return WORLD_HEIGHT / getHeight();
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
}
