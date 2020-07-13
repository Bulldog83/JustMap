package ru.bulldog.justmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.AdvancedInfo;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.map.data.MapCache;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.StorageUtil;

public class JustMapClient implements ClientModInitializer {
	public final static ClientConfig CONFIG = ClientConfig.get();
	public final static Minimap MAP = new Minimap();
	
	private boolean paused;
	private Screen lastScreen;
	
	@Override
	public void onInitializeClient() {
		KeyHandler.initKeyBindings();

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			if (lastScreen != client.currentScreen) {
				if (client.currentScreen instanceof TitleScreen) {
					JustMap.WORKER.execute("Clearing map cache...", MapCache::clearData);
					JustMap.WORKER.execute("Closing storage...", StorageUtil::closeStorage);
				}
				this.lastScreen = client.currentScreen;
			}
			if (lastScreen instanceof TitleScreen) return;
			
			AdvancedInfo.getInstance().updateInfo();
			KeyHandler.update();
			MAP.update();

			boolean paused = this.paused;
			boolean online = !client.isIntegratedServerRunning() && client.currentScreen == null;
			this.paused = client.isPaused() || client.currentScreen != null && client.currentScreen.isPauseScreen();
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
