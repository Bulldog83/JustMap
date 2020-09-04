package ru.bulldog.justmap.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.MinecraftServer;
import ru.bulldog.justmap.server.config.ServerConfig;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class JustMapServer implements DedicatedServerModInitializer {
	public final static ServerConfig CONFIG = ServerConfig.get();
	private static MinecraftServer DEDICATED_SERVER;

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STARTING.register(server -> {
			DEDICATED_SERVER = server;
		});
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> {
			TaskManager.shutdown();
			DEDICATED_SERVER = null;
		});
	}
	
	public static MinecraftServer getServer() {
		return DEDICATED_SERVER;
	}
}
