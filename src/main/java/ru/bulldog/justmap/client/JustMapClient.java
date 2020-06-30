package ru.bulldog.justmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.AdvancedInfo;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.map.data.MapCache;
import ru.bulldog.justmap.map.minimap.Minimap;

public class JustMapClient implements ClientModInitializer {
	public final static ClientConfig CONFIG = ClientConfig.get();
	public final static Minimap MAP = new Minimap();
	
	private boolean paused;
	
	@Override
	public void onInitializeClient() {
		KeyHandler.initKeyBindings();

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			AdvancedInfo.getInstance().updateInfo();
			KeyHandler.update();
			MAP.update();

			boolean paused = this.paused;
			boolean online = !client.isIntegratedServerRunning() && client.currentScreen == null;
			this.paused = client.isPaused() || client.overlay != null && client.overlay.pausesGame() ||
					client.currentScreen != null && client.currentScreen.isPauseScreen();
			long time = System.currentTimeMillis();
			if (!paused && this.paused) {
				JustMap.LOGGER.logInfo("Saving chunks data...");
				MapCache.saveData();
			} else if (online && time - MapCache.lastSaved > 60000) {
				MapCache.saveData();
			}
		});
	}
}
