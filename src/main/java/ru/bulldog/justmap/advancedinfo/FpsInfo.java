package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.MinecraftClient;

import ru.bulldog.justmap.client.config.ClientSettings;

public class FpsInfo extends InfoText {

	public FpsInfo() {
		super("FPS: 00 fps");
	}

	@Override
	public void update() {
		this.setVisible(ClientSettings.showFPS);
		MinecraftClient minecraft = MinecraftClient.getInstance();
		if (visible && minecraft.fpsDebugString.indexOf("fps") > 0) {
			this.setText("FPS: " + minecraft.fpsDebugString.substring(0, minecraft.fpsDebugString.indexOf("fps") + 3));
		}
	}
}
