package ru.bulldog.justmap.util;

public enum ScreenPosition {
	TOP_LEFT,
	TOP_CENTER,
	TOP_RIGHT,
	MIDDLE_LEFT,
	MIDDLE_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_RIGHT;
  
	public static ScreenPosition get(int i) {
		return values()[i];
	}
}