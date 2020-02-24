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
import ru.bulldog.justmap.minimap.MapPosition;

public class ClientConfig extends Config{
	
	private static ClientConfig instance;
	
	public static ClientConfig get() {
		if (instance == null) {
			instance = new ClientConfig();
		}
		
		return instance;
	}
	
	private ClientConfig() {
		KEEPER.registerEntry("map_visible", new BooleanEntry(ClientParams.mapVisible, (b) -> ClientParams.mapVisible = b, () -> ClientParams.mapVisible));
		KEEPER.registerEntry("map_position", new EnumEntry<MapPosition>(ClientParams.mapPosition, (e) -> ClientParams.mapPosition = e, () -> ClientParams.mapPosition));
		KEEPER.registerEntry("map_offset", new IntegerEntry(ClientParams.positionOffset, (i) -> ClientParams.positionOffset = i, () -> ClientParams.positionOffset));
		KEEPER.registerEntry("map_size", new IntegerRange(ClientParams.mapSize, (i) -> ClientParams.mapSize = i, () -> ClientParams.mapSize, 16, 256));
		KEEPER.registerEntry("map_scale", new FloatRange(ClientParams.mapScale, (f) -> ClientParams.mapScale = f, () -> ClientParams.mapScale, 0.25F, 2.0F));
		KEEPER.registerEntry("map_saturation", new IntegerRange(ClientParams.mapSaturation, (i) -> ClientParams.mapSaturation = i, () -> ClientParams.mapSaturation, -50, 50));
		KEEPER.registerEntry("map_brightness", new IntegerRange(ClientParams.mapBrightness, (i) -> ClientParams.mapBrightness = i, () -> ClientParams.mapBrightness, -50, 50));
		KEEPER.registerEntry("show_caves", new BooleanEntry(ClientParams.showCaves, (b) -> ClientParams.showCaves = b, () -> ClientParams.showCaves));
		KEEPER.registerEntry("show_position", new BooleanEntry(ClientParams.showPosition, (b) -> ClientParams.showPosition = b, () -> ClientParams.showPosition));
		KEEPER.registerEntry("show_FPS", new BooleanEntry(ClientParams.showFPS, (b) -> ClientParams.showFPS = b, () -> ClientParams.showFPS));
		KEEPER.registerEntry("show_biome", new BooleanEntry(ClientParams.showBiome, (b) -> ClientParams.showBiome = b, () -> ClientParams.showBiome));
		KEEPER.registerEntry("show_time", new BooleanEntry(ClientParams.showTime, (b) -> ClientParams.showTime = b, () -> ClientParams.showTime));
		KEEPER.registerEntry("move_effects", new BooleanEntry(ClientParams.moveEffects, (b) -> ClientParams.moveEffects = b, () -> ClientParams.moveEffects));
		KEEPER.registerEntry("show_effect_timers", new BooleanEntry(ClientParams.showEffectTimers, (b) -> ClientParams.showEffectTimers = b, () -> ClientParams.showEffectTimers));
		KEEPER.registerEntry("alternate_color_render", new BooleanEntry(ClientParams.alternateColorRender, (b) -> ClientParams.alternateColorRender = b, () -> ClientParams.alternateColorRender));
		KEEPER.registerEntry("use_skins", new BooleanEntry(ClientParams.useSkins, (b) -> ClientParams.useSkins = b, () -> ClientParams.useSkins));
		KEEPER.registerEntry("current_skin", new IntegerEntry(ClientParams.currentSkin, (i) -> ClientParams.currentSkin = i, () -> ClientParams.currentSkin));
		KEEPER.registerEntry("update_per_cycle", new IntegerRange(ClientParams.updatePerCycle, (i) -> ClientParams.updatePerCycle = i, () -> ClientParams.updatePerCycle, 1, 500));
		KEEPER.registerEntry("purge_delay", new IntegerRange(ClientParams.purgeDelay, (i) -> ClientParams.purgeDelay = i, () -> ClientParams.purgeDelay, 1, 600));
		KEEPER.registerEntry("purge_amount", new IntegerRange(ClientParams.purgeAmount, (i) -> ClientParams.purgeAmount = i, () -> ClientParams.purgeAmount, 100, 50000));
		KEEPER.registerEntry("show_terrain", new BooleanEntry(ClientParams.showTerrain, (b) -> ClientParams.showTerrain = b, () -> ClientParams.showTerrain));
		KEEPER.registerEntry("terrain_strength", new IntegerRange(ClientParams.terrainStrength, (i) -> ClientParams.terrainStrength = i, () -> ClientParams.terrainStrength, 2, 9));
		KEEPER.registerEntry("draw_chunk_grid", new BooleanEntry(ClientParams.drawChunkGrid, (b) -> ClientParams.drawChunkGrid = b, () -> ClientParams.drawChunkGrid));
		KEEPER.registerEntry("show_in_chat", new BooleanEntry(ClientParams.showInChat, (b) -> ClientParams.showInChat = b, () -> ClientParams.showInChat));
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
		KEEPER.registerEntry("entity_icon_size", new IntegerRange(ClientParams.entityIconSize, (i) -> ClientParams.entityIconSize = i, () -> ClientParams.entityIconSize, 2, 16));
		KEEPER.registerEntry("entity_model_size", new IntegerRange(ClientParams.entityModelSize, (i) -> ClientParams.entityModelSize = i, () -> ClientParams.entityModelSize, 2, 16));
		
		JsonObject config = ConfigWriter.load();
		if (config.size() > 0) {
			KEEPER.fromJson(config);
		} else {
			ConfigWriter.save(KEEPER.toJson());
		}
	}

	@Override
	public void saveChanges()  {
		ConfigWriter.save(KEEPER.toJson());
		JustMapClient.MAP.onConfigChanges();
	}
}
