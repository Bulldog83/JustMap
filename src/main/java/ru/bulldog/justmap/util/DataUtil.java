package ru.bulldog.justmap.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.map.data.Layer;

public class DataUtil {
	private static MinecraftClient minecraft = JustMapClient.MINECRAFT;
	private static BlockPos.Mutable currentPos = new BlockPos.Mutable();
	private static ClientWorld clientWorld = null;
	private static ServerWorld serverWorld = null;
	private static Layer currentLayer = Layer.SURFACE;
	private static int currentLevel = 0;
	private static double doubleX = 0.0;
	private static double doubleZ = 0.0;
	private static int coordX = 0;
	private static int coordY = 0;
	private static int coordZ = 0;
	
	public static void update() {
		if (clientWorld != minecraft.world) {
			clientWorld = minecraft.world;
			if (minecraft.isIntegratedServerRunning()) {
				serverWorld = minecraft.getServer().getWorld(clientWorld.getRegistryKey());
			} else {
				serverWorld = null;
			}
		}
		if (minecraft.getCameraEntity() == null && minecraft.player == null) return;
		Entity posEntity = minecraft.getCameraEntity() != null ? minecraft.getCameraEntity() : minecraft.player;
		float tickDelta = minecraft.getTickDelta();
		doubleX = posEntity.prevX + (posEntity.getX() - posEntity.prevX) * (double) tickDelta;
		doubleZ = posEntity.prevZ + (posEntity.getZ() - posEntity.prevZ) * (double) tickDelta;
		coordX = (int) (posEntity.getX() < 0.0 ? posEntity.getX() - 1.0 : posEntity.getX());
		coordZ = (int) (posEntity.getZ() < 0.0 ? posEntity.getZ() - 1.0 : posEntity.getZ());
		coordY = (int) (minecraft.getCameraEntity().getY());
		currentLayer = getLayer(minecraft.world, currentPos());
		currentLevel = getLevel(currentLayer, coordY);
	}
	
	public static MinecraftClient getMinecraft() {
		return minecraft;
	}
	
	public static World getWorld() {
		return serverWorld != null ? serverWorld : clientWorld;
	}
	
	public static ClientWorld getClientWorld() {
		return clientWorld;
	}
	
	public static GameOptions getGameOptions() {
		return minecraft.options;
	}

	public static int coordX() {
		return coordX;
	}

	public static int coordZ() {
		return coordZ;
	}

	public static int coordY() {
		return coordY;
	}

	public static BlockPos currentPos() {
		return currentPos.set(coordX, coordY, coordZ);
	}
	
	public static double doubleCoordX() {
		return doubleX;
	}

	public static double doubleCoordZ() {
		return doubleZ;
	}
	
	public static String posToString(BlockPos pos) {
		return posToString(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public static String posToString(double x, double y, double z) {
		return String.format("%d, %d, %d", (int) x, (int) y, (int) z);
	}
	
	public static Layer getLayer() {
		return currentLayer;
	}
	
	public static int getLevel() {
		return currentLevel;
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
