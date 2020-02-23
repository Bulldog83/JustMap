package ru.bulldog.justmap.config;

import com.google.gson.JsonObject;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.config.ConfigKeeper.*;
import ru.bulldog.justmap.minimap.MapPosition;

public final class Config {
	
	private final static ConfigKeeper KEEPER = ConfigKeeper.getInstance();
	
	private static Config instance;
	
	public static Config get() {
		if (instance == null) {
			instance = new Config();
		}
		
		return instance;
	}
	
	private Config() {
		KEEPER.registerEntry("map_visible", new BooleanEntry(Params.mapVisible, (b) -> Params.mapVisible = b, () -> Params.mapVisible));
		KEEPER.registerEntry("map_position", new EnumEntry<MapPosition>(Params.mapPosition, (e) -> Params.mapPosition = e, () -> Params.mapPosition));
		KEEPER.registerEntry("map_offset", new IntegerEntry(Params.positionOffset, (i) -> Params.positionOffset = i, () -> Params.positionOffset));
		KEEPER.registerEntry("map_size", new IntegerRange(Params.mapSize, (i) -> Params.mapSize = i, () -> Params.mapSize, 16, 256));
		KEEPER.registerEntry("map_scale", new FloatRange(Params.mapScale, (f) -> Params.mapScale = f, () -> Params.mapScale, 0.25F, 2.0F));
		KEEPER.registerEntry("map_saturation", new IntegerRange(Params.mapSaturation, (i) -> Params.mapSaturation = i, () -> Params.mapSaturation, -50, 50));
		KEEPER.registerEntry("map_brightness", new IntegerRange(Params.mapBrightness, (i) -> Params.mapBrightness = i, () -> Params.mapBrightness, -50, 50));
		KEEPER.registerEntry("show_caves", new BooleanEntry(Params.showCaves, (b) -> Params.showCaves = b, () -> Params.showCaves));
		KEEPER.registerEntry("show_position", new BooleanEntry(Params.showPosition, (b) -> Params.showPosition = b, () -> Params.showPosition));
		KEEPER.registerEntry("show_FPS", new BooleanEntry(Params.showFPS, (b) -> Params.showFPS = b, () -> Params.showFPS));
		KEEPER.registerEntry("show_biome", new BooleanEntry(Params.showBiome, (b) -> Params.showBiome = b, () -> Params.showBiome));
		KEEPER.registerEntry("show_time", new BooleanEntry(Params.showTime, (b) -> Params.showTime = b, () -> Params.showTime));
		KEEPER.registerEntry("move_effects", new BooleanEntry(Params.moveEffects, (b) -> Params.moveEffects = b, () -> Params.moveEffects));
		KEEPER.registerEntry("show_effect_timers", new BooleanEntry(Params.showEffectTimers, (b) -> Params.showEffectTimers = b, () -> Params.showEffectTimers));
		KEEPER.registerEntry("alternate_color_render", new BooleanEntry(Params.alternateColorRender, (b) -> Params.alternateColorRender = b, () -> Params.alternateColorRender));
		KEEPER.registerEntry("use_skins", new BooleanEntry(Params.useSkins, (b) -> Params.useSkins = b, () -> Params.useSkins));
		KEEPER.registerEntry("current_skin", new IntegerEntry(Params.currentSkin, (i) -> Params.currentSkin = i, () -> Params.currentSkin));
		KEEPER.registerEntry("update_per_cycle", new IntegerRange(Params.updatePerCycle, (i) -> Params.updatePerCycle = i, () -> Params.updatePerCycle, 1, 500));
		KEEPER.registerEntry("purge_delay", new IntegerRange(Params.purgeDelay, (i) -> Params.purgeDelay = i, () -> Params.purgeDelay, 1, 600));
		KEEPER.registerEntry("purge_amount", new IntegerRange(Params.purgeAmount, (i) -> Params.purgeAmount = i, () -> Params.purgeAmount, 100, 50000));
		KEEPER.registerEntry("show_terrain", new BooleanEntry(Params.showTerrain, (b) -> Params.showTerrain = b, () -> Params.showTerrain));
		KEEPER.registerEntry("terrain_strength", new IntegerRange(Params.terrainStrength, (i) -> Params.terrainStrength = i, () -> Params.terrainStrength, 2, 9));
		KEEPER.registerEntry("draw_chunk_grid", new BooleanEntry(Params.drawChunkGrid, (b) -> Params.drawChunkGrid = b, () -> Params.drawChunkGrid));
		KEEPER.registerEntry("show_in_chat", new BooleanEntry(Params.showInChat, (b) -> Params.showInChat = b, () -> Params.showInChat));
		KEEPER.registerEntry("waypoints_tracking", new BooleanEntry(Params.waypointsTracking, (b) -> Params.waypointsTracking = b, () -> Params.waypointsTracking));
		KEEPER.registerEntry("waypoints_world_render", new BooleanEntry(Params.waypointsWorldRender, (b) -> Params.waypointsWorldRender = b, () -> Params.waypointsWorldRender));
		KEEPER.registerEntry("render_light_beam", new BooleanEntry(Params.renderLightBeam, (b) -> Params.renderLightBeam = b, () -> Params.renderLightBeam));
		KEEPER.registerEntry("render_markers", new BooleanEntry(Params.renderMarkers, (b) -> Params.renderMarkers = b, () -> Params.renderMarkers));
		KEEPER.registerEntry("render_animation", new BooleanEntry(Params.renderAnimation, (b) -> Params.renderAnimation = b, () -> Params.renderAnimation));
		KEEPER.registerEntry("min_render_dist", new IntegerRange(Params.minRenderDist, (i) -> Params.minRenderDist = i, () -> Params.minRenderDist, 1, 100));
		KEEPER.registerEntry("max_render_dist", new IntegerRange(Params.maxRenderDist, (i) -> Params.maxRenderDist = i, () -> Params.maxRenderDist, 10, 3000));
		KEEPER.registerEntry("show_entities", new BooleanEntry(Params.showEntities, (b) -> Params.showEntities = b, () -> Params.showEntities));
		KEEPER.registerEntry("show_entity_heads", new BooleanEntry(Params.showEntityHeads, (b) -> Params.showEntityHeads = b, () -> Params.showEntityHeads));
		KEEPER.registerEntry("show_hostile", new BooleanEntry(Params.showHostile, (b) -> Params.showHostile = b, () -> Params.showHostile));
		KEEPER.registerEntry("show_creatures", new BooleanEntry(Params.showCreatures, (b) -> Params.showCreatures = b, () -> Params.showCreatures));
		KEEPER.registerEntry("show_players", new BooleanEntry(Params.showPlayers, (b) -> Params.showPlayers = b, () -> Params.showPlayers));
		KEEPER.registerEntry("show_player_heads", new BooleanEntry(Params.showPlayerHeads, (b) -> Params.showPlayerHeads = b, () -> Params.showPlayerHeads));
		KEEPER.registerEntry("show_player_names", new BooleanEntry(Params.showPlayerNames, (b) -> Params.showPlayerNames = b, () -> Params.showPlayerNames));
		KEEPER.registerEntry("render_entity_model", new BooleanEntry(Params.renderEntityModel, (b) -> Params.renderEntityModel = b, () -> Params.renderEntityModel));
		KEEPER.registerEntry("show_icons_outline", new BooleanEntry(Params.showIconsOutline, (b) -> Params.showIconsOutline = b, () -> Params.showIconsOutline));
		KEEPER.registerEntry("entity_icon_size", new IntegerRange(Params.entityIconSize, (i) -> Params.entityIconSize = i, () -> Params.entityIconSize, 2, 16));
		KEEPER.registerEntry("entity_model_size", new IntegerRange(Params.entityModelSize, (i) -> Params.entityModelSize = i, () -> Params.entityModelSize, 2, 16));
		
		JsonObject config = ConfigWriter.load();
		if (config.size() > 0) {
			KEEPER.fromJson(config);
		} else {
			ConfigWriter.save(KEEPER.toJson());
		}
	}
	
