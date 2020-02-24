package ru.bulldog.justmap.client;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.minimap.Minimap;

public class JustMapClient implements ClientModInitializer {
	public final static ClientConfig CONFIG = ClientConfig.get();
	public final static Minimap MAP = new Minimap();
	
	@Override
	public void onInitializeClient() {
		KeyBindingRegistry.INSTANCE.addCategory(JustMap.MODID);		
		
		KeyHandler.INSTANCE.initKeyBindings();		
		ClientTickCallback.EVENT.register((client) -> {
			KeyHandler.INSTANCE.update();
			MAP.update();
		});
	}
}
