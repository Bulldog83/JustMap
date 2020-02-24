package ru.bulldog.justmap.server;

import net.fabricmc.api.DedicatedServerModInitializer;
import ru.bulldog.justmap.server.config.ServerConfig;

public class JustMapServer implements DedicatedServerModInitializer {
	public final static ServerConfig CONFIG = ServerConfig.get();

	@Override
	public void onInitializeServer() {}
}
