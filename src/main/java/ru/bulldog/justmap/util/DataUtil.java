package ru.bulldog.justmap.util;

import java.util.function.Supplier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.Worldmap;
import ru.bulldog.justmap.map.data.Layer;

public class DataUtil {
	private static MinecraftClient minecraft = JustMapClient.MINECRAFT;
	private static BlockPos.Mutable currentPos = new BlockPos.Mutable();
	private static ClientWorld clientWorld = null;
	private static ServerWorld serverWorld = null;
	private static RegistryKey<DimensionType> dimension = null;
	private static Supplier<PersistentStateManager> persistentSupplier = null;
	private static Layer currentLayer = Layer.SURFACE;
	private static int currentLevel = 0;
	private static int coordX = 0;
	private static int coordY = 0;
	private static int coordZ = 0;
	
	public static void updateWorld(ClientWorld world) {
		clientWorld = world;
		dimension = world.getDimensionRegistryKey();
		if (minecraft.isIntegratedServerRunning()) {
			MinecraftServer server = minecraft.getServer();
			serverWorld = minecraft.getServer().getWorld(world.getRegistryKey());
			persistentSupplier = () -> {
				return server.getOverworld().getPersistentStateManager();
			};
		} else {
			serverWorld = null;
			persistentSupplier = null;
		}
	}
	
	public static void update() {
		currentLayer = Layer.SURFACE;
		currentLevel = 0;
		coordX = 0;
		coordY = 0;
		coordZ = 0;
		
		Entity posEntity = getPosEntity();
		if (posEntity == null) return;
		
		coordX = (int) (posEntity.getX() < 0.0 ? posEntity.getX() - 1.0 : posEntity.getX());
		coordZ = (int) (posEntity.getZ() < 0.0 ? posEntity.getZ() - 1.0 : posEntity.getZ());
		coordY = (int) (posEntity.getY());
		
		if (clientWorld == null) return;
		currentLayer = getLayer(clientWorld, currentPos());
		currentLevel = getLevel(currentLayer, coordY);
	}
	
	public static IMap getMap() {
		return minecraft.currentScreen instanceof Worldmap ? (Worldmap) minecraft.currentScreen : JustMapClient.MAP;
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
	
	public static ServerWorld getServerWorld() {
		return serverWorld;
	}
	
	public static RegistryKey<DimensionType> getDimension() {
		return dimension;
	}
	
	public static Supplier<PersistentStateManager> getPersistentSupplier() {
		return persistentSupplier;
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
	
	public static double doubleX() {
		Entity posEntity = getPosEntity();
		return posEntity.prevX + (posEntity.getX() - posEntity.prevX) * (double) minecraft.getTickDelta();
	}

	public static double doubleZ() {
		Entity posEntity = getPosEntity();
		return posEntity.prevZ + (posEntity.getZ() - posEntity.prevZ) * (double) minecraft.getTickDelta();
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
		}
		
		return Layer.SURFACE;
	}
	
	public static int getLevel(Layer layer, int y) {
		if (Layer.SURFACE.equals(layer)) return 0;
		return y / layer.height;
	}

	private static Entity getPosEntity() {
		return minecraft.getCameraEntity() != null ? minecraft.getCameraEntity() : minecraft.player;
	}
}
