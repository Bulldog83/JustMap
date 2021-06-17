package ru.bulldog.justmap.advancedinfo;

import java.util.HashMap;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.EquipmentSlot;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.enums.ScreenPosition;

public class AdvancedInfo {

	private static final TextManager mapTextManager = new TextManager();
	private static AdvancedInfo INSTANCE;
	
	public static AdvancedInfo getInstance() {
		if (INSTANCE == null) {
			INSTANCE = new AdvancedInfo();
		}
		
		return INSTANCE;
	}
	
	public static TextManager getMapTextManager() {
		return mapTextManager;
	}
	
	private final Minecraft minecraft = Minecraft.getInstance();
	private final Map<ScreenPosition, TextManager> managers;
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
		
		this.infoPos = ClientSettings.infoPosition;
		this.itemsPos = ClientSettings.itemsPosition;
		
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
		if (minecraft == null || !ClientSettings.advancedInfo) return;
		if (minecraft.screen != null &&
		  !(minecraft.screen instanceof ChatScreen)) return;
		
		if (ClientSettings.infoPosition != infoPos || ClientSettings.itemsPosition != itemsPos) {
			this.initInfo();
		}
		this.managers.forEach((position, manager) -> {
			manager.updatePosition(position);
			manager.update();
		});
	}
	
	public void draw(PoseStack matrixStack) {
		if (!ClientSettings.advancedInfo) return;
		if (minecraft.screen != null &&
		  !(minecraft.screen instanceof ChatScreen)) return;
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		this.managers.forEach((position, manager) -> manager.draw(matrixStack));
	}
}
