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
		this.addButton(new ButtonWidget(width / 2 - 85, height - 60, 80, 20, LangUtil.getString("gui", "save"), this::onPressSave));
		this.addButton(new ButtonWidget(width / 2 + 5, height - 60, 80, 20, LangUtil.getString("gui", "reset"), this::onPressReset));
		this.mapHolder = this.addChild(new MapWidget(this, JustMapClient.MAP));
	}
	
	private <T extends Element> T addChild(T element) {
		this.children.add(element);
		return element;
	}

	private void onPressReset(ButtonWidget button) {
		this.mapHolder.resetPosition();
	}
	
	private void onPressSave(ButtonWidget button) {
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
