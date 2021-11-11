package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.MinecraftClient;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.enums.TextAlignment;

public class TimeInfo extends InfoText {

	private final String title;
	
	public TimeInfo() {
		super("00:00");
		this.title = "Time: ";
	}
	
	public TimeInfo(TextAlignment alignment, String title) {
		super(alignment, "00:00");
		this.title = title;
	}

	@Override
	public void update() {
		this.setVisible(ClientSettings.showTime);
		MinecraftClient minecraft = MinecraftClient.getInstance();
		if (visible && minecraft.world != null) {
			this.setText(title + this.timeString(minecraft.world.getTimeOfDay()));
		}
	}
	
	private String timeString(long time) {
		time = time > 24000 ? time % 24000 : time;
	
		int h = (int) time / 1000 + 6;
		int m = (int) (((time % 1000) / 1000.0F) * 60);
		
		h = h >= 24 ? h - 24 : h;
	
		return String.format("%02d:%02d", h, m);
	}

}
