package ru.bulldog.justmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;

import net.minecraft.client.MinecraftClient;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.map.data.MapCache;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.TaskManager;

public class JustMapClient implements ClientModInitializer {
	public final static ClientConfig CONFIG = ClientConfig.get();
	public final static Minimap MAP = new Minimap();
	
	private boolean paused;
	
	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory(JustMap.MODID);
		
		KeyHandler.INSTANCE.initKeyBindings();		
		ClientTickCallback.EVENT.register((client) -> {
			KeyHandler.INSTANCE.update();
			MAP.update();
			
			boolean paused = this.paused;
			this.paused = MinecraftClient.getInstance().isPaused();
			if (!paused && this.paused) {
				JustMap.LOGGER.logInfo("Saving chunks data...");
				MapCache.saveData();
			}
		});
		
		ServerStopCallback.EVENT.register((server) -> {
			TaskManager.shutdown();
		});
	}
}
