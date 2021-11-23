package ru.bulldog.justmap.client.config;

import java.util.Arrays;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.enums.ArrowType;
import ru.bulldog.justmap.enums.MapShape;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.util.LangUtil;

public final class ConfigFactory {

	private final static ClientConfig modConfig = JustMapClient.getConfig();

	private static Text lang(String key) {
		return LangUtil.getText("configuration", key);
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

		EnumEntry<ScreenPosition> drawPosConfig = modConfig.getEntry("map_position");
		EnumSelectorBuilder<ScreenPosition> drawPosEntry = entryBuilder.startEnumSelector(lang("map_position"), ScreenPosition.class, drawPosConfig.getValue());
		drawPosEntry.setSaveConsumer(drawPosConfig::setValue)
					.setDefaultValue(drawPosConfig.getDefault());
		EnumEntry<MultiworldDetection> mwDetectConfig = modConfig.getEntry("multiworld_detection");
		EnumSelectorBuilder<MultiworldDetection> mwDetectEntry = entryBuilder.startEnumSelector(lang("multiworld_detection_type"), MultiworldDetection.class, mwDetectConfig.getValue());
		mwDetectEntry.setSaveConsumer(mwDetectConfig::setValue)
					 .setDefaultValue(mwDetectConfig.getDefault());

		MinecraftClient minecraft = MinecraftClient.getInstance();
		int offset = modConfig.getInt("map_offset");
		int maxX = minecraft.getWindow().getScaledWidth();
		int maxY = minecraft.getWindow().getScaledHeight();

		general.addEntry(drawPosEntry.build());
		general.addEntry(entryBuilder.startIntField(lang("map_offset"), offset)
				.setSaveConsumer(val -> modConfig.setInt("map_offset", val))
				.setDefaultValue((int) modConfig.getDefault("map_offset"))
				.build());
		general.addEntry(entryBuilder.startIntSlider(lang("map_position_x"), modConfig.getInt("map_position_x"), offset, maxX)
				.setSaveConsumer(val -> modConfig.setInt("map_position_x", val))
				.setDefaultValue((int) modConfig.getDefault("map_position_x"))
				.build());
		general.addEntry(entryBuilder.startIntSlider(lang("map_position_y"), modConfig.getInt("map_position_y"), offset, maxY)
				.setSaveConsumer(val -> modConfig.setInt("map_position_y", val))
				.setDefaultValue((int) modConfig.getDefault("map_position_y"))
				.build());
		general.addEntry(entryBuilder.startDropdownMenu(lang("map_size"), modConfig.getInt("map_size"), (val) -> getIntValue(val, modConfig.getDefault("map_size")))
				.setSaveConsumer(val -> modConfig.setRanged("map_size", val))
				.setDefaultValue((int) modConfig.getDefault("map_size"))
				.setSelections(Arrays.asList(32, 64, 96, 128, 160, 192, 224, 256))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_big_map"), modConfig.getBoolean("show_big_map"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_big_map", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_big_map"))
				.build());
		general.addEntry(entryBuilder.startDropdownMenu(lang("big_map_size"), modConfig.getInt("big_map_size"), (val) -> getIntValue(val, modConfig.getDefault("big_map_size")))
				.setSaveConsumer(val -> modConfig.setRanged("big_map_size", val))
				.setDefaultValue((int) modConfig.getDefault("big_map_size"))
				.setSelections(Arrays.asList(256, 272, 288, 304, 320, 336, 352, 368, 384, 400))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_in_chat"), modConfig.getBoolean("show_in_chat"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_in_chat", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_in_chat"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("move_effects"), modConfig.getBoolean("move_effects"))
				.setSaveConsumer(val -> modConfig.setBoolean("move_effects", val))
				.setDefaultValue((boolean) modConfig.getDefault("move_effects"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_effect_timers"), modConfig.getBoolean("show_effect_timers"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_effect_timers", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_effect_timers"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("rotate_map"), modConfig.getBoolean("rotate_map"))
				.setSaveConsumer(val -> modConfig.setBoolean("rotate_map", val))
				.setDefaultValue((boolean) modConfig.getDefault("rotate_map"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("detect_multiworlds"), modConfig.getBoolean("detect_multiworlds"))
				.setSaveConsumer(val -> modConfig.setBoolean("detect_multiworlds", val))
				.setDefaultValue((boolean) modConfig.getDefault("detect_multiworlds"))
				.build());
		general.addEntry(mwDetectEntry.build());

		ConfigCategory mapDetails = configBuilder.getOrCreateCategory(lang("category.details"));
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_caves"), modConfig.getBoolean("show_caves"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_caves", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_caves"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_terrain"), modConfig.getBoolean("show_terrain"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_terrain", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_terrain"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_topography"), modConfig.getBoolean("show_topography"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_topography", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_topography"))
				.build());
		mapDetails.addEntry(entryBuilder.startIntSlider(lang("terrain_strength"), modConfig.getInt("terrain_strength"), 2, 9)
				.setSaveConsumer(val -> modConfig.setRanged("terrain_strength", val))
				.setDefaultValue((int) modConfig.getDefault("terrain_strength"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_grid"), modConfig.getBoolean("draw_chunk_grid"))
				.setSaveConsumer(val -> modConfig.setBoolean("draw_chunk_grid", val))
				.setDefaultValue((boolean) modConfig.getDefault("draw_chunk_grid"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_worldmap_grid"), modConfig.getBoolean("draw_worldmap_grid"))
				.setSaveConsumer(val -> modConfig.setBoolean("draw_worldmap_grid", val))
				.setDefaultValue((boolean) modConfig.getDefault("draw_worldmap_grid"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_slime_chunks"), modConfig.getBoolean("show_slime"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_slime", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_slime"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("show_loaded_chunks"), modConfig.getBoolean("show_loaded_chunks"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_loaded_chunks", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_loaded_chunks"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("hide_plants"), modConfig.getBoolean("hide_plants"))
				.setSaveConsumer(val -> modConfig.setBoolean("hide_plants", val))
				.setDefaultValue((boolean) modConfig.getDefault("hide_plants"))
				.build());
		mapDetails.addEntry(entryBuilder.startBooleanToggle(lang("hide_water"), modConfig.getBoolean("hide_water"))
				.setSaveConsumer(val -> modConfig.setBoolean("hide_water", val))
				.setDefaultValue((boolean) modConfig.getDefault("hide_water"))
				.build());

		EnumEntry<ArrowType> arrowTypeConfig = modConfig.getEntry("arrow_type");
		EnumSelectorBuilder<ArrowType> arrowTypeEntry = entryBuilder.startEnumSelector(lang("arrow_type"), ArrowType.class, arrowTypeConfig.getValue());
		arrowTypeEntry.setSaveConsumer(arrowTypeConfig::setValue)
					  .setDefaultValue(arrowTypeConfig.getDefault());
		EnumEntry<MapShape> mapShapeConfig = modConfig.getEntry("map_shape");
		EnumSelectorBuilder<MapShape> mapShapeEntry = entryBuilder.startEnumSelector(lang("map_shape"), MapShape.class, mapShapeConfig.getValue());
		mapShapeEntry.setSaveConsumer(mapShapeConfig::setValue)
					 .setDefaultValue(mapShapeConfig.getDefault());
		FloatSliderBuilder doubleSlider = new FloatSliderBuilder(entryBuilder.getResetButtonKey(), lang("skin_border_scale"), modConfig.getFloat("skin_scale"), 0.5F, 3.0F)
				.setSaveConsumer(val -> modConfig.setRanged("skin_scale", val))
				.setDefaultValue((float) modConfig.getDefault("skin_scale"));

		ConfigCategory mapAppearance = configBuilder.getOrCreateCategory(lang("category.appearance"));
		mapAppearance.addEntry(mapShapeEntry.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("use_skins"), modConfig.getBoolean("use_skins"))
				.setSaveConsumer(val -> modConfig.setBoolean("use_skins", val))
				.setDefaultValue((boolean) modConfig.getDefault("use_skins"))
				.build());
		mapAppearance.addEntry(entryBuilder.startDropdownMenu(lang("current_skin"), MapSkin.getCurrentSkin(), MapSkin::getSkinByName, MapSkin::getName)
				.setSaveConsumer(skin -> {
					if (Minimap.isRound() && !skin.isRound() ||
						!Minimap.isRound() && !skin.isSquare()) {
						skin = MapSkin.getDefaultSkin();
					}
					modConfig.setInt("current_skin", skin.id);
				})
				.setDefaultValue(MapSkin::getDefaultSkin)
				.setSelections(MapSkin.getSkins())
				.build());
		mapAppearance.addEntry(entryBuilder.startDropdownMenu(lang("big_map_skin"), MapSkin.getBigMapSkin(), MapSkin::getSkinByName, MapSkin::getName)
				.setSaveConsumer(val -> modConfig.setInt("big_map_skin", val.id))
				.setDefaultValue(MapSkin.getDefaultSquareSkin())
				.setSelections(MapSkin.getSquareSkins())
				.build());
		mapAppearance.addEntry(doubleSlider.build());
		mapAppearance.addEntry(arrowTypeEntry.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("simple_arrow"), modConfig.getBoolean("simple_direction_arrow"))
				.setSaveConsumer(val -> modConfig.setBoolean("simple_direction_arrow", val))
				.setDefaultValue((boolean) modConfig.getDefault("simple_direction_arrow"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("arrow_size"), modConfig.getInt("arrow_size"), 6, 16)
				.setSaveConsumer(val -> modConfig.setRanged("arrow_size", val))
				.setDefaultValue((int) modConfig.getDefault("arrow_size"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("worldmap_icon_size"), modConfig.getInt("worldmap_icon_size"), 8, 16)
				.setSaveConsumer(val -> modConfig.setRanged("worldmap_icon_size", val))
				.setDefaultValue((int) modConfig.getDefault("worldmap_icon_size"))
				.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("alternate_color_render"), modConfig.getBoolean("alternate_color_render"))
				.setSaveConsumer(val -> modConfig.setBoolean("alternate_color_render", val))
				.setDefaultValue((boolean) modConfig.getDefault("alternate_color_render"))
				.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("water_tint"), modConfig.getBoolean("water_tint"))
				.setSaveConsumer(val -> modConfig.setBoolean("water_tint", val))
				.setDefaultValue((boolean) modConfig.getDefault("water_tint"))
				.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("texture_filter"), modConfig.getBoolean("texture_filter"))
				.setSaveConsumer(val -> modConfig.setBoolean("texture_filter", val))
				.setDefaultValue((boolean) modConfig.getDefault("texture_filter"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("map_saturation"), modConfig.getInt("map_saturation"), -50, 50)
				.setSaveConsumer(val -> modConfig.setRanged("map_saturation", val))
				.setDefaultValue((int) modConfig.getDefault("map_saturation"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("map_brightness"), modConfig.getInt("map_brightness"), -50, 50)
				.setSaveConsumer(val -> modConfig.setRanged("map_brightness", val))
				.setDefaultValue((int) modConfig.getDefault("map_brightness"))
				.build());

		ConfigCategory waypoints = configBuilder.getOrCreateCategory(lang("category.waypoints"));
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("show_waypoints"), modConfig.getBoolean("show_waypoints"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_waypoints", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_waypoints"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("jump_to_waypoints"), modConfig.getBoolean("jump_to_waypoints"))
				.setSaveConsumer(val -> modConfig.setBoolean("jump_to_waypoints", val))
				.setDefaultValue((boolean) modConfig.getDefault("jump_to_waypoints"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("waypoints_tracking"), modConfig.getBoolean("waypoints_tracking"))
				.setSaveConsumer(val -> modConfig.setBoolean("waypoints_tracking", val))
				.setDefaultValue((boolean) modConfig.getDefault("waypoints_tracking"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("waypoints_render"), modConfig.getBoolean("waypoints_world_render"))
				.setSaveConsumer(val -> modConfig.setBoolean("waypoints_world_render", val))
				.setDefaultValue((boolean) modConfig.getDefault("waypoints_world_render"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_light_beam"), modConfig.getBoolean("render_light_beam"))
				.setSaveConsumer(val -> modConfig.setBoolean("render_light_beam", val))
				.setDefaultValue((boolean) modConfig.getDefault("render_light_beam"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_markers"), modConfig.getBoolean("render_markers"))
				.setSaveConsumer(val -> modConfig.setBoolean("render_markers", val))
				.setDefaultValue((boolean) modConfig.getDefault("render_markers"))
				.build());
		waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_animation"), modConfig.getBoolean("render_animation"))
				.setSaveConsumer(val -> modConfig.setBoolean("render_animation", val))
				.setDefaultValue((boolean) modConfig.getDefault("render_animation"))
				.build());
		waypoints.addEntry(entryBuilder.startIntSlider(lang("min_render_dist"), modConfig.getInt("min_render_dist"), 1, 100)
				.setSaveConsumer(val -> modConfig.setRanged("min_render_dist", val))
				.setDefaultValue((int) modConfig.getDefault("min_render_dist"))
				.build());
		waypoints.addEntry(entryBuilder.startIntSlider(lang("max_render_dist"), modConfig.getInt("max_render_dist"), 10, 3000)
				.setSaveConsumer(val -> modConfig.setRanged("max_render_dist", val))
				.setDefaultValue((int) modConfig.getDefault("max_render_dist"))
				.build());

		ConfigCategory entityRadar = configBuilder.getOrCreateCategory(lang("category.entity_radar"));
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_entities"), modConfig.getBoolean("show_entities"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_entities", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_entities"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_entity_heads"), modConfig.getBoolean("show_entity_heads"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_entity_heads", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_entity_heads"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("icons_shading"), modConfig.getBoolean("map_icons_shading"))
				.setSaveConsumer(val -> modConfig.setBoolean("map_icons_shading", val))
				.setDefaultValue((boolean) modConfig.getDefault("map_icons_shading"))
				.build());
		entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_icon_size"), modConfig.getInt("entity_icon_size"), 2, 16)
				.setSaveConsumer(val -> modConfig.setRanged("entity_icon_size", val))
				.setDefaultValue((int) modConfig.getDefault("entity_icon_size"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_hostile"), modConfig.getBoolean("show_hostile"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_hostile", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_hostile"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_creatures"), modConfig.getBoolean("show_creatures"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_creatures", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_creatures"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_players"), modConfig.getBoolean("show_players"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_players", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_players"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_player_heads"), modConfig.getBoolean("show_player_heads"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_player_heads", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_player_heads"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_player_names"), modConfig.getBoolean("show_player_names"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_player_names", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_player_names"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_icons_outline"), modConfig.getBoolean("show_icons_outline"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_icons_outline", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_icons_outline"))
				.build());
		entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_outline_thickness"), modConfig.getInt("entity_outline_size"), 1, 5)
				.setSaveConsumer(val -> modConfig.setRanged("entity_outline_size", val))
				.setDefaultValue((int) modConfig.getDefault("entity_outline_size"))
				.build());
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("render_entity_model"), modConfig.getBoolean("render_entity_model"))
				.setSaveConsumer(val -> modConfig.setBoolean("render_entity_model", val))
				.setDefaultValue((boolean) modConfig.getDefault("render_entity_model"))
				.build());
		entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_model_size"), modConfig.getInt("entity_model_size"), 2, 16)
				.setSaveConsumer(val -> modConfig.setRanged("entity_model_size", val))
				.setDefaultValue((int) modConfig.getDefault("entity_model_size"))
				.build());

		EnumEntry<ScreenPosition> infoPosConfig = modConfig.getEntry("info_position");
		EnumSelectorBuilder<ScreenPosition> infoPosEntry = entryBuilder.startEnumSelector(lang("info_position"), ScreenPosition.class, infoPosConfig.getValue());
		infoPosEntry.setSaveConsumer(val -> {
					if (val == ScreenPosition.USER_DEFINED) {
						val = infoPosConfig.getDefault();
					}
					infoPosConfig.setValue(val);
				})
				.setDefaultValue(infoPosConfig.getDefault());
		EnumEntry<ScreenPosition> itemsPosConfig = modConfig.getEntry("items_position");
		EnumSelectorBuilder<ScreenPosition> itemsPosEntry = entryBuilder.startEnumSelector(lang("equipment_position"), ScreenPosition.class, itemsPosConfig.getValue());
		itemsPosEntry.setSaveConsumer(val -> {
			if (val == ScreenPosition.USER_DEFINED) {
				val = itemsPosConfig.getDefault();
			}
			itemsPosConfig.setValue(val);
		})
		.setDefaultValue(itemsPosConfig.getDefault());

		ConfigCategory mapInfo = configBuilder.getOrCreateCategory(lang("category.info"));
		mapInfo.addEntry(infoPosEntry.build());
		mapInfo.addEntry(itemsPosEntry.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("advanced_info"), modConfig.getBoolean("advanced_info"))
				.setSaveConsumer(val -> modConfig.setBoolean("advanced_info", val))
				.setDefaultValue((boolean) modConfig.getDefault("advanced_info"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("map_info"), modConfig.getBoolean("map_info"))
				.setSaveConsumer(val -> modConfig.setBoolean("map_info", val))
				.setDefaultValue((boolean) modConfig.getDefault("map_info"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_position"), modConfig.getBoolean("show_position"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_position", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_position"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_biome"), modConfig.getBoolean("show_biome"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_biome", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_biome"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_fps"), modConfig.getBoolean("show_FPS"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_FPS", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_FPS"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_game_time"), modConfig.getBoolean("show_time"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_time", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_time"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_light_level"), modConfig.getBoolean("show_light"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_light", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_light"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_equipment_info"), modConfig.getBoolean("show_items"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_items", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_items"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_mainhand"), modConfig.getBoolean("show_mainhand"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_mainhand", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_mainhand"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_offhand"), modConfig.getBoolean("show_offhand"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_offhand", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_offhand"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_head"), modConfig.getBoolean("show_head"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_head", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_head"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_chest"), modConfig.getBoolean("show_chest"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_chest", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_chest"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_legs"), modConfig.getBoolean("show_legs"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_legs", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_legs"))
				.build());
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_feet"), modConfig.getBoolean("show_feet"))
				.setSaveConsumer(val -> modConfig.setBoolean("show_feet", val))
				.setDefaultValue((boolean) modConfig.getDefault("show_feet"))
				.build());

		ConfigCategory optimization = configBuilder.getOrCreateCategory(lang("category.optimization"));
		optimization.addEntry(entryBuilder.startIntField(lang("chunk_update_interval"), modConfig.getInt("chunk_update_interval"))
				.setSaveConsumer(val -> modConfig.setRanged("chunk_update_interval", val))
				.setDefaultValue((int) modConfig.getDefault("chunk_update_interval"))
				.setMin(500).setMax(5000).build());
		optimization.addEntry(entryBuilder.startIntField(lang("chunk_level_update_interval"), modConfig.getInt("chunk_level_update_interval"))
				.setSaveConsumer(val -> modConfig.setRanged("chunk_level_update_interval", val))
				.setDefaultValue((int) modConfig.getDefault("chunk_level_update_interval"))
				.setMin(500).setMax(10000).build());
		optimization.addEntry(entryBuilder.startIntField(lang("purge_delay"), modConfig.getInt("purge_delay"))
				.setSaveConsumer(val -> modConfig.setRanged("purge_delay", val))
				.setDefaultValue((int) modConfig.getDefault("purge_delay"))
				.setMin(10).setMax(600).build());
		optimization.addEntry(entryBuilder.startIntField(lang("purge_amount"), modConfig.getInt("purge_amount"))
				.setSaveConsumer(val -> modConfig.setRanged("purge_amount", val))
				.setDefaultValue((int) modConfig.getDefault("purge_amount"))
				.setMin(100).setMax(5000).build());
		optimization.addEntry(entryBuilder.startBooleanToggle(lang("uninterrupted_map_update"), modConfig.getBoolean("force_map_update"))
				.setSaveConsumer(val -> modConfig.setBoolean("force_map_update", val))
				.setDefaultValue((boolean) modConfig.getDefault("force_map_update"))
				.build());
		optimization.addEntry(entryBuilder.startBooleanToggle(lang("use_fast_render"), modConfig.getBoolean("use_fast_render"))
				.setSaveConsumer(val -> modConfig.setBoolean("use_fast_render", val))
				.setDefaultValue((boolean) modConfig.getDefault("use_fast_render"))
				.build());

		configBuilder.setDoesConfirmSave(false);
		configBuilder.transparentBackground();
		configBuilder.setSavingRunnable(modConfig::saveChanges);

		return configBuilder;
	}

	private static int getIntValue(String value, int defVal) {
		try {
			return Integer.parseInt(value);
		} catch (NumberFormatException ex) {
			return defVal;
		}
	}
}
