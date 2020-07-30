package ru.bulldog.justmap.client.config;

import com.google.gson.JsonObject;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.config.Config;
import ru.bulldog.justmap.config.ConfigWriter;
import ru.bulldog.justmap.config.ConfigKeeper.BooleanEntry;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.config.ConfigKeeper.FloatRange;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerEntry;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerRange;
import ru.bulldog.justmap.enums.ArrowType;
import ru.bulldog.justmap.enums.MapShape;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.map.data.WorldManager;

public class ClientConfig extends Config {
	
	private static ClientConfig instance;
	
	public static ClientConfig get() {
		if (instance == null) {
			instance = new ClientConfig();
		}
		
		return instance;
	}
	
	private ClientConfig() {
		KEEPER.registerEntry("map_visible", new BooleanEntry(ClientParams.mapVisible, (b) -> ClientParams.mapVisible = b, () -> ClientParams.mapVisible));
		KEEPER.registerEntry("map_position", new EnumEntry<ScreenPosition>(ClientParams.mapPosition, (e) -> ClientParams.mapPosition = e, () -> ClientParams.mapPosition));
		KEEPER.registerEntry("arrow_type", new EnumEntry<ArrowType>(ClientParams.arrowIconType, (e) -> ClientParams.arrowIconType = e, () -> ClientParams.arrowIconType));
		KEEPER.registerEntry("map_offset", new IntegerEntry(ClientParams.positionOffset, (i) -> ClientParams.positionOffset = i, () -> ClientParams.positionOffset));
		KEEPER.registerEntry("map_size", new IntegerRange(ClientParams.mapSize, (i) -> ClientParams.mapSize = i, () -> ClientParams.mapSize, 16, 256));
		KEEPER.registerEntry("big_map_size", new IntegerRange(ClientParams.bigMapSize, (i) -> ClientParams.bigMapSize = i, () -> ClientParams.bigMapSize, 256, 400));
		KEEPER.registerEntry("map_scale", new FloatRange(ClientParams.mapScale, (f) -> ClientParams.mapScale = f, () -> ClientParams.mapScale, 0.25F, 2.0F));
		KEEPER.registerEntry("map_saturation", new IntegerRange(ClientParams.mapSaturation, (i) -> ClientParams.mapSaturation = i, () -> ClientParams.mapSaturation, -50, 50));
		KEEPER.registerEntry("map_brightness", new IntegerRange(ClientParams.mapBrightness, (i) -> ClientParams.mapBrightness = i, () -> ClientParams.mapBrightness, -50, 50));
		KEEPER.registerEntry("rotate_map", new BooleanEntry(ClientParams.rotateMap, (b) -> ClientParams.rotateMap = b, () -> ClientParams.rotateMap));
		KEEPER.registerEntry("show_caves", new BooleanEntry(ClientParams.drawCaves, (b) -> ClientParams.drawCaves = b, () -> ClientParams.drawCaves));
		KEEPER.registerEntry("hide_plants", new BooleanEntry(ClientParams.hidePlants, (b) -> ClientParams.hidePlants = b, () -> ClientParams.hidePlants));
		KEEPER.registerEntry("hide_water", new BooleanEntry(ClientParams.hideWater, (b) -> ClientParams.hideWater = b, () -> ClientParams.hideWater));
		KEEPER.registerEntry("advanced_info", new BooleanEntry(ClientParams.advancedInfo, (b) -> ClientParams.advancedInfo = b, () -> ClientParams.advancedInfo));
		KEEPER.registerEntry("map_info", new BooleanEntry(ClientParams.mapInfo, (b) -> ClientParams.mapInfo = b, () -> ClientParams.mapInfo));
		KEEPER.registerEntry("show_position", new BooleanEntry(ClientParams.showPosition, (b) -> ClientParams.showPosition = b, () -> ClientParams.showPosition));
		KEEPER.registerEntry("show_FPS", new BooleanEntry(ClientParams.showFPS, (b) -> ClientParams.showFPS = b, () -> ClientParams.showFPS));
		KEEPER.registerEntry("show_biome", new BooleanEntry(ClientParams.showBiome, (b) -> ClientParams.showBiome = b, () -> ClientParams.showBiome));
		KEEPER.registerEntry("show_time", new BooleanEntry(ClientParams.showTime, (b) -> ClientParams.showTime = b, () -> ClientParams.showTime));
		KEEPER.registerEntry("show_light", new BooleanEntry(ClientParams.showLight, (b) -> ClientParams.showLight = b, () -> ClientParams.showLight));
		KEEPER.registerEntry("show_items", new BooleanEntry(ClientParams.showItems, (b) -> ClientParams.showItems = b, () -> ClientParams.showItems));
		KEEPER.registerEntry("show_mainhand", new BooleanEntry(ClientParams.showMainhand, (b) -> ClientParams.showMainhand = b, () -> ClientParams.showMainhand));
		KEEPER.registerEntry("show_offhand", new BooleanEntry(ClientParams.showOffhand, (b) -> ClientParams.showOffhand = b, () -> ClientParams.showOffhand));
		KEEPER.registerEntry("show_head", new BooleanEntry(ClientParams.showHead, (b) -> ClientParams.showHead = b, () -> ClientParams.showHead));
		KEEPER.registerEntry("show_chest", new BooleanEntry(ClientParams.showChest, (b) -> ClientParams.showChest = b, () -> ClientParams.showChest));
		KEEPER.registerEntry("show_legs", new BooleanEntry(ClientParams.showLegs, (b) -> ClientParams.showLegs = b, () -> ClientParams.showLegs));
		KEEPER.registerEntry("show_feet", new BooleanEntry(ClientParams.showFeet, (b) -> ClientParams.showFeet = b, () -> ClientParams.showFeet));
		KEEPER.registerEntry("move_effects", new BooleanEntry(ClientParams.moveEffects, (b) -> ClientParams.moveEffects = b, () -> ClientParams.moveEffects));
		KEEPER.registerEntry("show_effect_timers", new BooleanEntry(ClientParams.showEffectTimers, (b) -> ClientParams.showEffectTimers = b, () -> ClientParams.showEffectTimers));
		KEEPER.registerEntry("alternate_color_render", new BooleanEntry(ClientParams.alternateColorRender, (b) -> ClientParams.alternateColorRender = b, () -> ClientParams.alternateColorRender));
		KEEPER.registerEntry("texture_filter", new BooleanEntry(ClientParams.textureFilter, (b) -> ClientParams.textureFilter = b, () -> ClientParams.textureFilter));
		KEEPER.registerEntry("water_tint", new BooleanEntry(ClientParams.waterTint, (b) -> ClientParams.waterTint = b, () -> ClientParams.waterTint));
		KEEPER.registerEntry("use_skins", new BooleanEntry(ClientParams.useSkins, (b) -> ClientParams.useSkins = b, () -> ClientParams.useSkins));
		KEEPER.registerEntry("skin_scale", new FloatRange(ClientParams.skinScale, (f) -> ClientParams.skinScale = f, () -> ClientParams.skinScale, 0.5F, 3.0F));
		KEEPER.registerEntry("simple_direction_arrow", new BooleanEntry(ClientParams.simpleArrow, (b) -> ClientParams.simpleArrow = b, () -> ClientParams.simpleArrow));
		KEEPER.registerEntry("current_skin", new IntegerEntry(ClientParams.currentSkin, (i) -> ClientParams.currentSkin = i, () -> ClientParams.currentSkin));
		KEEPER.registerEntry("big_map_skin", new IntegerEntry(ClientParams.bigMapSkin, (i) -> ClientParams.bigMapSkin = i, () -> ClientParams.bigMapSkin));
		KEEPER.registerEntry("chunk_update_interval", new IntegerRange(ClientParams.chunkUpdateInterval, (i) -> ClientParams.chunkUpdateInterval = i, () -> ClientParams.chunkUpdateInterval, 500, 5000));
		KEEPER.registerEntry("chunk_level_update_interval", new IntegerRange(ClientParams.chunkLevelUpdateInterval, (i) -> ClientParams.chunkLevelUpdateInterval = i, () -> ClientParams.chunkLevelUpdateInterval, 500, 10000));
		KEEPER.registerEntry("purge_delay", new IntegerRange(ClientParams.purgeDelay, (i) -> ClientParams.purgeDelay = i, () -> ClientParams.purgeDelay, 1, 600));
		KEEPER.registerEntry("purge_amount", new IntegerRange(ClientParams.purgeAmount, (i) -> ClientParams.purgeAmount = i, () -> ClientParams.purgeAmount, 100, 50000));
		KEEPER.registerEntry("show_terrain", new BooleanEntry(ClientParams.showTerrain, (b) -> ClientParams.showTerrain = b, () -> ClientParams.showTerrain));
		KEEPER.registerEntry("show_topography", new BooleanEntry(ClientParams.showTopography, (b) -> ClientParams.showTopography = b, () -> ClientParams.showTopography));
		KEEPER.registerEntry("terrain_strength", new IntegerRange(ClientParams.terrainStrength, (i) -> ClientParams.terrainStrength = i, () -> ClientParams.terrainStrength, 2, 9));
		KEEPER.registerEntry("draw_chunk_grid", new BooleanEntry(ClientParams.showGrid, (b) -> ClientParams.showGrid = b, () -> ClientParams.showGrid));
		KEEPER.registerEntry("show_in_chat", new BooleanEntry(ClientParams.showInChat, (b) -> ClientParams.showInChat = b, () -> ClientParams.showInChat));
		KEEPER.registerEntry("show_waypoints", new BooleanEntry(ClientParams.showWaypoints, (b) -> ClientParams.showWaypoints = b, () -> ClientParams.showWaypoints));
		KEEPER.registerEntry("jump_to_waypoints", new BooleanEntry(ClientParams.jumpToWaypoints, (b) -> ClientParams.jumpToWaypoints = b, () -> ClientParams.jumpToWaypoints));
		KEEPER.registerEntry("waypoints_tracking", new BooleanEntry(ClientParams.waypointsTracking, (b) -> ClientParams.waypointsTracking = b, () -> ClientParams.waypointsTracking));
		KEEPER.registerEntry("waypoints_world_render", new BooleanEntry(ClientParams.waypointsWorldRender, (b) -> ClientParams.waypointsWorldRender = b, () -> ClientParams.waypointsWorldRender));
		KEEPER.registerEntry("render_light_beam", new BooleanEntry(ClientParams.renderLightBeam, (b) -> ClientParams.renderLightBeam = b, () -> ClientParams.renderLightBeam));
		KEEPER.registerEntry("render_markers", new BooleanEntry(ClientParams.renderMarkers, (b) -> ClientParams.renderMarkers = b, () -> ClientParams.renderMarkers));
		KEEPER.registerEntry("render_animation", new BooleanEntry(ClientParams.renderAnimation, (b) -> ClientParams.renderAnimation = b, () -> ClientParams.renderAnimation));
		KEEPER.registerEntry("min_render_dist", new IntegerRange(ClientParams.minRenderDist, (i) -> ClientParams.minRenderDist = i, () -> ClientParams.minRenderDist, 1, 100));
		KEEPER.registerEntry("max_render_dist", new IntegerRange(ClientParams.maxRenderDist, (i) -> ClientParams.maxRenderDist = i, () -> ClientParams.maxRenderDist, 10, 3000));
		KEEPER.registerEntry("show_entities", new BooleanEntry(ClientParams.showEntities, (b) -> ClientParams.showEntities = b, () -> ClientParams.showEntities));
		KEEPER.registerEntry("show_entity_heads", new BooleanEntry(ClientParams.showEntityHeads, (b) -> ClientParams.showEntityHeads = b, () -> ClientParams.showEntityHeads));
		KEEPER.registerEntry("show_hostile", new BooleanEntry(ClientParams.showHostile, (b) -> ClientParams.showHostile = b, () -> ClientParams.showHostile));
		KEEPER.registerEntry("show_creatures", new BooleanEntry(ClientParams.showCreatures, (b) -> ClientParams.showCreatures = b, () -> ClientParams.showCreatures));
		KEEPER.registerEntry("show_players", new BooleanEntry(ClientParams.showPlayers, (b) -> ClientParams.showPlayers = b, () -> ClientParams.showPlayers));
		KEEPER.registerEntry("show_player_heads", new BooleanEntry(ClientParams.showPlayerHeads, (b) -> ClientParams.showPlayerHeads = b, () -> ClientParams.showPlayerHeads));
		KEEPER.registerEntry("show_player_names", new BooleanEntry(ClientParams.showPlayerNames, (b) -> ClientParams.showPlayerNames = b, () -> ClientParams.showPlayerNames));
		KEEPER.registerEntry("render_entity_model", new BooleanEntry(ClientParams.renderEntityModel, (b) -> ClientParams.renderEntityModel = b, () -> ClientParams.renderEntityModel));
		KEEPER.registerEntry("show_icons_outline", new BooleanEntry(ClientParams.showIconsOutline, (b) -> ClientParams.showIconsOutline = b, () -> ClientParams.showIconsOutline));
		KEEPER.registerEntry("show_big_map", new BooleanEntry(ClientParams.showBigMap, (b) -> ClientParams.showBigMap = b, () -> ClientParams.showBigMap));
		KEEPER.registerEntry("force_map_update", new BooleanEntry(ClientParams.forceUpdate, (b) -> ClientParams.forceUpdate = b, () -> ClientParams.forceUpdate));
		KEEPER.registerEntry("show_slime", new BooleanEntry(ClientParams.showSlime, (b) -> ClientParams.showSlime = b, () -> ClientParams.showSlime));
		KEEPER.registerEntry("show_loaded_chunks", new BooleanEntry(ClientParams.showLoadedChunks, (b) -> ClientParams.showLoadedChunks = b, () -> ClientParams.showLoadedChunks));
		KEEPER.registerEntry("detect_multiworlds", new BooleanEntry(ClientParams.detectMultiworlds, (b) -> ClientParams.detectMultiworlds = b, () -> ClientParams.detectMultiworlds));
		KEEPER.registerEntry("entity_icon_size", new IntegerRange(ClientParams.entityIconSize, (i) -> ClientParams.entityIconSize = i, () -> ClientParams.entityIconSize, 2, 16));
		KEEPER.registerEntry("entity_model_size", new IntegerRange(ClientParams.entityModelSize, (i) -> ClientParams.entityModelSize = i, () -> ClientParams.entityModelSize, 2, 16));
		KEEPER.registerEntry("entity_outline_size", new IntegerRange(ClientParams.entityOutlineSize, (i) -> ClientParams.entityOutlineSize = i, () -> ClientParams.entityOutlineSize, 1, 5));
		KEEPER.registerEntry("arrow_size", new IntegerRange(ClientParams.arrowIconSize, (i) -> ClientParams.arrowIconSize = i, () -> ClientParams.arrowIconSize, 6, 16));
		KEEPER.registerEntry("worldmap_icon_size", new IntegerRange(ClientParams.worldmapIconSize, (i) -> ClientParams.worldmapIconSize = i, () -> ClientParams.worldmapIconSize, 8, 16));
		KEEPER.registerEntry("info_position", new EnumEntry<ScreenPosition>(ClientParams.infoPosition, (e) -> ClientParams.infoPosition = e, () -> ClientParams.infoPosition));
		KEEPER.registerEntry("items_position", new EnumEntry<ScreenPosition>(ClientParams.itemsPosition, (e) -> ClientParams.itemsPosition = e, () -> ClientParams.itemsPosition));
		KEEPER.registerEntry("map_shape", new EnumEntry<MapShape>(ClientParams.mapShape, (e) -> ClientParams.mapShape = e, () -> ClientParams.mapShape));
		KEEPER.registerEntry("multiworld_detection", new EnumEntry<MultiworldDetection>(ClientParams.multiworldDetection, (e) -> ClientParams.multiworldDetection = e, () -> ClientParams.multiworldDetection));
		
		JsonObject config = ConfigWriter.load();
		if (config.size() > 0) {
			KEEPER.fromJson(config);
		} else {
			ConfigWriter.save(KEEPER.toJson());
		}
	}

	@Override
	public void saveChanges()  {
		WorldManager.onConfigUpdate();
		JustMapClient.MAP.updateMapParams();
		ConfigWriter.save(KEEPER.toJson());
	}
}
