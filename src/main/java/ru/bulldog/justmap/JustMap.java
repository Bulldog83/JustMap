package ru.bulldog.justmap;

import ru.bulldog.justmap.client.KeyHandler;
import ru.bulldog.justmap.config.Config;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.util.Logger;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;

public class JustMap implements ClientModInitializer {
	public static final String MODID = "justmap";	
	public static final Logger LOGGER = Logger.get();
	public final static Config CONFIG = Config.get();
	public final static Minimap MAP = new Minimap();
	
	@Override
	public void onInitializeClient() {	
		KeyBindingRegistry.INSTANCE.addCategory(MODID);		
		
		KeyHandler.INSTANCE.initKeyBindings();		
		ClientTickCallback.EVENT.register((client) -> {
			KeyHandler.INSTANCE.update();
			MAP.update();
		});
	}
}
