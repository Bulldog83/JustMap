package ru.bulldog.justmap.advancedinfo;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.entity.EquipmentSlot;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.util.DataUtil;

public class AdvancedInfo {

	private static AdvancedInfo INSTANCE;
	private static TextManager mapTextManager = new TextManager();
	
	public static AdvancedInfo getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AdvancedInfo();
		}
		
		return INSTANCE;
	}
	
	public static TextManager getMapTextManager() {
		return mapTextManager;
	}
	
	private MinecraftClient minecraft = DataUtil.getMinecraft();
	private Map<ScreenPosition, TextManager> managers;
	private ScreenPosition infoPos;
	private ScreenPosition itemsPos;
	
	private AdvancedInfo() {
		this.managers = new HashMap<>();
	}
	
	public TextManager getTextManager(ScreenPosition position) {
		if (managers.containsKey(position)) {
			return this.managers.get(position);
		}
		int lineWidth = 128;
		TextManager textManager = new TextManager();
		textManager.setLineWidth(lineWidth);
		this.managers.put(position, textManager);
		
		return textManager;
	}
	
	private void initInfo() {
		this.managers.forEach((position, manager) -> manager.clear());
		
		this.infoPos = ClientParams.infoPosition;
		this.itemsPos = ClientParams.itemsPosition;
		
		TextManager textManager = this.getTextManager(infoPos);
		textManager.setSpacing(12);
		
		textManager.add(new BiomeInfo());
		textManager.add(new TimeInfo());
		textManager.add(new FpsInfo());
		textManager.add(new LightLevelInfo());
		
		textManager = this.getTextManager(itemsPos);
		textManager.setSpacing(16);
		
		textManager.add(new ItemInfo(EquipmentSlot.MAINHAND));
		textManager.add(new ItemInfo(EquipmentSlot.OFFHAND));
		textManager.add(new ItemInfo(EquipmentSlot.HEAD));
		textManager.add(new ItemInfo(EquipmentSlot.CHEST));
		textManager.add(new ItemInfo(EquipmentSlot.LEGS));
		textManager.add(new ItemInfo(EquipmentSlot.FEET));
	}
	
	public void updateInfo() {
		if (!ClientParams.advancedInfo) return;
		if (minecraft.currentScreen != null &&
		  !(minecraft.currentScreen instanceof ChatScreen)) return;
		
		if (ClientParams.infoPosition != infoPos || ClientParams.itemsPosition != itemsPos) {
			this.initInfo();
		}
		this.managers.forEach((position, manager) -> {
			manager.updatePosition(position);
			manager.update();
		});
	}
	
	public void draw() {
		if (!ClientParams.advancedInfo) return;
		if (minecraft.currentScreen != null &&
		  !(minecraft.currentScreen instanceof ChatScreen)) return;
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.managers.forEach((position, manager) -> manager.draw());
	}
}
