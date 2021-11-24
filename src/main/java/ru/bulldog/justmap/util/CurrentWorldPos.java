package ru.bulldog.justmap.util;

import java.util.function.Supplier;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentStateManager;
import net.minecraft.world.World;

import ru.bulldog.justmap.util.math.MathUtil;

public class CurrentWorldPos {
	private static final BlockPos.Mutable currentPos = new BlockPos.Mutable();
	private static ClientWorld clientWorld = null;
	private static ServerWorld serverWorld = null;
	private static Supplier<PersistentStateManager> persistentSupplier = null;
	private static int coordX;
	private static int coordY;
	private static int coordZ;

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

	public static void updatePositionOnTick() {
		if (getPosEntity() == null) {
			coordX = 0;
			coordY = 0;
			coordZ = 0;
		} else {
			coordX = MathUtil.floor(getPosEntity().getX());
			coordZ = MathUtil.floor(getPosEntity().getZ());
			coordY = (int) getPosEntity().getY();
		}
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

	public static Supplier<PersistentStateManager> getPersistentSupplier() {
		return persistentSupplier;
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
