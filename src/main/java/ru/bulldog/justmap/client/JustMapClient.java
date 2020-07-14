package ru.bulldog.justmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.RealmsMainScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.text.TranslatableText;

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
	private boolean isOnTitleScreen = true;
	
	@Override
	public void onInitializeClient() {
		KeyHandler.initKeyBindings();

		ClientTickEvents.END_CLIENT_TICK.register((client) -> {
			boolean isTitle = this.isOnTitleScreen(client.currentScreen);
			if (isTitle && !isOnTitleScreen) {
				MapCache.saveData();
				JustMap.WORKER.execute("Clearing map cache...", MapCache::clearData);
				JustMap.WORKER.execute("Closing storage...", StorageUtil::closeStorage);
			}
			this.isOnTitleScreen = isTitle;
			if (isOnTitleScreen) return;
			
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
	
	private boolean isOnTitleScreen(Screen currentScreen) {
		boolean isTitleScreen = false;
		if (currentScreen.getTitle() instanceof TranslatableText) {
			TranslatableText title = (TranslatableText) currentScreen.getTitle();
			isTitleScreen = title.getKey().equals("dataPack.title");
		}
		
		return currentScreen instanceof TitleScreen ||
			   currentScreen instanceof SelectWorldScreen ||
		       currentScreen instanceof MultiplayerScreen ||
		       currentScreen instanceof BackupPromptScreen ||
		       currentScreen instanceof CreateWorldScreen ||
		       currentScreen instanceof EditGameRulesScreen ||
		       currentScreen instanceof EditWorldScreen ||
		       currentScreen instanceof RealmsMainScreen ||
		       currentScreen instanceof RealmsGenericErrorScreen ||
		       isTitleScreen;
	}
}
