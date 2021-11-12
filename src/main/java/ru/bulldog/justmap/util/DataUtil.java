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
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.server.JustMapServer;
import ru.bulldog.justmap.util.math.MathUtil;

public class DataUtil {
	private static final BlockPos.Mutable currentPos = new BlockPos.Mutable();
	private static final DynamicRegistryManager registryManager = DynamicRegistryManager.create();
	private static ClientWorld clientWorld = null;
	private static ServerWorld serverWorld = null;
	private static Supplier<PersistentStateManager> persistentSupplier = null;
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
			persistentSupplier = () -> server.getOverworld().getPersistentStateManager();
		} else {
			serverWorld = null;
			persistentSupplier = null;
		}
	}

	public static void updateOnTick() {
		coordX = 0;
		coordY = 0;
		coordZ = 0;

		Entity posEntity = getPosEntity();
		if (posEntity == null) return;

		coordX = MathUtil.floor(posEntity.getX());
		coordZ = MathUtil.floor(posEntity.getZ());
		coordY = (int) posEntity.getY();
	}

	public static World getWorld() {
		return serverWorld != null ? serverWorld : clientWorld;
	}

	public static Identifier getBiomeId(World world, Biome biome) {
		Identifier biomeId = world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
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
		return MinecraftClient.getInstance().options;
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

	private static Entity getPosEntity() {
		MinecraftClient minecraft = MinecraftClient.getInstance();
		return minecraft.getCameraEntity() != null ? minecraft.getCameraEntity() : minecraft.player;
	}
}
