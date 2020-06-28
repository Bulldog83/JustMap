package ru.bulldog.justmap.advancedinfo;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
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
	
	public void draw(MatrixStack matrixStack) {
		this.managers.forEach((position, manager) -> manager.draw(matrixStack));
	}
}
