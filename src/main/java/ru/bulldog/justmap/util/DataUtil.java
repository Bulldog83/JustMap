package ru.bulldog.justmap.util;

import java.util.function.Supplier;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.options.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.dimension.Dimension;
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.screen.Worldmap;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;

public class DataUtil {
	private static MinecraftClient minecraft = JustMapClient.MINECRAFT;
	private static BlockPos.Mutable currentPos = new BlockPos.Mutable();
	private static ClientWorld clientWorld = null;
	private static ServerWorld serverWorld = null;
	private static Dimension dimension = null;
	private static Supplier<PersistentStateManager> persistentSupplier = null;
	private static Layer currentLayer = Layer.SURFACE;
	private static int currentLevel = 0;
	private static int coordX = 0;
	private static int coordY = 0;
	private static int coordZ = 0;
	
	public static void updateWorld(ClientWorld world) {
		clientWorld = world;
		dimension = world.dimension;
		if (minecraft.isIntegratedServerRunning()) {
			MinecraftServer server = minecraft.getServer();
			serverWorld = minecraft.getServer().getWorld(dimension.getType());
			persistentSupplier = () -> {
				return server.getWorld(DimensionType.OVERWORLD).getPersistentStateManager();
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
	
	public static boolean isOnline() {
		return !minecraft.isIntegratedServerRunning();
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
	
	public static Dimension getDimension() {
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
	
	public static double doubleX(Entity entity) {
		if (entity == null) return 0.0;
		return entity.prevX + (entity.getX() - entity.prevX) * (double) minecraft.getTickDelta();
	}

	public static double doubleZ(Entity entity) {
		if (entity == null) return 0.0;
		return entity.prevZ + (entity.getZ() - entity.prevZ) * (double) minecraft.getTickDelta();
	}
	
	public static double doubleX() {
		return doubleX(getPosEntity());
	}
	
	public static double doubleZ() {
		return doubleZ(getPosEntity());
	}

	static boolean hasSkyLight(World world, BlockPos pos) {
		if (world.getLightLevel(LightType.SKY, pos) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.up()) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.north()) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.east()) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.south()) > 0) return true;
		if (world.getLightLevel(LightType.SKY, pos.west()) > 0) return true;
		
		return false;
	}
	
	public static Layer getLayer() {
		return currentLayer;
	}
	
	public static int getLevel() {
		return currentLevel;
	}
	
	public static Layer getLayer(World world, BlockPos pos) {
		if (DimensionUtil.isNether(world.dimension)) {
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
