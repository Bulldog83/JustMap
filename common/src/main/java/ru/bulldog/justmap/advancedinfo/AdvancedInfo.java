package ru.bulldog.justmap.advancedinfo;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.world.entity.EquipmentSlot;
import ru.bulldog.justmap.client.config.Settings;
import ru.bulldog.justmap.client.config.Settings.ScreenPositionEntry;
import ru.bulldog.justmap.config.PathConfig;
import ru.bulldog.justmap.enums.ScreenPosition;

public class AdvancedInfo {

	private static AdvancedInfo INSTANCE;
	private static final TextManager mapTextManager = new TextManager();
	private static final PathConfig CONFIG = Settings.MAP_INFO;
	
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
	private boolean enabled;
	
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
		this.enabled = CONFIG.getBooleanRoot("enable_info", true);
		if (minecraft == null || !enabled) return;
		if (minecraft.screen != null &&
		  !(minecraft.screen instanceof ChatScreen)) return;
		
		ScreenPosition infoPos = CONFIG.getEnum("info_position", ScreenPosition.TOP_LEFT, ScreenPositionEntry.class);
		ScreenPosition itemsPos = CONFIG.getEnum("items_position", ScreenPosition.MIDDLE_LEFT, ScreenPositionEntry.class);
		if (this.infoPos != infoPos || this.itemsPos != itemsPos) {
			this.infoPos = infoPos;
			this.itemsPos = itemsPos;
			this.initInfo();
		}
		this.managers.forEach((position, manager) -> {
			manager.updatePosition(position);
			manager.update();
		});
	}
	
	public void draw(PoseStack matrixStack) {
		if (!enabled) return;
		if (minecraft.screen != null &&
		  !(minecraft.screen instanceof ChatScreen)) return;
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.managers.forEach((position, manager) -> manager.draw(matrixStack));
	}
}
