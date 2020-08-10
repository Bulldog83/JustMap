package ru.bulldog.justmap.client.screen;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.util.math.MatrixStack;
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
		this.addButton(new ButtonWidget(width / 2 - 30, height - 50, 60, 20, LangUtil.getText("gui", "save"), this::onPressSave));
		this.mapHolder = this.addChild(new MapWidget(this, JustMapClient.MAP));
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
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		fill(matrices, 0, 0, width, height, 0x88000000);
		this.mapHolder.render(matrices, mouseX, mouseY, delta);
		super.render(matrices, mouseX, mouseY, delta);
	}

	@Override
	public void onClose() {
		this.client.openScreen(parent);
	}
}
