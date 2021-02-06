package ru.bulldog.justmap.util;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.screen.Worldmap;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.server.JustMapServer;
import ru.bulldog.justmap.util.math.MathUtil;

public class DataUtil {
	private static MutableBlockPos currentPos = new MutableBlockPos();
	private static DynamicRegistryManager registryManager = DynamicRegistryManager.create();
	private static ClientLevel clientWorld = null;
	private static ServerLevel serverWorld = null;
	private static Supplier<PersistentStateManager> persistentSupplier = null;
	private static Layer currentLayer = Layer.SURFACE;
	private static int currentLevel = 0;
	private static int coordX = 0;
	private static int coordY = 0;
	private static int coordZ = 0;
	
	@Environment(EnvType.CLIENT)
	public static void updateWorld(ClientLevel world) {
		clientWorld = world;
		Minecraft minecraft = Minecraft.getInstance();
		if (isSingleplayer()) {
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
		return !isSingleplayer();
	}
	public static boolean isSingleplayer() {
		return Minecraft.getInstance().hasSingleplayerServer();
	}
	
	public static IMap getMap() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.currentScreen instanceof Worldmap ? (Worldmap) minecraft.currentScreen : JustMapClient.getMap();
	}
	
	@Environment(EnvType.SERVER)
	public static Level getServerWorld(ResourceKey<Level> worldKey) {
		return JustMapServer.getServer().getWorld(worldKey);
	}
	
	public static Level getWorld() {
		return serverWorld != null ? serverWorld : clientWorld;
	}
	
	public static WritableRegistry<Biome> getBiomeRegistry(Level world) {
		return world.registryAccess().registry(Registry.BIOME_REGISTRY).get();
	}
	
	public static ResourceLocation getBiomeId(Level world, Biome biome) {
		ResourceLocation biomeId = getBiomeRegistry(world).getKey(biome);
		return biomeId != null ? biomeId : BuiltinRegistries.BIOME.getKey(biome);
	}
	
	public static MutableRegistry<Biome> getBiomeRegistry() {
		if (JustMap.getSide() == EnvType.CLIENT) {
			Minecraft minecraft = Minecraft.getInstance();
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
	
	public static ClientLevel getClientWorld() {
		return clientWorld;
	}
	
	public static ServerWorld getServerWorld() {
		return serverWorld;
	}
	
	public static Supplier<PersistentStateManager> getPersistentSupplier() {
		return persistentSupplier;
	}
	
	public static GameOptions getGameOptions() {
		Minecraft minecraft = Minecraft.getInstance();
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

	static boolean isSkyVisible(Level world, BlockPos pos) {
		if (world.canSeeSky(pos)) return true;
		if (world.canSeeSky(pos.above())) return true;
		if (world.canSeeSky(pos.north())) return true;
		if (world.canSeeSky(pos.east())) return true;
		if (world.canSeeSky(pos.south())) return true;
		if (world.canSeeSky(pos.west())) return true;
		
		return false;
	}
	
	public static Layer getLayer() {
		return currentLayer;
	}
	
	public static int getLevel() {
		return currentLevel;
	}
	
	public static Layer getLayer(Level world, BlockPos pos) {
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
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.getCameraEntity() != null ? minecraft.getCameraEntity() : minecraft.player;
	}
}
