package ru.bulldog.justmap.advancedinfo;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import ru.bulldog.justmap.util.ScreenPosition;

public class AdvancedInfo {

	private static AdvancedInfo INSTANCE;
	
	public static AdvancedInfo getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AdvancedInfo();
		}
		
		return INSTANCE;
	}
	
	private MinecraftClient minecraft = MinecraftClient.getInstance();
	private Map<ScreenPosition, TextManager> managers;
	private TextManager mapTextManager;
	
	private AdvancedInfo() {
		this.managers = new HashMap<>();		
		this.mapTextManager = new TextManager();
	}
	
	public TextManager getMapTextManager() {
		return this.mapTextManager;
	}
}
