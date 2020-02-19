package ru.bulldog.justmap.minimap;

public enum MapPosition {
	TOP_LEFT,
	TOP_CENTER,
	TOP_RIGHT,
	MIDDLE_LEFT,
	MIDDLE_RIGHT,
	BOTTOM_LEFT,
	BOTTOM_RIGHT;
  
	public static MapPosition get(int i) {
		return values()[i];
	}
}