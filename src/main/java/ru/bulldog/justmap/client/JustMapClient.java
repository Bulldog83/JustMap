package ru.bulldog.justmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.MinecraftClient;
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
import ru.bulldog.justmap.map.data.DimensionData;
import ru.bulldog.justmap.map.data.DimensionManager;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class JustMapClient implements ClientModInitializer {
	public final static MinecraftClient MINECRAFT = MinecraftClient.getInstance();
	public final static ClientConfig CONFIG = ClientConfig.get();
	public final static Minimap MAP = new Minimap();
	
	private boolean isOnTitleScreen = true;
	
	@Override
	public void onInitializeClient() {
		KeyHandler.initKeyBindings();

		ClientChunkEvents.CHUNK_LOAD.register((world, chunk) -> {
			DimensionData data = DimensionManager.getData(MAP);
			if (data != null) {
				data.addLoadedChunk(world, chunk);
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			boolean isTitle = this.isOnTitleScreen(client.currentScreen);
			if (isTitle && !isOnTitleScreen) {
				JustMapClient.stop();
			}
			this.isOnTitleScreen = isTitle;
			if (isOnTitleScreen) return;
			
			DimensionManager.memoryControl();
			AdvancedInfo.getInstance().updateInfo();
			KeyHandler.update();
			MAP.update();
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			JustMapClient.stop();
			TaskManager.shutdown();
		});
	}
	
	private static void stop() {
		JustMap.WORKER.execute("Clearing map cache...", DimensionManager::clearData);
	}
	
	private boolean isOnTitleScreen(Screen currentScreen) {
		if (currentScreen == null) return false;
		
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
