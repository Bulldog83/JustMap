package ru.bulldog.justmap.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.server.MinecraftServer;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.data.MapDataProvider;
import ru.bulldog.justmap.network.ServerNetworkHandler;
import ru.bulldog.justmap.server.config.ServerConfig;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class JustMapServer implements DedicatedServerModInitializer {
	public final static ServerConfig CONFIG = ServerConfig.get();
	private static ServerNetworkHandler networkHandler;
	private static MinecraftServer dedicatedServer;

	@Override
	public void onInitializeServer() {
		JustMap.setSide(EnvType.SERVER);
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			dedicatedServer = server;
			networkHandler = new ServerNetworkHandler(server);
			networkHandler.registerPacketsListeners();
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			TaskManager.shutdown();
			dedicatedServer = null;
			networkHandler = null;
		});
		ServerTickEvents.END_SERVER_TICK.register(server -> {
			MapDataProvider.getManager().onTick(true);
		});
	}
	
	public static MinecraftServer getServer() {
		return dedicatedServer;
	}
	
	public static ServerNetworkHandler getNetworkHandler() {
		return networkHandler;
	}
}
