package ru.bulldog.justmap.advancedinfo;

import ru.bulldog.justmap.client.config.ClientParams;

public class FpsInfo extends InfoText {

	public FpsInfo() {
		super("FPS: 00 fps");
	}

	@Override
	public void update() {
		this.setVisible(ClientParams.showFPS);
		if (visible && minecraft.fpsDebugString.indexOf("fps") > 0) {
			this.setText("FPS: " + minecraft.fpsDebugString.substring(0, minecraft.fpsDebugString.indexOf("fps") + 3));
		}
	}
}
