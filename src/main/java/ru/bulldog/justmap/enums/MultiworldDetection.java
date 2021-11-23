package ru.bulldog.justmap.enums;

import ru.bulldog.justmap.client.config.ClientSettings;

public enum MultiworldDetection {
	MANUAL,
	MIXED,
	AUTO;

	public static boolean isManual() {
		return ClientSettings.multiworldDetection.equals(MANUAL);
	}

	public static boolean isAuto() {
		return ClientSettings.multiworldDetection.equals(AUTO);
	}

	public static boolean isMixed() {
		return ClientSettings.multiworldDetection.equals(MIXED);
	}
}
