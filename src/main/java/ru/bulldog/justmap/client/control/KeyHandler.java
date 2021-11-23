package ru.bulldog.justmap.client.control;

import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import org.lwjgl.glfw.GLFW;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ConfigFactory;
import ru.bulldog.justmap.client.screen.WaypointsListScreen;
import ru.bulldog.justmap.client.screen.WorldmapScreen;

public final class KeyHandler {
	
	private static List<KeyParser> parsers;
	
	private KeyHandler() {}
	
	public static void initKeyBindings() {
		parsers = new ArrayList<>();
		
		registerKey(new KeyParser(createKeyBinding("create_waypoint", GLFW.GLFW_KEY_B)) {
			@Override
			public void onKeyUp() {
				JustMapClient.getMiniMap().createWaypoint();
			}

			@Override
			public boolean isListening() {
				return MC.player != null && MC.currentScreen == null;
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_map_visible", GLFW.GLFW_KEY_H)) {
			@Override
			public void onKeyUp() {
				JustMapClient.getConfig().setBoolean("map_visible", !JustMapClient.getConfig().getBoolean("map_visible"));
				JustMapClient.getConfig().saveChanges();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_big_map", GLFW.GLFW_KEY_N)) {
			@Override
			public void onKeyUp() {
				JustMapClient.getConfig().setBoolean("show_big_map", !JustMapClient.getConfig().getBoolean("show_big_map"));
				JustMapClient.getConfig().saveChanges();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_show_caves", GLFW.GLFW_KEY_K)) {
			@Override
			public void onKeyUp() {
				JustMapClient.getConfig().setBoolean("show_caves", !JustMapClient.getConfig().getBoolean("show_caves"));
				JustMapClient.getConfig().saveChanges();
			}
	
			@Override
			public boolean isListening() {
				return JustMapClient.getMiniMap().isMapVisible();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_show_entities", GLFW.GLFW_KEY_Y)) {
			@Override
			public void onKeyUp() {
				JustMapClient.getConfig().setBoolean("show_entities", !JustMapClient.getConfig().getBoolean("show_entities"));
				JustMapClient.getConfig().saveChanges();
			}
	
			@Override
			public boolean isListening() {
				return JustMapClient.getMiniMap().isMapVisible();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_show_waypoints", GLFW.GLFW_KEY_P)) {
			@Override
			public void onKeyUp() {
				JustMapClient.getConfig().setBoolean("show_waypoints", !JustMapClient.getConfig().getBoolean("show_waypoints"));
				JustMapClient.getConfig().saveChanges();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("waypoints_list", GLFW.GLFW_KEY_U)) {
			@Override
			public void onKeyUp() {
				MC.setScreen(new WaypointsListScreen(null));
			}
	
			@Override
			public boolean isListening() {
				return MC.player != null && MC.currentScreen == null;
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("show_config", GLFW.GLFW_KEY_J)) {
			@Override
			public void onKeyUp() {
				MC.setScreen(ConfigFactory.getConfigScreen(null));
			}
			
			@Override
			public boolean isListening() {
				return MC.currentScreen == null;
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("show_worldmap", GLFW.GLFW_KEY_M)) {
			@Override
			public void onKeyUp() {
				MC.setScreen(WorldmapScreen.getScreen());
			}
			
			@Override
			public boolean isListening() {
				return MC.player != null && MC.currentScreen == null;
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("reduce_scale", GLFW.GLFW_KEY_LEFT_BRACKET)) {
			@Override
			public void onKeyUp() {
				JustMapClient.getConfig().updateMapScale(0.5F);
			}
	
			@Override
			public boolean isListening() {
				return JustMapClient.getMiniMap().isMapVisible();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("increase_scale", GLFW.GLFW_KEY_RIGHT_BRACKET)) {
			@Override
			public void onKeyUp() {
				JustMapClient.getConfig().updateMapScale(2F);
			}
	
			@Override
			public boolean isListening() {
				return JustMapClient.getMiniMap().isMapVisible();
			}
		});
	}
	
	public static void update() {
		for (KeyParser kp : parsers) {
			if (kp.isListening()) {
				if (kp.keyBinding.wasPressed()) {
					kp.onKeyUp();
				} else if (kp.keyBinding.isPressed()) {
					kp.onKeyDown();
				}
			}
		}
	}
	
	private static void registerKey(KeyParser parser) {
		KeyBindingHelper.registerKeyBinding(parser.keyBinding);
		parsers.add(parser);
	}
	
	private static KeyBinding createKeyBinding(String name, int key) {
		return new KeyBinding(String.format("key.%s.%s", JustMap.MODID, name), key, JustMap.MODID);
	}
}
