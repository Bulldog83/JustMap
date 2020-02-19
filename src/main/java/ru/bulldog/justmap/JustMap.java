package ru.bulldog.justmap;

import ru.bulldog.justmap.client.KeyHandler;
import ru.bulldog.justmap.config.Config;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.util.Logger;

import java.io.File;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.fabricmc.fabric.api.event.client.ClientTickCallback;
import net.minecraft.client.MinecraftClient;

public class JustMap implements ClientModInitializer {
	public static final String MODID = "justmap";	
	public static final Logger LOGGER = Logger.getLogger();
	public final static Config CONFIG = Config.get();
	public final static Minimap MINIMAP = new Minimap();
	public final static File MAP_DIR = new File(MinecraftClient.getInstance().runDirectory, "justmap/");
	
	@Override
	public void onInitializeClient() {	
		KeyBindingRegistry.INSTANCE.addCategory(MODID);		
		
		KeyHandler.INSTANCE.initKeyBindings();		
		ClientTickCallback.EVENT.register((client) -> {
			KeyHandler.INSTANCE.update();
			MINIMAP.update();
		});
	}
}
