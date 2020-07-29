package ru.bulldog.justmap.enums;

import ru.bulldog.justmap.client.config.ClientParams;

public enum MultiworldDetection {
	MANUAL,
	AUTO;
	
	public static boolean isManual() {
		return ClientParams.multiworldDetection.equals(MANUAL);
	}
	
	public static boolean isAuto() {
		return ClientParams.multiworldDetection.equals(AUTO);
	}
}
