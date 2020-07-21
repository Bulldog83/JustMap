package ru.bulldog.justmap.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import ru.bulldog.justmap.server.config.ServerConfig;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class JustMapServer implements DedicatedServerModInitializer {
	public final static ServerConfig CONFIG = ServerConfig.get();

	@Override
	public void onInitializeServer() {
		ServerLifecycleEvents.SERVER_STOPPED.register(server -> TaskManager.shutdown());
	}
}
