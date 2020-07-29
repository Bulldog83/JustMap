package ru.bulldog.justmap.client;

import com.mojang.realmsclient.RealmsMainScreen;
import com.mojang.realmsclient.gui.screens.RealmsGenericErrorScreen;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.pack.DataPackScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.AdvancedInfo;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.data.DimensionManager;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class JustMapClient implements ClientModInitializer {
	public final static MinecraftClient MINECRAFT = MinecraftClient.getInstance();
	public final static ClientConfig CONFIG = ClientConfig.get();
	public final static Minimap MAP = new Minimap();
	public static boolean canMapping = false;
	
	private boolean isOnTitleScreen = true;
	
	@Override
	public void onInitializeClient() {
		KeyHandler.initKeyBindings();

		ClientChunkEvents.CHUNK_LOAD.register(DimensionManager::onChunkLoad);
		ClientTickEvents.END_CLIENT_TICK.register(minecraft -> {
			boolean isTitle = this.isOnTitleScreen(minecraft.currentScreen);
			if (isTitle && !isOnTitleScreen) {
				JustMapClient.stop();
			}
			this.isOnTitleScreen = isTitle;
			if (isOnTitleScreen || !isMappingAllowed()) return;

			DataUtil.update();
			KeyHandler.update();
			JustMapClient.MAP.update();
			AdvancedInfo.getInstance().updateInfo();
			DimensionManager.update();
			DimensionManager.memoryControl();
			ChunkUpdateListener.proceed();
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			JustMapClient.stop();
			TaskManager.shutdown();
		});
	}
	
	private static void stop() {
		ChunkUpdateListener.stop();
		JustMap.WORKER.execute("Clearing map cache...", DimensionManager::close);
	}
	
	public static boolean isMappingAllowed() {
		MinecraftClient minecraft = DataUtil.getMinecraft();
		return canMapping && minecraft.world != null &&
			   (minecraft.getCameraEntity() != null || minecraft.player != null);
	}
	
	private boolean isOnTitleScreen(Screen currentScreen) {
		return currentScreen instanceof TitleScreen ||
			   currentScreen instanceof SelectWorldScreen ||
		       currentScreen instanceof MultiplayerScreen ||
		       currentScreen instanceof BackupPromptScreen ||
		       currentScreen instanceof CreateWorldScreen ||
		       currentScreen instanceof DataPackScreen ||
		       currentScreen instanceof EditGameRulesScreen ||
		       currentScreen instanceof EditWorldScreen ||
		       currentScreen instanceof RealmsMainScreen ||
		       currentScreen instanceof RealmsGenericErrorScreen;
	}
}
