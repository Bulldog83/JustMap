package ru.bulldog.justmap.client.config;

import com.google.gson.JsonObject;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.config.Config;
import ru.bulldog.justmap.config.ConfigKeeper.BooleanEntry;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.config.ConfigKeeper.FloatRange;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerEntry;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerRange;
import ru.bulldog.justmap.config.ConfigWriter;
import ru.bulldog.justmap.enums.ArrowType;
import ru.bulldog.justmap.enums.MapShape;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.map.data.MapDataProvider;

public class ClientConfig extends Config {

	private static ClientConfig instance;

	public static ClientConfig get() {
		if (instance == null) {
			instance = new ClientConfig();
		}

		return instance;
	}

	private ClientConfig() {
		KEEPER.registerEntry("map_visible", new BooleanEntry(ClientSettings.mapVisible, (b) -> ClientSettings.mapVisible = b, () -> ClientSettings.mapVisible));
		KEEPER.registerEntry("map_position", new EnumEntry<ScreenPosition>(ClientSettings.mapPosition, (e) -> ClientSettings.mapPosition = e, () -> ClientSettings.mapPosition));
		KEEPER.registerEntry("arrow_type", new EnumEntry<ArrowType>(ClientSettings.arrowIconType, (e) -> ClientSettings.arrowIconType = e, () -> ClientSettings.arrowIconType));
		KEEPER.registerEntry("map_offset", new IntegerEntry(ClientSettings.positionOffset, (i) -> ClientSettings.positionOffset = i, () -> ClientSettings.positionOffset));
		KEEPER.registerEntry("map_position_x", new IntegerEntry(ClientSettings.mapPositionX, (i) -> ClientSettings.mapPositionX = i, () -> ClientSettings.mapPositionX));
		KEEPER.registerEntry("map_position_y", new IntegerEntry(ClientSettings.mapPositionY, (i) -> ClientSettings.mapPositionY = i, () -> ClientSettings.mapPositionY));
		KEEPER.registerEntry("map_size", new IntegerRange(ClientSettings.mapSize, (i) -> ClientSettings.mapSize = i, () -> ClientSettings.mapSize, 16, 256));
		KEEPER.registerEntry("big_map_size", new IntegerRange(ClientSettings.bigMapSize, (i) -> ClientSettings.bigMapSize = i, () -> ClientSettings.bigMapSize, 256, 400));
		KEEPER.registerEntry("map_scale", new FloatRange(ClientSettings.mapScale, (f) -> ClientSettings.mapScale = f, () -> ClientSettings.mapScale, 0.25F, 2.0F));
		KEEPER.registerEntry("map_saturation", new IntegerRange(ClientSettings.mapSaturation, (i) -> ClientSettings.mapSaturation = i, () -> ClientSettings.mapSaturation, -50, 50));
		KEEPER.registerEntry("map_brightness", new IntegerRange(ClientSettings.mapBrightness, (i) -> ClientSettings.mapBrightness = i, () -> ClientSettings.mapBrightness, -50, 50));
		KEEPER.registerEntry("rotate_map", new BooleanEntry(ClientSettings.rotateMap, (b) -> ClientSettings.rotateMap = b, () -> ClientSettings.rotateMap));
		KEEPER.registerEntry("show_caves", new BooleanEntry(ClientSettings.drawCaves, (b) -> ClientSettings.drawCaves = b, () -> ClientSettings.drawCaves));
		KEEPER.registerEntry("hide_plants", new BooleanEntry(ClientSettings.hidePlants, (b) -> ClientSettings.hidePlants = b, () -> ClientSettings.hidePlants));
		KEEPER.registerEntry("hide_water", new BooleanEntry(ClientSettings.hideWater, (b) -> ClientSettings.hideWater = b, () -> ClientSettings.hideWater));
		KEEPER.registerEntry("advanced_info", new BooleanEntry(ClientSettings.advancedInfo, (b) -> ClientSettings.advancedInfo = b, () -> ClientSettings.advancedInfo));
		KEEPER.registerEntry("map_info", new BooleanEntry(ClientSettings.mapInfo, (b) -> ClientSettings.mapInfo = b, () -> ClientSettings.mapInfo));
		KEEPER.registerEntry("show_position", new BooleanEntry(ClientSettings.showPosition, (b) -> ClientSettings.showPosition = b, () -> ClientSettings.showPosition));
		KEEPER.registerEntry("show_FPS", new BooleanEntry(ClientSettings.showFPS, (b) -> ClientSettings.showFPS = b, () -> ClientSettings.showFPS));
		KEEPER.registerEntry("show_biome", new BooleanEntry(ClientSettings.showBiome, (b) -> ClientSettings.showBiome = b, () -> ClientSettings.showBiome));
		KEEPER.registerEntry("show_time", new BooleanEntry(ClientSettings.showTime, (b) -> ClientSettings.showTime = b, () -> ClientSettings.showTime));
		KEEPER.registerEntry("show_light", new BooleanEntry(ClientSettings.showLight, (b) -> ClientSettings.showLight = b, () -> ClientSettings.showLight));
		KEEPER.registerEntry("show_items", new BooleanEntry(ClientSettings.showItems, (b) -> ClientSettings.showItems = b, () -> ClientSettings.showItems));
		KEEPER.registerEntry("show_mainhand", new BooleanEntry(ClientSettings.showMainhand, (b) -> ClientSettings.showMainhand = b, () -> ClientSettings.showMainhand));
		KEEPER.registerEntry("show_offhand", new BooleanEntry(ClientSettings.showOffhand, (b) -> ClientSettings.showOffhand = b, () -> ClientSettings.showOffhand));
		KEEPER.registerEntry("show_head", new BooleanEntry(ClientSettings.showHead, (b) -> ClientSettings.showHead = b, () -> ClientSettings.showHead));
		KEEPER.registerEntry("show_chest", new BooleanEntry(ClientSettings.showChest, (b) -> ClientSettings.showChest = b, () -> ClientSettings.showChest));
		KEEPER.registerEntry("show_legs", new BooleanEntry(ClientSettings.showLegs, (b) -> ClientSettings.showLegs = b, () -> ClientSettings.showLegs));
		KEEPER.registerEntry("show_feet", new BooleanEntry(ClientSettings.showFeet, (b) -> ClientSettings.showFeet = b, () -> ClientSettings.showFeet));
		KEEPER.registerEntry("move_effects", new BooleanEntry(ClientSettings.moveEffects, (b) -> ClientSettings.moveEffects = b, () -> ClientSettings.moveEffects));
		KEEPER.registerEntry("show_effect_timers", new BooleanEntry(ClientSettings.showEffectTimers, (b) -> ClientSettings.showEffectTimers = b, () -> ClientSettings.showEffectTimers));
		KEEPER.registerEntry("alternate_color_render", new BooleanEntry(ClientSettings.alternateColorRender, (b) -> ClientSettings.alternateColorRender = b, () -> ClientSettings.alternateColorRender));
		KEEPER.registerEntry("texture_filter", new BooleanEntry(ClientSettings.textureFilter, (b) -> ClientSettings.textureFilter = b, () -> ClientSettings.textureFilter));
		KEEPER.registerEntry("water_tint", new BooleanEntry(ClientSettings.waterTint, (b) -> ClientSettings.waterTint = b, () -> ClientSettings.waterTint));
		KEEPER.registerEntry("use_skins", new BooleanEntry(ClientSettings.useSkins, (b) -> ClientSettings.useSkins = b, () -> ClientSettings.useSkins));
		KEEPER.registerEntry("skin_scale", new FloatRange(ClientSettings.skinScale, (f) -> ClientSettings.skinScale = f, () -> ClientSettings.skinScale, 0.5F, 3.0F));
		KEEPER.registerEntry("simple_direction_arrow", new BooleanEntry(ClientSettings.simpleArrow, (b) -> ClientSettings.simpleArrow = b, () -> ClientSettings.simpleArrow));
		KEEPER.registerEntry("current_skin", new IntegerEntry(ClientSettings.currentSkin, (i) -> ClientSettings.currentSkin = i, () -> ClientSettings.currentSkin));
		KEEPER.registerEntry("big_map_skin", new IntegerEntry(ClientSettings.bigMapSkin, (i) -> ClientSettings.bigMapSkin = i, () -> ClientSettings.bigMapSkin));
		KEEPER.registerEntry("chunk_update_interval", new IntegerRange(ClientSettings.chunkUpdateInterval, (i) -> ClientSettings.chunkUpdateInterval = i, () -> ClientSettings.chunkUpdateInterval, 500, 5000));
		KEEPER.registerEntry("chunk_level_update_interval", new IntegerRange(ClientSettings.chunkLevelUpdateInterval, (i) -> ClientSettings.chunkLevelUpdateInterval = i, () -> ClientSettings.chunkLevelUpdateInterval, 500, 10000));
		KEEPER.registerEntry("purge_delay", new IntegerRange(ClientSettings.purgeDelay, (i) -> ClientSettings.purgeDelay = i, () -> ClientSettings.purgeDelay, 1, 600));
		KEEPER.registerEntry("purge_amount", new IntegerRange(ClientSettings.purgeAmount, (i) -> ClientSettings.purgeAmount = i, () -> ClientSettings.purgeAmount, 100, 50000));
		KEEPER.registerEntry("show_terrain", new BooleanEntry(ClientSettings.showTerrain, (b) -> ClientSettings.showTerrain = b, () -> ClientSettings.showTerrain));
		KEEPER.registerEntry("show_topography", new BooleanEntry(ClientSettings.showTopography, (b) -> ClientSettings.showTopography = b, () -> ClientSettings.showTopography));
		KEEPER.registerEntry("terrain_strength", new IntegerRange(ClientSettings.terrainStrength, (i) -> ClientSettings.terrainStrength = i, () -> ClientSettings.terrainStrength, 2, 9));
		KEEPER.registerEntry("draw_chunk_grid", new BooleanEntry(ClientSettings.showGrid, (b) -> ClientSettings.showGrid = b, () -> ClientSettings.showGrid));
		KEEPER.registerEntry("draw_worldmap_grid", new BooleanEntry(ClientSettings.showWorldmapGrid, (b) -> ClientSettings.showWorldmapGrid = b, () -> ClientSettings.showWorldmapGrid));
		KEEPER.registerEntry("show_in_chat", new BooleanEntry(ClientSettings.showInChat, (b) -> ClientSettings.showInChat = b, () -> ClientSettings.showInChat));
		KEEPER.registerEntry("show_waypoints", new BooleanEntry(ClientSettings.showWaypoints, (b) -> ClientSettings.showWaypoints = b, () -> ClientSettings.showWaypoints));
		KEEPER.registerEntry("jump_to_waypoints", new BooleanEntry(ClientSettings.jumpToWaypoints, (b) -> ClientSettings.jumpToWaypoints = b, () -> ClientSettings.jumpToWaypoints));
		KEEPER.registerEntry("waypoints_tracking", new BooleanEntry(ClientSettings.waypointsTracking, (b) -> ClientSettings.waypointsTracking = b, () -> ClientSettings.waypointsTracking));
		KEEPER.registerEntry("waypoints_world_render", new BooleanEntry(ClientSettings.waypointsWorldRender, (b) -> ClientSettings.waypointsWorldRender = b, () -> ClientSettings.waypointsWorldRender));
		KEEPER.registerEntry("render_light_beam", new BooleanEntry(ClientSettings.renderLightBeam, (b) -> ClientSettings.renderLightBeam = b, () -> ClientSettings.renderLightBeam));
		KEEPER.registerEntry("render_markers", new BooleanEntry(ClientSettings.renderMarkers, (b) -> ClientSettings.renderMarkers = b, () -> ClientSettings.renderMarkers));
		KEEPER.registerEntry("render_animation", new BooleanEntry(ClientSettings.renderAnimation, (b) -> ClientSettings.renderAnimation = b, () -> ClientSettings.renderAnimation));
		KEEPER.registerEntry("min_render_dist", new IntegerRange(ClientSettings.minRenderDist, (i) -> ClientSettings.minRenderDist = i, () -> ClientSettings.minRenderDist, 1, 100));
		KEEPER.registerEntry("max_render_dist", new IntegerRange(ClientSettings.maxRenderDist, (i) -> ClientSettings.maxRenderDist = i, () -> ClientSettings.maxRenderDist, 10, 3000));
		KEEPER.registerEntry("show_entities", new BooleanEntry(ClientSettings.showEntities, (b) -> ClientSettings.showEntities = b, () -> ClientSettings.showEntities));
		KEEPER.registerEntry("show_entity_heads", new BooleanEntry(ClientSettings.showEntityHeads, (b) -> ClientSettings.showEntityHeads = b, () -> ClientSettings.showEntityHeads));
		KEEPER.registerEntry("show_hostile", new BooleanEntry(ClientSettings.showHostile, (b) -> ClientSettings.showHostile = b, () -> ClientSettings.showHostile));
		KEEPER.registerEntry("show_creatures", new BooleanEntry(ClientSettings.showCreatures, (b) -> ClientSettings.showCreatures = b, () -> ClientSettings.showCreatures));
		KEEPER.registerEntry("show_players", new BooleanEntry(ClientSettings.showPlayers, (b) -> ClientSettings.showPlayers = b, () -> ClientSettings.showPlayers));
		KEEPER.registerEntry("show_player_heads", new BooleanEntry(ClientSettings.showPlayerHeads, (b) -> ClientSettings.showPlayerHeads = b, () -> ClientSettings.showPlayerHeads));
		KEEPER.registerEntry("show_player_names", new BooleanEntry(ClientSettings.showPlayerNames, (b) -> ClientSettings.showPlayerNames = b, () -> ClientSettings.showPlayerNames));
		KEEPER.registerEntry("render_entity_model", new BooleanEntry(ClientSettings.renderEntityModel, (b) -> ClientSettings.renderEntityModel = b, () -> ClientSettings.renderEntityModel));
		KEEPER.registerEntry("map_icons_shading", new BooleanEntry(ClientSettings.entityIconsShading, (b) -> ClientSettings.entityIconsShading = b, () -> ClientSettings.entityIconsShading));
		KEEPER.registerEntry("show_icons_outline", new BooleanEntry(ClientSettings.showIconsOutline, (b) -> ClientSettings.showIconsOutline = b, () -> ClientSettings.showIconsOutline));
		KEEPER.registerEntry("show_big_map", new BooleanEntry(ClientSettings.showBigMap, (b) -> ClientSettings.showBigMap = b, () -> ClientSettings.showBigMap));
		KEEPER.registerEntry("force_map_update", new BooleanEntry(ClientSettings.forceUpdate, (b) -> ClientSettings.forceUpdate = b, () -> ClientSettings.forceUpdate));
		KEEPER.registerEntry("use_fast_render", new BooleanEntry(ClientSettings.fastRender, (b) -> ClientSettings.fastRender = b, () -> ClientSettings.fastRender));
		KEEPER.registerEntry("show_slime", new BooleanEntry(ClientSettings.showSlime, (b) -> ClientSettings.showSlime = b, () -> ClientSettings.showSlime));
		KEEPER.registerEntry("show_loaded_chunks", new BooleanEntry(ClientSettings.showLoadedChunks, (b) -> ClientSettings.showLoadedChunks = b, () -> ClientSettings.showLoadedChunks));
		KEEPER.registerEntry("detect_multiworlds", new BooleanEntry(ClientSettings.detectMultiworlds, (b) -> ClientSettings.detectMultiworlds = b, () -> ClientSettings.detectMultiworlds));
		KEEPER.registerEntry("entity_icon_size", new IntegerRange(ClientSettings.entityIconSize, (i) -> ClientSettings.entityIconSize = i, () -> ClientSettings.entityIconSize, 2, 16));
		KEEPER.registerEntry("entity_model_size", new IntegerRange(ClientSettings.entityModelSize, (i) -> ClientSettings.entityModelSize = i, () -> ClientSettings.entityModelSize, 2, 16));
		KEEPER.registerEntry("entity_outline_size", new IntegerRange(ClientSettings.entityOutlineSize, (i) -> ClientSettings.entityOutlineSize = i, () -> ClientSettings.entityOutlineSize, 1, 5));
		KEEPER.registerEntry("arrow_size", new IntegerRange(ClientSettings.arrowIconSize, (i) -> ClientSettings.arrowIconSize = i, () -> ClientSettings.arrowIconSize, 6, 16));
		KEEPER.registerEntry("worldmap_icon_size", new IntegerRange(ClientSettings.worldmapIconSize, (i) -> ClientSettings.worldmapIconSize = i, () -> ClientSettings.worldmapIconSize, 8, 16));
		KEEPER.registerEntry("info_position", new EnumEntry<ScreenPosition>(ClientSettings.infoPosition, (e) -> ClientSettings.infoPosition = e, () -> ClientSettings.infoPosition));
		KEEPER.registerEntry("items_position", new EnumEntry<ScreenPosition>(ClientSettings.itemsPosition, (e) -> ClientSettings.itemsPosition = e, () -> ClientSettings.itemsPosition));
		KEEPER.registerEntry("map_shape", new EnumEntry<MapShape>(ClientSettings.mapShape, (e) -> ClientSettings.mapShape = e, () -> ClientSettings.mapShape));
		KEEPER.registerEntry("multiworld_detection", new EnumEntry<MultiworldDetection>(ClientSettings.multiworldDetection, (e) -> ClientSettings.multiworldDetection = e, () -> ClientSettings.multiworldDetection));

		JsonObject config = ConfigWriter.load();
		if (config.size() > 0) {
			KEEPER.fromJson(config);
		} else {
			ConfigWriter.save(KEEPER.toJson());
		}
	}

	public float getMapScale() {
		return this.getFloat("map_scale");
	}

	public void updateMapScale(float value) {
		this.setRanged("map_scale", this.getMapScale() * value);
		this.saveChanges();
	}

	public void reloadFromDisk() {
		JsonObject config = ConfigWriter.load();
		if (config.size() > 0) {
			KEEPER.fromJson(config);
		}
	}

	@Override
	public void saveChanges() {
		ConfigWriter.save(KEEPER.toJson());
		JustMapClient.getMiniMap().updateMapParams();
		MapDataProvider.getManager().onConfigUpdate();
	}
}
