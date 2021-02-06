package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.Minecraft;

import ru.bulldog.justmap.client.config.ClientSettings;

public class FpsInfo extends InfoText {

	public FpsInfo() {
		super("FPS: 00 fps");
	}

	@Override
	public void update() {
		this.setVisible(ClientSettings.showFPS);
		Minecraft minecraft = Minecraft.getInstance();
		if (visible && minecraft.fpsString.indexOf("fps") > 0) {
			this.setText("FPS: " + minecraft.fpsString.substring(0, minecraft.fpsString.indexOf("fps") + 3));
		}
	}
}
