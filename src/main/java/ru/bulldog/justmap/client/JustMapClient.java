package ru.bulldog.justmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.BackupPromptScreen;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.gui.screen.world.CreateWorldScreen;
import net.minecraft.client.gui.screen.world.EditGameRulesScreen;
import net.minecraft.client.gui.screen.world.EditWorldScreen;
import net.minecraft.client.gui.screen.world.SelectWorldScreen;
import net.minecraft.client.realms.gui.screen.RealmsGenericErrorScreen;
import net.minecraft.client.realms.gui.screen.RealmsMainScreen;
import net.minecraft.text.TranslatableText;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.AdvancedInfo;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.client.control.KeyHandler;
import ru.bulldog.justmap.map.data.MapDataProvider;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.network.ClientNetworkHandler;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class JustMapClient implements ClientModInitializer {
	private static ClientConfig config = ClientConfig.get();
	private static Minimap minimap = new Minimap();
	private static MinecraftClient minecraft;
	private static ClientNetworkHandler networkHandler;
	private static boolean canMapping = false;
	private static boolean isOnTitleScreen = true;

	@Override
	public void onInitializeClient() {
		JustMap.setSide(EnvType.CLIENT);
		KeyHandler.initKeyBindings();
		ClientLifecycleEvents.CLIENT_STARTED.register((client) -> {
			minecraft = client;
			networkHandler = new ClientNetworkHandler();
			networkHandler.registerPacketsListeners();
			config = ClientConfig.get();
			minimap = new Minimap();
			Colors.INSTANCE.loadData();
		});
		ClientChunkEvents.CHUNK_LOAD.register(MapDataProvider.getManager()::onChunkLoad);
		HudRenderCallback.EVENT.register((matrices, delta) -> {
			if (!minecraft.options.debugEnabled) {
				JustMapClient.minimap.getRenderer().renderMap(matrices);
				AdvancedInfo.getInstance().draw(matrices);
			}
		});
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (minecraft == null) return;
			boolean isTitle = this.isOnTitleScreen(client.currentScreen);
			if (isTitle && !isOnTitleScreen) {
				JustMapClient.stop();
			}
			isOnTitleScreen = isTitle;

			AdvancedInfo.getInstance().updateInfo();
			KeyHandler.update();

			if (!canMapping()) return;

			DataUtil.update();
			JustMapClient.minimap.update();
			MapDataProvider.getManager().onTick(false);
		});
		ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
			JustMapClient.stop();
			TaskManager.shutdown();
			minecraft = null;
		});
	}

	private static void stop() {
		MapDataProvider.getManager().onWorldStop();
		Colors.INSTANCE.saveData();
	}

	public static void startMapping() {
		canMapping = true;
	}

	public static void stopMapping() {
		canMapping = false;
	}

	public static boolean canMapping() {
		return !isOnTitleScreen && canMapping && minecraft.world != null &&
				(minecraft.getCameraEntity() != null || minecraft.player != null);
	}

	public static Minimap getMiniMap() {
		return minimap;
	}

	public static ClientConfig getConfig() {
		return config;
	}

	public static ClientNetworkHandler getNetworkHandler() {
		return networkHandler;
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
