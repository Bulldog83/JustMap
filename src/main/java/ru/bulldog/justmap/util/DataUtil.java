package ru.bulldog.justmap.util;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Registry;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.WritableRegistry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.DimensionDataStorage;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.screen.Worldmap;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.server.JustMapServer;
import ru.bulldog.justmap.util.math.MathUtil;

@SuppressWarnings("ConstantConditions")
public class DataUtil {
	private static final BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
	private static final RegistryAccess registryManager = RegistryAccess.builtin();
	private static ClientLevel clientWorld = null;
	private static ServerLevel serverWorld = null;
	private static Supplier<DimensionDataStorage> persistentSupplier = null;
	private static Layer currentLayer = Layer.SURFACE;
	private static int currentLevel = 0;
	private static int coordX = 0;
	private static int coordY = 0;
	private static int coordZ = 0;
	
	@Environment(EnvType.CLIENT)
	public static void updateWorld(ClientLevel world) {
		clientWorld = world;
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.hasSingleplayerServer()) {
			MinecraftServer server = minecraft.getSingleplayerServer();
			serverWorld = minecraft.getSingleplayerServer().getLevel(world.dimension());
			persistentSupplier = () -> server.overworld().getDataStorage();
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
		return !Minecraft.getInstance().hasSingleplayerServer();
	}
	
	public static IMap getMap() {
		Minecraft minecraft = Minecraft.getInstance();
		return minecraft.screen instanceof Worldmap ? (Worldmap) minecraft.screen : JustMapClient.getMap();
	}
	
	@Environment(EnvType.SERVER)
	public static Level getServerWorld(ResourceKey<Level> worldKey) {
		return JustMapServer.getServer().getLevel(worldKey);
	}
	
	public static Level getWorld() {
		return serverWorld != null ? serverWorld : clientWorld;
	}
	
	public static WritableRegistry<Biome> getBiomeRegistry(Level world) {
		return (WritableRegistry<Biome>) world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
	}
	
	public static ResourceLocation getBiomeId(Level world, Biome biome) {
		ResourceLocation biomeId = getBiomeRegistry(world).getKey(biome);
		return biomeId != null ? biomeId : BuiltinRegistries.BIOME.getKey(biome);
	}
	
	public static WritableRegistry<Biome> getBiomeRegistry() {
		if (JustMap.getSide() == EnvType.CLIENT) {
			Minecraft minecraft = Minecraft.getInstance();
			ClientPacketListener networkHandler = minecraft.getConnection();
			if (networkHandler != null) {
				return (WritableRegistry<Biome>) minecraft.getConnection().registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
			}
			return (WritableRegistry<Biome>) registryManager.registryOrThrow(Registry.BIOME_REGISTRY);
		}
		MinecraftServer server = JustMapServer.getServer();
		if (server != null) {
			return (WritableRegistry<Biome>) server.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
		}
		return (WritableRegistry<Biome>) registryManager.registryOrThrow(Registry.BIOME_REGISTRY);
	}
	
	public static ClientLevel getClientWorld() {
		return clientWorld;
	}
	
	public static ServerLevel getServerWorld() {
		return serverWorld;
	}
	
	public static Supplier<DimensionDataStorage> getPersistentSupplier() {
		return persistentSupplier;
	}
	
	public static Options getGameOptions() {
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
		return MathUtil.lerp(delta, entity.xo, entity.getX());
	}

	public static double doubleZ(Entity entity, float delta) {
		if (entity == null) return 0.0;
		return MathUtil.lerp(delta, entity.zo, entity.getZ());
	}
	
	public static double doubleX(float delta) {
		return doubleX(getPosEntity(), delta);
	}
	
	public static double doubleZ(float delta) {
		return doubleZ(getPosEntity(), delta);
	}

	static boolean hasSkyLight(Level world, BlockPos pos) {
		if (world.getBrightness(LightLayer.SKY, pos) > 0) return true;
		if (world.getBrightness(LightLayer.SKY, pos.above()) > 0) return true;
		if (world.getBrightness(LightLayer.SKY, pos.north()) > 0) return true;
		if (world.getBrightness(LightLayer.SKY, pos.east()) > 0) return true;
		if (world.getBrightness(LightLayer.SKY, pos.south()) > 0) return true;
		if (world.getBrightness(LightLayer.SKY, pos.west()) > 0) return true;
		
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
