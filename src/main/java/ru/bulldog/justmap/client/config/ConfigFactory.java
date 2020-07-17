package ru.bulldog.justmap.client.config;

import java.util.Arrays;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.map.DirectionArrow;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.util.ScreenPosition;

public final class ConfigFactory {
	
	private static Text lang(String key) {
		return new TranslatableText("justmap.configuration." + key);
	}
	
	public static Screen getConfigScreen(Screen parent) {
		ConfigBuilder configBuilder = ConfigFactory.getConfigBuilder();		
		configBuilder.setParentScreen(parent);
		
		return configBuilder.build();
	}
	
	private static ConfigBuilder getConfigBuilder() {
		ConfigBuilder configBuilder = ConfigBuilder.create().setTitle(new LiteralText("Just Map Configuration"));
		ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
		
		ConfigCategory general = configBuilder.getOrCreateCategory(lang("category.general"));
		
		@SuppressWarnings("unchecked")
		EnumEntry<ScreenPosition> drawPosConfig = (EnumEntry<ScreenPosition>) JustMapClient.CONFIG.getEntry("map_position");
		EnumSelectorBuilder<ScreenPosition> drawPosEntry = entryBuilder.startEnumSelector(lang("map_position"), ScreenPosition.class, drawPosConfig.getValue());
		drawPosEntry.setSaveConsumer(val -> drawPosConfig.setValue(val))
					.setDefaultValue(drawPosConfig.getDefault());
		
		general.addEntry(drawPosEntry.build());
		general.addEntry(entryBuilder.startIntField(lang("map_offset"), JustMapClient.CONFIG.getInt("map_offset"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setInt("map_offset", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("map_offset"))
				.build());
		general.addEntry(entryBuilder.startDropdownMenu(lang("map_size"), JustMapClient.CONFIG.getInt("map_size"), (val) -> {
					return getIntValue(val, (int) JustMapClient.CONFIG.getDefault("map_size"));
				})
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("map_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("map_size"))
				.setSelections(Arrays.asList(32, 64, 96, 128, 160, 192, 224, 256))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_big_map"), JustMapClient.CONFIG.getBoolean("show_big_map"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_big_map", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_big_map"))
				.build());
		general.addEntry(entryBuilder.startDropdownMenu(lang("big_map_size"), JustMapClient.CONFIG.getInt("big_map_size"), (val) -> {
					return getIntValue(val, (int) JustMapClient.CONFIG.getDefault("big_map_size"));
				})
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("big_map_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("big_map_size"))
				.setSelections(Arrays.asList(256, 272, 288, 304, 320, 336, 352, 368, 384, 400))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_in_chat"), JustMapClient.CONFIG.getBoolean("show_in_chat"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_in_chat", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_in_chat"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("move_effects"), JustMapClient.CONFIG.getBoolean("move_effects"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("move_effects", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("move_effects"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_effect_timers"), JustMapClient.CONFIG.getBoolean("show_effect_timers"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_effect_timers", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_effect_timers"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("rotate_map"), JustMapClient.CONFIG.getBoolean("rotate_map"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("rotate_map", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("rotate_map"))
				.build());
		
		ConfigCategory mapDetails = configBuilder.getOrCreateCategory(lang("category.details"));
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_caves"), JustMapClient.CONFIG.getBoolean("show_caves"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_caves", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_caves"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_terrain"), JustMapClient.CONFIG.getBoolean("show_terrain"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_terrain", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_terrain"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_topography"), JustMapClient.CONFIG.getBoolean("show_topography"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_topography", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_topography"))
				.build());
		mapDetails.addEntry(entryBuilder.startIntSlider(lang("terrain_strength"), JustMapClient.CONFIG.getInt("terrain_strength"), 2, 9)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("terrain_strength", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("terrain_strength"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_grid"), JustMapClient.CONFIG.getBoolean("draw_chunk_grid"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("draw_chunk_grid", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("draw_chunk_grid"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_slime_chunks"), JustMapClient.CONFIG.getBoolean("show_slime"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_slime", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_slime"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_loaded_chunks"), JustMapClient.CONFIG.getBoolean("show_loaded_chunks"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_loaded_chunks", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_loaded_chunks"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("hide_plants"), JustMapClient.CONFIG.getBoolean("hide_plants"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("hide_plants", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("hide_plants"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("hide_water"), JustMapClient.CONFIG.getBoolean("hide_water"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("hide_water", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("hide_water"))
				.build());
		
		@SuppressWarnings("unchecked")
		EnumEntry<DirectionArrow.Type> arrowTypeConfig = (EnumEntry<DirectionArrow.Type>) JustMapClient.CONFIG.getEntry("arrow_type");
		EnumSelectorBuilder<DirectionArrow.Type> arrowTypeEntry = entryBuilder.startEnumSelector(lang("arrow_type"), DirectionArrow.Type.class, arrowTypeConfig.getValue());
		arrowTypeEntry.setSaveConsumer(val -> arrowTypeConfig.setValue(val))
					  .setDefaultValue(arrowTypeConfig.getDefault());
		@SuppressWarnings("unchecked")
		EnumEntry<Minimap.Shape> mapShapeConfig = (EnumEntry<Minimap.Shape>) JustMapClient.CONFIG.getEntry("map_shape");
		EnumSelectorBuilder<Minimap.Shape> mapShapeEntry = entryBuilder.startEnumSelector(lang("map_shape"), Minimap.Shape.class, mapShapeConfig.getValue());
		mapShapeEntry.setSaveConsumer(val -> mapShapeConfig.setValue(val))
					 .setDefaultValue(mapShapeConfig.getDefault());
		FloatSliderBuilder doubleSlider = new FloatSliderBuilder(entryBuilder.getResetButtonKey(), lang("skin_border_scale"), JustMapClient.CONFIG.getFloat("skin_scale"), 0.5F, 3.0F)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("skin_scale", val))
				.setDefaultValue((float) JustMapClient.CONFIG.getDefault("skin_scale"));
		
		ConfigCategory mapAppearance = configBuilder.getOrCreateCategory(lang("category.appearance"));
		mapAppearance.addEntry(mapShapeEntry.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("use_skins"), JustMapClient.CONFIG.getBoolean("use_skins"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("use_skins", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("use_skins"))
				.build());
		mapAppearance.addEntry(entryBuilder.startDropdownMenu(lang("current_skin"), MapSkin.getCurrentSkin(), MapSkin::getSkinByName, MapSkin::getName)
				.setSaveConsumer(skin -> {
					if (Minimap.isRound() && !skin.isRound() ||
						!Minimap.isRound() && !skin.isSquare()) {
						skin = MapSkin.getDefaultSkin();
					}
					JustMapClient.CONFIG.setInt("current_skin", skin.id);
				})
				.setDefaultValue(MapSkin::getDefaultSkin)
				.setSelections(MapSkin.getSkins())
				.build());
		mapAppearance.addEntry(entryBuilder.startDropdownMenu(lang("big_map_skin"), MapSkin.getBigMapSkin(), MapSkin::getSkinByName, MapSkin::getName)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setInt("big_map_skin", val.id))
				.setDefaultValue(MapSkin.getDefaultSquareSkin())
				.setSelections(MapSkin.getSquareSkins())
				.build());
		mapAppearance.addEntry(doubleSlider.build());
		mapAppearance.addEntry(arrowTypeEntry.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("simple_arrow"), JustMapClient.CONFIG.getBoolean("simple_direction_arrow"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("simple_direction_arrow", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("simple_direction_arrow"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("arrow_size"), JustMapClient.CONFIG.getInt("arrow_size"), 6, 16)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("arrow_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("arrow_size"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("worldmap_icon_size"), JustMapClient.CONFIG.getInt("worldmap_icon_size"), 8, 16)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("worldmap_icon_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("worldmap_icon_size"))
				.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("alternate_color_render"), JustMapClient.CONFIG.getBoolean("alternate_color_render"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("alternate_color_render", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("alternate_color_render"))
				.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("water_tint"), JustMapClient.CONFIG.getBoolean("water_tint"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("water_tint", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("water_tint"))
				.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("texture_filter"), JustMapClient.CONFIG.getBoolean("texture_filter"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("texture_filter", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("texture_filter"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("map_saturation"), JustMapClient.CONFIG.getInt("map_saturation"), -50, 50)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("map_saturation", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("map_saturation"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("map_brightness"), JustMapClient.CONFIG.getInt("map_brightness"), -50, 50)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("map_brightness", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("map_brightness"))
				.build());
		
		ConfigCategory waypoints = configBuilder.getOrCreateCategory(lang("category.waypoints"));
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("show_waypoints"), JustMapClient.CONFIG.getBoolean("show_waypoints"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_waypoints", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_waypoints"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("jump_to_waypoints"), JustMapClient.CONFIG.getBoolean("jump_to_waypoints"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("jump_to_waypoints", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("jump_to_waypoints"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("waypoints_tracking"), JustMapClient.CONFIG.getBoolean("waypoints_tracking"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("waypoints_tracking", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("waypoints_tracking"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("waypoints_render"), JustMapClient.CONFIG.getBoolean("waypoints_world_render"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("waypoints_world_render", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("waypoints_world_render"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_light_beam"), JustMapClient.CONFIG.getBoolean("render_light_beam"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("render_light_beam", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("render_light_beam"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_markers"), JustMapClient.CONFIG.getBoolean("render_markers"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("render_markers", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("render_markers"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_animation"), JustMapClient.CONFIG.getBoolean("render_animation"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("render_animation", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("render_animation"))
				.build());
		waypoints.addEntry(entryBuilder.startIntSlider(lang("min_render_dist"), JustMapClient.CONFIG.getInt("min_render_dist"), 1, 100)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("min_render_dist", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("min_render_dist"))
				.build());
		waypoints.addEntry(entryBuilder.startIntSlider(lang("max_render_dist"), JustMapClient.CONFIG.getInt("max_render_dist"), 10, 3000)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("max_render_dist", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("max_render_dist"))
				.build());
		
		ConfigCategory entityRadar = configBuilder.getOrCreateCategory(lang("category.entity_radar"));
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_entities"), JustMapClient.CONFIG.getBoolean("show_entities"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_entities", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_entities"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_entity_heads"), JustMapClient.CONFIG.getBoolean("show_entity_heads"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_entity_heads", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_entity_heads"))
				.build());
		entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_icon_size"), JustMapClient.CONFIG.getInt("entity_icon_size"), 2, 16)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("entity_icon_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("entity_icon_size"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_hostile"), JustMapClient.CONFIG.getBoolean("show_hostile"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_hostile", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_hostile"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_creatures"), JustMapClient.CONFIG.getBoolean("show_creatures"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_creatures", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_creatures"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_players"), JustMapClient.CONFIG.getBoolean("show_players"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_players", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_players"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_player_heads"), JustMapClient.CONFIG.getBoolean("show_player_heads"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_player_heads", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_player_heads"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_player_names"), JustMapClient.CONFIG.getBoolean("show_player_names"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_player_names", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_player_names"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_icons_outline"), JustMapClient.CONFIG.getBoolean("show_icons_outline"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_icons_outline", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_icons_outline"))
				.build());
		entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_outline_thickness"), JustMapClient.CONFIG.getInt("entity_outline_size"), 1, 5)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("entity_outline_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("entity_outline_size"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("render_entity_model"), JustMapClient.CONFIG.getBoolean("render_entity_model"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("render_entity_model", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("render_entity_model"))
				.build());
		entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_model_size"), JustMapClient.CONFIG.getInt("entity_model_size"), 2, 16)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("entity_model_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("entity_model_size"))
				.build());
		
		@SuppressWarnings("unchecked")
		EnumEntry<ScreenPosition> infoPosConfig = (EnumEntry<ScreenPosition>) JustMapClient.CONFIG.getEntry("info_position");
		EnumSelectorBuilder<ScreenPosition> infoPosEntry = entryBuilder.startEnumSelector(lang("info_position"), ScreenPosition.class, infoPosConfig.getValue());
		infoPosEntry.setSaveConsumer(val -> infoPosConfig.setValue(val))
					.setDefaultValue(infoPosConfig.getDefault());
		@SuppressWarnings("unchecked")
		EnumEntry<ScreenPosition> itemsPosConfig = (EnumEntry<ScreenPosition>) JustMapClient.CONFIG.getEntry("items_position");
		EnumSelectorBuilder<ScreenPosition> itemsPosEntry = entryBuilder.startEnumSelector(lang("equipment_position"), ScreenPosition.class, itemsPosConfig.getValue());
		itemsPosEntry.setSaveConsumer(val -> itemsPosConfig.setValue(val))
		.setDefaultValue(itemsPosConfig.getDefault());
		
		ConfigCategory mapInfo = configBuilder.getOrCreateCategory(lang("category.info"));
		mapInfo.addEntry(infoPosEntry.build());
		mapInfo.addEntry(itemsPosEntry.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("advanced_info"), JustMapClient.CONFIG.getBoolean("advanced_info"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("advanced_info", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("advanced_info"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("map_info"), JustMapClient.CONFIG.getBoolean("map_info"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("map_info", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("map_info"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_position"), JustMapClient.CONFIG.getBoolean("show_position"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_position", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_position"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_biome"), JustMapClient.CONFIG.getBoolean("show_biome"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_biome", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_biome"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_fps"), JustMapClient.CONFIG.getBoolean("show_FPS"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_FPS", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_FPS"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_game_time"), JustMapClient.CONFIG.getBoolean("show_time"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_time", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_time"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_light_level"), JustMapClient.CONFIG.getBoolean("show_light"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_light", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_light"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_equipment_info"), JustMapClient.CONFIG.getBoolean("show_items"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_items", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_items"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_mainhand"), JustMapClient.CONFIG.getBoolean("show_mainhand"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_mainhand", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_mainhand"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_offhand"), JustMapClient.CONFIG.getBoolean("show_offhand"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_offhand", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_offhand"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_head"), JustMapClient.CONFIG.getBoolean("show_head"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_head", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_head"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_chest"), JustMapClient.CONFIG.getBoolean("show_chest"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_chest", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_chest"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_legs"), JustMapClient.CONFIG.getBoolean("show_legs"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_legs", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_legs"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_feet"), JustMapClient.CONFIG.getBoolean("show_feet"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_feet", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_feet"))
				.build());
	
		ConfigCategory optimization = configBuilder.getOrCreateCategory(lang("category.optimization"));
		optimization.addEntry(entryBuilder.startIntField(lang("chunk_update_interval"), JustMapClient.CONFIG.getInt("chunk_update_interval"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("chunk_update_interval", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("chunk_update_interval"))
				.setMin(500).setMax(5000).build());
		optimization.addEntry(entryBuilder.startIntField(lang("chunk_level_update_interval"), JustMapClient.CONFIG.getInt("chunk_level_update_interval"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("chunk_level_update_interval", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("chunk_level_update_interval"))
				.setMin(500).setMax(10000).build());
		optimization.addEntry(entryBuilder.startIntField(lang("purge_delay"), JustMapClient.CONFIG.getInt("purge_delay"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("purge_delay", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("purge_delay"))
				.setMin(10).setMax(600).build());
		optimization.addEntry(entryBuilder.startIntField(lang("purge_amount"), JustMapClient.CONFIG.getInt("purge_amount"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("purge_amount", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("purge_amount"))
				.setMin(100).setMax(5000).build());
		optimization.addEntry(entryBuilder.startBooleanToggle(lang("uninterrupted_map_update"), JustMapClient.CONFIG.getBoolean("force_map_update"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("force_map_update", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("force_map_update"))
				.build());
		optimization.addEntry(entryBuilder.startBooleanToggle(lang("generate_new_chunks"), JustMapClient.CONFIG.getBoolean("generate_new_chunks"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("generate_new_chunks", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("generate_new_chunks"))
				.build());
		
		configBuilder.setDoesConfirmSave(false);
		configBuilder.transparentBackground();
		configBuilder.setSavingRunnable(JustMapClient.CONFIG::saveChanges);
		
		return configBuilder;
	}
	
	private static int getIntValue(String value, int defVal) {
		try {
			return Integer.valueOf(value);
		} catch (NumberFormatException ex) {
			return defVal;
		}
	}
}