	public Entry<?> getEntry(String key) {
		return KEEPER.getEntry(key);
	}
	
	public Object getDefault(String key) {
		Entry<?> entry = KEEPER.getEntry(key);
		return entry != null ? entry.getDefault() : null;
	}
	
	public String getString(String key) {
		String str = (String) KEEPER.getValue(key);		
		return str != null ? str : "";
	}
	
	public boolean setString(String key, String value) {
		try {
			StringEntry entry = (StringEntry) KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
			
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public int getInt(String key) {
		Integer val = (Integer) KEEPER.getValue(key);		
		return val != null ? val : 0;
	}
	
	public boolean setInt(String key, int value) {
		try {
			IntegerEntry entry = (IntegerEntry) KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
			
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public <T extends Comparable<T>> boolean setRanged(String key, T value) {
		try {
			Entry<?> entry = KEEPER.getEntry(key);
			if (entry instanceof RangeEntry) {
				@SuppressWarnings("unchecked")
				RangeEntry<T> range = (RangeEntry<T>) entry;
				
				range.setValue(value);
				KEEPER.set(key, range);
				
				return true;
			}
			
			return false;
		} catch (NullPointerException | ClassCastException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public float getFloat(String key) {
		Float val = (Float) KEEPER.getValue(key);		
		return val != null ? val : 0.0F;
	}
	
	public boolean setFloat(String key, float value) {
		try {
			FloatEntry entry = (FloatEntry) KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
			
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public boolean getBoolean(String key) {
		Boolean val = (Boolean) KEEPER.getValue(key);		
		return val != null ? val : false;
	}
	
	public boolean setBoolean(String key, boolean value) {
		try {
			BooleanEntry entry = (BooleanEntry) KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
			
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public void saveChanges() {
		ConfigWriter.save(KEEPER.toJson());
		JustMap.MAP.onConfigChanges();
	}
}
