package ru.bulldog.justmap.client.screen;

import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.client.widget.MapWidget;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.util.LangUtil;

public class MapPositionScreen extends Screen {

	private final static Text TITLE = LangUtil.getText("gui", "screen.map_position");
	private final static ClientConfig config = JustMapClient.CONFIG;
	
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
		this.addButton(new ButtonWidget(posX - 125, posY, 80, 20, LangUtil.getText("gui", "save"), button -> this.onSave()));
		this.addButton(new ButtonWidget(posX - 40, posY, 80, 20, LangUtil.getText("gui", "reset"), button -> this.onReset()));
		this.addButton(new ButtonWidget(posX + 45, posY, 80, 20, LangUtil.getText("gui", "cancel"), button -> this.onClose()));
		this.mapHolder = this.addChild(new MapWidget(this, JustMapClient.MAP));
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
	public void render(int mouseX, int mouseY, float delta) {
		fill(0, 0, width, height, 0x66000000);
		this.mapHolder.render(mouseX, mouseY, delta);
		super.render(mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		this.minecraft.openScreen(parent);
	}
}
