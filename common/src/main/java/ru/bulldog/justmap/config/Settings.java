package ru.bulldog.justmap.config;

import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import old_files.justmap.enums.ArrowType;
import old_files.justmap.enums.MapShape;
import old_files.justmap.enums.MultiworldDetection;
import old_files.justmap.enums.ScreenPosition;

import javax.annotation.Nullable;

public class Settings {
	public static final PathConfig GENERAL = new PathConfig("general");
	public static final PathConfig MAP_INFO = new PathConfig("advanced_info");
	public static final PathConfig MAP_DETAILS = new PathConfig("map_details");
	public static final PathConfig MAP_APPEARANCE = new PathConfig("map_appearance");
	public static final PathConfig WAYPOINTS = new PathConfig("waypoints");
	public static final PathConfig RADAR = new PathConfig("entity_radar");

	@Nullable
	public static ScreenPosition getScreenPosition(PathConfig category, String key) {
		return category.getEnum(key, ScreenPositionEntry.class);
	}

	public static ScreenPosition getScreenPosition(PathConfig category, String key, ScreenPosition defaultValue) {
		return category.getEnum(key, defaultValue, ScreenPositionEntry.class);
	}

	@Nullable
	public static MapShape getMapShape(PathConfig category, String key) {
		return category.getEnum(key, MapShapeEntry.class);
	}

	public static MapShape getMapShape(PathConfig category, String key, MapShape defaultValue) {
		return category.getEnum(key, defaultValue, MapShapeEntry.class);
	}

	public static boolean isMapRound() {
		return getMapShape(GENERAL, "map_shape") == MapShape.CIRCLE;
	}

	public static class ScreenPositionEntry extends EnumEntry<ScreenPosition> {

		public ScreenPositionEntry(ScreenPosition defaultValue) {
			super(defaultValue);
		}
	}

	public static class ArrowTypeEntry extends EnumEntry<ArrowType> {

		public ArrowTypeEntry(ArrowType defaultValue) {
			super(defaultValue);
		}
	}

	public static class MultiworldDetectionEntry extends EnumEntry<MultiworldDetection> {
		public MultiworldDetectionEntry(MultiworldDetection defaultValue) {
			super(defaultValue);
		}
	}

	public static class MapShapeEntry extends EnumEntry<MapShape> {
		public MapShapeEntry(MapShape defaultValue) {
			super(defaultValue);
		}
	}
}
