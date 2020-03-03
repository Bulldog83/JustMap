package ru.bulldog.justmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.fabricmc.fabric.api.event.server.ServerStopCallback;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.minimap.data.MapRegion;
import ru.bulldog.justmap.util.TaskManager;

public class JustMapClient implements ClientModInitializer {
	public final static ClientConfig CONFIG = ClientConfig.get();
	public final static Minimap MAP = new Minimap();
	public final static TaskManager UPDATER = new TaskManager("updating");
	
	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory(JustMap.MODID);
		
		KeyHandler.INSTANCE.initKeyBindings();		
		ClientTickCallback.EVENT.register((client) -> {
			KeyHandler.INSTANCE.update();
			MapRegion.saveImages();
			MAP.update();
		});
		
		ServerStopCallback.EVENT.register((server) -> {
			JustMap.EXECUTOR.stop();
			UPDATER.stop();
		});
	}
}
