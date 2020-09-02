package ru.bulldog.justmap.client;

import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.options.KeyBinding;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ConfigFactory;
import ru.bulldog.justmap.client.screen.WaypointsList;
import ru.bulldog.justmap.client.screen.Worldmap;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

public final class KeyHandler {
	
	private static List<KeyParser> parsers;
	
	private KeyHandler() {}
	
	public static void initKeyBindings() {
		parsers = new ArrayList<>();
		
		registerKey(new KeyParser(createKeyBinding("create_waypoint", GLFW.GLFW_KEY_B)) {
			@Override
			public void onKeyUp() {
				JustMapClient.MAP.createWaypoint();
			}

			@Override
			public boolean isListening() {
				return MC.player != null && MC.currentScreen == null;
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_map_visible", GLFW.GLFW_KEY_H)) {
			@Override
			public void onKeyUp() {
				JustMapClient.CONFIG.setBoolean("map_visible", !JustMapClient.CONFIG.getBoolean("map_visible"));
				JustMapClient.CONFIG.saveChanges();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_big_map", GLFW.GLFW_KEY_N)) {
			@Override
			public void onKeyUp() {
				JustMapClient.CONFIG.setBoolean("show_big_map", !JustMapClient.CONFIG.getBoolean("show_big_map"));
				JustMapClient.CONFIG.saveChanges();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_show_caves", GLFW.GLFW_KEY_K)) {
			@Override
			public void onKeyUp() {
				JustMapClient.CONFIG.setBoolean("show_caves", !JustMapClient.CONFIG.getBoolean("show_caves"));
				JustMapClient.CONFIG.saveChanges();
			}
	
			@Override
			public boolean isListening() {
				return JustMapClient.MAP.isMapVisible();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_show_entities", GLFW.GLFW_KEY_Y)) {
			@Override
			public void onKeyUp() {
				JustMapClient.CONFIG.setBoolean("show_entities", !JustMapClient.CONFIG.getBoolean("show_entities"));
				JustMapClient.CONFIG.saveChanges();
			}
	
			@Override
			public boolean isListening() {
				return JustMapClient.MAP.isMapVisible();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("toggle_show_waypoints", GLFW.GLFW_KEY_P)) {
			@Override
			public void onKeyUp() {
				JustMapClient.CONFIG.setBoolean("show_waypoints", !JustMapClient.CONFIG.getBoolean("show_waypoints"));
				JustMapClient.CONFIG.saveChanges();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("waypoints_list", GLFW.GLFW_KEY_U)) {
			@Override
			public void onKeyUp() {
				MC.openScreen(new WaypointsList(null));
			}
	
			@Override
			public boolean isListening() {
				return MC.player != null && MC.currentScreen == null;
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("show_config", GLFW.GLFW_KEY_J)) {
			@Override
			public void onKeyUp() {
				MC.openScreen(ConfigFactory.getConfigScreen(null));
			}
			
			@Override
			public boolean isListening() {
				return MC.currentScreen == null;
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("show_worldmap", GLFW.GLFW_KEY_M)) {
			@Override
			public void onKeyUp() {
				MC.openScreen(Worldmap.getScreen());
			}
			
			@Override
			public boolean isListening() {
				return MC.player != null && MC.currentScreen == null;
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("reduce_scale", GLFW.GLFW_KEY_LEFT_BRACKET)) {
			@Override
			public void onKeyUp() {
				JustMapClient.CONFIG.updateMapScale(-1);
			}
	
			@Override
			public boolean isListening() {
				return JustMapClient.MAP.isMapVisible();
			}
		});
		
		registerKey(new KeyParser(createKeyBinding("increase_scale", GLFW.GLFW_KEY_RIGHT_BRACKET)) {
			@Override
			public void onKeyUp() {
				JustMapClient.CONFIG.updateMapScale(1);
			}
	
			@Override
			public boolean isListening() {
				return JustMapClient.MAP.isMapVisible();
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