package ru.bulldog.justmap.client.screen;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.client.widget.MapWidget;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.util.LangUtil;

public class MapPositionScreen extends Screen {

	private final static Component TITLE = LangUtil.getText("gui", "screen.map_position");
	private final static ClientConfig config = JustMapClient.getConfig();
	
	private final Screen parent;
	private MapWidget mapHolder;
	
	public MapPositionScreen(Screen parent) {
		super(TITLE);
		this.parent = parent;
	}
	
	@Override
	public void init() {
		int posX = width / 2;
		int posY = height - 60;
		this.addWidget(new Button(posX - 125, posY, 80, 20, LangUtil.getText("gui", "save"), button -> this.onSave()));
		this.addWidget(new Button(posX - 40, posY, 80, 20, LangUtil.getText("gui", "reset"), button -> this.onReset()));
		this.addWidget(new Button(posX + 45, posY, 80, 20, LangUtil.getText("gui", "cancel"), button -> this.onClose()));
		this.mapHolder = addWidget(new MapWidget(this, JustMapClient.getMap()));
	}
	
	private void onReset() {
		this.mapHolder.resetPosition();
	}
	
	private void onSave() {
		EnumEntry<ScreenPosition> drawPosConfig = config.getEntry("map_position");
		drawPosConfig.setValue(ScreenPosition.USER_DEFINED);
		config.setInt("map_position_x", mapHolder.getX());
		config.setInt("map_position_y", mapHolder.getY());
		config.saveChanges();
		this.onClose();
	}
	
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		fill(matrices, 0, 0, width, height, 0x66000000);
		this.mapHolder.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	@SuppressWarnings("ConstantConditions")
	public void onClose() {
		minecraft.setScreen(parent);
	}
}
