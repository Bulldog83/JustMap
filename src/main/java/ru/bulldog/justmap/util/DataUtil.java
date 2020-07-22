package ru.bulldog.justmap.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.map.data.Layer;

public class DataUtil {
	private static MinecraftClient minecraft = JustMapClient.MINECRAFT;
	private static BlockPos.Mutable currentPos = new BlockPos.Mutable();
	
	public static MinecraftClient getMinecraft() {
		return minecraft;
	}
	
	public static World getWorld() {
		if (minecraft.isIntegratedServerRunning()) {
			return minecraft.getServer().getWorld(minecraft.world.getRegistryKey());
		}
		return minecraft.world;
	}
	
	public static ClientWorld getClientWorld() {
		return minecraft.world;
	}
	
	public static GameOptions getGameOptions() {
		return minecraft.options;
	}

	public static int coordX() {
		if (minecraft.getCameraEntity() == null) return 0;
		return (int) (minecraft.getCameraEntity().getX() < 0.0 ? minecraft.getCameraEntity().getX() - 1.0 : minecraft.getCameraEntity().getX());
	}

	public static int coordZ() {
		if (minecraft.getCameraEntity() == null) return 0;
		return (int) (minecraft.getCameraEntity().getZ() < 0.0 ? minecraft.getCameraEntity().getZ() - 1.0 : minecraft.getCameraEntity().getZ());
	}

	public static int coordY() {
		if (minecraft.getCameraEntity() == null) return 0;
		return (int) (minecraft.getCameraEntity().getY());
	}

	public static BlockPos currentPos() {
		return currentPos.set(coordX(), coordY(), coordZ());
	}
	
	public static double doubleCoordX() {
		return minecraft.getCameraEntity().prevX + (minecraft.getCameraEntity().getX() - minecraft.getCameraEntity().prevX) * (double) minecraft.getTickDelta();
	}

	public static double doubleCoordZ() {
		return minecraft.getCameraEntity().prevZ + (minecraft.getCameraEntity().getZ() - minecraft.getCameraEntity().prevZ) * (double) minecraft.getTickDelta();
	}
	
	public static String posToString(BlockPos pos) {
		return posToString(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public static String posToString(double x, double y, double z) {
		return String.format("%d, %d, %d", (int) x, (int) y, (int) z);
	}
	
	public static Layer getLayer() {
		return getLayer(minecraft.world, currentPos()); 
	}
	
	public static int getLevel() {
		return getLevel(getLayer(), coordY());
	}
	
	public static Layer getLayer(World world, BlockPos pos) {
		if (Dimension.isNether(world.getDimensionRegistryKey())) {
			return Layer.NETHER;
		} else if (RuleUtil.needRenderCaves(world, pos)) {
			return Layer.CAVES;
		} else {
			return Layer.SURFACE;
		}
	}
	
	public static int getLevel(Layer layer, int y) {
		if (Layer.SURFACE.equals(layer)) return 0;
		return y / layer.height;
	}
}
