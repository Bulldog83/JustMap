package ru.bulldog.justmap.util;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.option.GameOptions;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.registry.BuiltinRegistries;
import net.minecraft.util.registry.DynamicRegistryManager;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.LightType;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.screen.WorldmapScreen;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.server.JustMapServer;
import ru.bulldog.justmap.util.math.MathUtil;

public class DataUtil {
	private static final BlockPos.Mutable currentPos = new BlockPos.Mutable();
	private static final DynamicRegistryManager registryManager = DynamicRegistryManager.create();
	private static ClientWorld clientWorld = null;
	private static ServerWorld serverWorld = null;
	private static Supplier<PersistentStateManager> persistentSupplier = null;
	private static Layer currentLayer = Layer.SURFACE;
	private static int currentLevel = 0;
	private static int coordX = 0;
	private static int coordY = 0;
	private static int coordZ = 0;

	@Environment(EnvType.CLIENT)
	public static void updateWorld(ClientWorld world) {
		clientWorld = world;
		MinecraftClient minecraft = MinecraftClient.getInstance();
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

		coordX = MathUtil.floor(posEntity.getX());
		coordZ = MathUtil.floor(posEntity.getZ());
		coordY = (int) posEntity.getY();

		if (clientWorld == null) return;
		currentLayer = getLayer(getWorld(), currentPos());
		currentLevel = getLevel(currentLayer, coordY);
	}

	public static boolean isOnline() {
		return !MinecraftClient.getInstance().isIntegratedServerRunning();
	}

	public static IMap getCurrentlyShownMap() {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		return minecraft.currentScreen instanceof WorldmapScreen ? (WorldmapScreen) minecraft.currentScreen : JustMapClient.getMiniMap();
	}

	@Environment(EnvType.SERVER)
	public static World getServerWorld(RegistryKey<World> worldKey) {
		return JustMapServer.getServer().getWorld(worldKey);
	}

	public static World getWorld() {
		return serverWorld != null ? serverWorld : clientWorld;
	}

	public static Registry<Biome> getBiomeRegistry(World world) {
		return world.getRegistryManager().get(Registry.BIOME_KEY);
	}

	public static Identifier getBiomeId(World world, Biome biome) {
		Identifier biomeId = getBiomeRegistry(world).getId(biome);
		return biomeId != null ? biomeId : BuiltinRegistries.BIOME.getId(biome);
	}

	public static Registry<Biome> getBiomeRegistry() {
		if (JustMap.getSide() == EnvType.CLIENT) {
			MinecraftClient minecraft = MinecraftClient.getInstance();
			ClientPlayNetworkHandler networkHandler = minecraft.getNetworkHandler();
			if (networkHandler != null) {
				return minecraft.getNetworkHandler().getRegistryManager().get(Registry.BIOME_KEY);
			}
			return registryManager.get(Registry.BIOME_KEY);
		}
		MinecraftServer server = JustMapServer.getServer();
		if (server != null) {
			return server.getRegistryManager().get(Registry.BIOME_KEY);
		}
		return registryManager.get(Registry.BIOME_KEY);
	}

	public static ClientWorld getClientWorld() {
		return clientWorld;
	}

	public static ServerWorld getServerWorld() {
		return serverWorld;
	}

	public static Supplier<PersistentStateManager> getPersistentSupplier() {
		return persistentSupplier;
	}

	public static GameOptions getGameOptions() {
		MinecraftClient minecraft = MinecraftClient.getInstance();
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

	public static double doubleX(Entity entity, float delta) {
		if (entity == null) return 0.0;
		return MathUtil.lerp(delta, entity.prevX, entity.getX());
	}

	public static double doubleZ(Entity entity, float delta) {
		if (entity == null) return 0.0;
		return MathUtil.lerp(delta, entity.prevZ, entity.getZ());
	}

	public static double doubleX(float delta) {
		return doubleX(getPosEntity(), delta);
	}

	public static double doubleZ(float delta) {
		return doubleZ(getPosEntity(), delta);
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
		if (Dimension.isNether(world)) {
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
		MinecraftClient minecraft = MinecraftClient.getInstance();
		return minecraft.getCameraEntity() != null ? minecraft.getCameraEntity() : minecraft.player;
	}
}
