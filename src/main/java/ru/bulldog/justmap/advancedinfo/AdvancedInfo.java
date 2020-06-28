package ru.bulldog.justmap.advancedinfo;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import ru.bulldog.justmap.advancedinfo.TextManager.TextPosition;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ScreenPosition;

public class AdvancedInfo {

	private static AdvancedInfo INSTANCE;
	
	public static AdvancedInfo getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AdvancedInfo();
		}
		
		return INSTANCE;
	}
	
	private boolean initialized = false;
	
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
	
	public TextManager getTextManager(ScreenPosition position) {
		if (managers.containsKey(position)) {
			return this.managers.get(position);
		}
		
		int offset = ClientParams.positionOffset;
		int screenW = minecraft.getWindow().getScaledWidth();
		int screenH = minecraft.getWindow().getScaledHeight();
		int lineWidth = 64;
		
		TextManager textManager = new TextManager();
		textManager.setLineWidth(lineWidth);
		textManager.setSpacing(12);
		switch(position) {
			case TOP_LEFT:
				textManager.setPosition(offset, offset);
				break;
			case TOP_CENTER:
				textManager.setDirection(TextPosition.UNDER)
						   .setPosition(screenW / 2, offset);
				break;
			case TOP_RIGHT:
				textManager.setDirection(TextPosition.LEFT)
						   .setPosition(screenW, offset);
				break;
			case MIDDLE_LEFT:
				textManager.setPosition(offset, screenH / 2);
				break;
			case MIDDLE_RIGHT:
				textManager.setDirection(TextPosition.LEFT)
						   .setPosition(screenW, screenH / 2);
				break;
			case BOTTOM_LEFT:
				textManager.setDirection(TextPosition.ABOVE_RIGHT)
						   .setPosition(offset, screenH);
				break;	
			case BOTTOM_RIGHT:
				textManager.setDirection(TextPosition.ABOVE_LEFT)
						   .setPosition(screenW, screenH);
				break;
		}
		this.managers.put(position, textManager);
		
		return textManager;
	}
	
	private void initInfo() {
		TextManager textManager = this.getTextManager(ScreenPosition.TOP_LEFT);
		textManager.setSpacing(12);
		
		textManager.add(new BiomeInfo());
		textManager.add(new TimeInfo());
		textManager.add(new FpsInfo());
		textManager.add(new LightLevelInfo());
		
		textManager = this.getTextManager(ScreenPosition.MIDDLE_LEFT);
		textManager.setSpacing(16);
		
		textManager.add(new ItemInfo(EquipmentSlot.MAINHAND));
		textManager.add(new ItemInfo(EquipmentSlot.OFFHAND));
		textManager.add(new ItemInfo(EquipmentSlot.HEAD));
		textManager.add(new ItemInfo(EquipmentSlot.CHEST));
		textManager.add(new ItemInfo(EquipmentSlot.LEGS));
		textManager.add(new ItemInfo(EquipmentSlot.FEET));
		
		this.initialized = true;
	}
	
	public void updateInfo() {
		if (minecraft.currentScreen != null &&
		  !(minecraft.currentScreen instanceof ChatScreen)) return;
		
		if (!initialized) this.initInfo();
		int screenH = minecraft.getWindow().getScaledHeight();
		this.managers.forEach((position, manager) -> {
			switch(position) {
				case MIDDLE_LEFT:
					manager.setPosition(manager.getX(), 
							screenH / 2 - ((manager.size() / 2) * manager.getSpacing()));
					break;
				case MIDDLE_RIGHT:
					break;
				case BOTTOM_LEFT:
					break;	
				case BOTTOM_RIGHT:
					break;
				default:
					break;
			}
			
			manager.update();
		});
	}
	
	public void draw(MatrixStack matrixStack) {
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.managers.forEach((position, manager) -> manager.draw(matrixStack));
	}
}
