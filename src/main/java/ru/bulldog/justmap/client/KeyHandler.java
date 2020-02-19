package ru.bulldog.justmap.client;

import net.fabricmc.fabric.api.client.keybinding.FabricKeyBinding;
import net.fabricmc.fabric.api.client.keybinding.KeyBindingRegistry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.minimap.waypoint.WaypointsList;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.glfw.GLFW;

public enum KeyHandler {
	INSTANCE;
	
	private List<KeyParser> parsers = new ArrayList<>();
	
	KeyHandler() {}
	
	public void register(KeyParser parser) {
		KeyBindingRegistry.INSTANCE.register(parser.keyBinding);
		parsers.add(parser);
	}
	
	public void initKeyBindings() {
		INSTANCE.register(new KeyParser(createKeyBinding("create_waypoint", GLFW.GLFW_KEY_B)) {
			@Override
			public void onKeyUp() {
				JustMap.MINIMAP.createWaypoint();
			}

			@Override
			public boolean isListening() {
				return MC.player != null && MC.currentScreen == null;
			}
		});
		
		INSTANCE.register(new KeyParser(createKeyBinding("toggle_map_visible", GLFW.GLFW_KEY_H)) {
			@Override
			public void onKeyUp() {
				JustMap.CONFIG.setBoolean("map_visible", !JustMap.CONFIG.getBoolean("map_visible"));
				JustMap.CONFIG.saveChanges();
			}
		});
		
		INSTANCE.register(new KeyParser(createKeyBinding("toggle_show_caves", GLFW.GLFW_KEY_K)) {
			@Override
			public void onKeyUp() {
				JustMap.CONFIG.setBoolean("show_caves", !JustMap.CONFIG.getBoolean("show_caves"));
				JustMap.CONFIG.saveChanges();
			}
	
			@Override
			public boolean isListening() {
				return JustMap.MINIMAP.isMapVisible();
			}
		});
		
		INSTANCE.register(new KeyParser(createKeyBinding("toggle_show_entities", GLFW.GLFW_KEY_Y)) {
			@Override
			public void onKeyUp() {
				JustMap.CONFIG.setBoolean("show_entities", !JustMap.CONFIG.getBoolean("show_entities"));
				JustMap.CONFIG.saveChanges();
			}
	
			@Override
			public boolean isListening() {
				return JustMap.MINIMAP.isMapVisible();
			}
		});
		
		INSTANCE.register(new KeyParser(createKeyBinding("waypoints_list", GLFW.GLFW_KEY_U)) {
			@Override
			public void onKeyUp() {
				MinecraftClient.getInstance().openScreen(new WaypointsList(null));
			}
	
			@Override
			public boolean isListening() {
				return MC.player != null && MC.currentScreen == null;
			}
		});
		
		INSTANCE.register(new KeyParser(createKeyBinding("reduce_scale", GLFW.GLFW_KEY_LEFT_BRACKET)) {
			@Override
			public void onKeyUp() {
				JustMap.CONFIG.setRanged("map_scale", JustMap.CONFIG.getFloat("map_scale") - 0.25F);
				JustMap.CONFIG.saveChanges();
			}
	
			@Override
			public boolean isListening() {
				return JustMap.MINIMAP.isMapVisible();
			}
		});
		
		INSTANCE.register(new KeyParser(createKeyBinding("increase_scale", GLFW.GLFW_KEY_RIGHT_BRACKET)) {
			@Override
			public void onKeyUp() {
				JustMap.CONFIG.setRanged("map_scale", JustMap.CONFIG.getFloat("map_scale") + 0.25F);
				JustMap.CONFIG.saveChanges();
			}
	
			@Override
			public boolean isListening() {
				return JustMap.MINIMAP.isMapVisible();
			}
		});
	}
	
	public void update() {
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
	
	private FabricKeyBinding createKeyBinding(String name, int key) {
		return FabricKeyBinding.Builder.create(new Identifier(JustMap.MODID, name), InputUtil.Type.KEYSYM, key, JustMap.MODID).build();
	}
}