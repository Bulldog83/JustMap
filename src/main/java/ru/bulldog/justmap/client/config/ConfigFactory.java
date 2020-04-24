package ru.bulldog.justmap.client.config;

import java.util.Optional;

import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder;

import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableText;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.map.minimap.MapPosition;
import ru.bulldog.justmap.map.minimap.MapSkin;

public final class ConfigFactory {
	
	private static Text lang(String key) {
		return new TranslatableText("justmap.configuration." + key);
	}
	
	private static Optional<Text[]> getTooltip(String tooltip, boolean condition) {
		return condition ? Optional.empty() : Optional.ofNullable(new Text[] {
			new LiteralText(I18n.translate("justmap.configuration." + tooltip))
		});
	}
	
	private static ConfigBuilder configBuilder;
	
	public static Screen getConfigScreen(Screen parent) {
		if (configBuilder == null) {
			createConfigBuilder();
		}		
		configBuilder.setParentScreen(parent);
		
		return configBuilder.build();
	}
	
	private static void createConfigBuilder() {
		configBuilder = ConfigBuilder.create().setTitle(new LiteralText("Just Map Configuration"));
		ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
		
		ConfigCategory general = configBuilder.getOrCreateCategory(lang("category.general"));
		
		@SuppressWarnings("unchecked")
		EnumEntry<MapPosition> drawPosConfig = (EnumEntry<MapPosition>) JustMapClient.CONFIG.getEntry("map_position");
		
		EnumSelectorBuilder<MapPosition> drawPosEntry = entryBuilder.startEnumSelector(lang("map_position"), MapPosition.class, drawPosConfig.getValue());
		drawPosEntry.setSaveConsumer(val -> drawPosConfig.setValue(val))
					.setDefaultValue(drawPosConfig.getDefault());
		general.addEntry(drawPosEntry.build());
		general.addEntry(entryBuilder.startIntField(lang("map_offset"), JustMapClient.CONFIG.getInt("map_offset"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setInt("map_offset", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("map_offset"))
				.build());			
		general.addEntry(entryBuilder.startIntField(lang("map_size"), JustMapClient.CONFIG.getInt("map_size"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("map_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("map_size"))
				.setMin(32).setMax(480).build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_in_chat"), JustMapClient.CONFIG.getBoolean("show_in_chat"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_in_chat", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_in_chat"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_caves"), JustMapClient.CONFIG.getBoolean("show_caves"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_caves", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_caves"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("ignore_plants"), JustMapClient.CONFIG.getBoolean("ignore_plants"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("ignore_plants", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("ignore_plants"))
				.build());
		general.addEntry(entryBuilder.startBooleanToggle(lang("show_terrain"), JustMapClient.CONFIG.getBoolean("show_terrain"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("show_terrain", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("show_terrain"))
				.build());
		general.addEntry(entryBuilder.startIntSlider(lang("terrain_strength"), JustMapClient.CONFIG.getInt("terrain_strength"), 2, 9)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("terrain_strength", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("terrain_strength"))
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
		
		ConfigCategory mapAppearance = configBuilder.getOrCreateCategory(lang("category.appearance"));
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("alternate_color_render"), JustMapClient.CONFIG.getBoolean("alternate_color_render"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("alternate_color_render", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("alternate_color_render"))
				.build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("map_saturation"), JustMapClient.CONFIG.getInt("map_saturation"), -50, 50)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("map_saturation", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("map_saturation"))
				.setTooltipSupplier(() -> {
					return getTooltip("tooltip_color_config", JustMapClient.CONFIG.getBoolean("alternate_color_render"));
				}).build());
		mapAppearance.addEntry(entryBuilder.startIntSlider(lang("map_brightness"), JustMapClient.CONFIG.getInt("map_brightness"), -50, 50)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("map_brightness", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("map_brightness"))
				.setTooltipSupplier(() -> {
					return getTooltip("tooltip_color_config", JustMapClient.CONFIG.getBoolean("alternate_color_render"));
				}).build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("use_skins"), JustMapClient.CONFIG.getBoolean("use_skins"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("use_skins", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("use_skins"))
				.build());
		mapAppearance.addEntry(entryBuilder.startDropdownMenu(lang("current_skin"), MapSkin.getCurrentSkin(), MapSkin::getSkinByName, MapSkin::getName)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setInt("current_skin", val.id))
				.setDefaultValue(MapSkin.getSkin((int) JustMapClient.CONFIG.getDefault("current_skin")))
				.setSelections(MapSkin.getSkins())
				.build());
		mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("simple_arrow"), JustMapClient.CONFIG.getBoolean("simple_direction_arrow"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("simple_direction_arrow", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("simple_direction_arrow"))
				.build());
		
		ConfigCategory waypoints = configBuilder.getOrCreateCategory(lang("category.waypoints"));
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
		entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("render_entity_model"), JustMapClient.CONFIG.getBoolean("render_entity_model"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("render_entity_model", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("render_entity_model"))
				.build());
		entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_model_size"), JustMapClient.CONFIG.getInt("entity_model_size"), 2, 16)
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("entity_model_size", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("entity_model_size"))
				.build());
		
		ConfigCategory mapInfo = configBuilder.getOrCreateCategory(lang("category.info"));
		mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_grid"), JustMapClient.CONFIG.getBoolean("draw_chunk_grid"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setBoolean("draw_chunk_grid", val))
				.setDefaultValue((boolean) JustMapClient.CONFIG.getDefault("draw_chunk_grid"))
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
	
		ConfigCategory optimization = configBuilder.getOrCreateCategory(lang("category.optimization"));
		optimization.addEntry(entryBuilder.startIntField(lang("chunk_update_interval"), JustMapClient.CONFIG.getInt("chunk_update_interval"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("chunk_update_interval", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("chunk_update_interval"))
				.setMin(500).setMax(5000).build());
		optimization.addEntry(entryBuilder.startIntField(lang("chunk_level_update_interval"), JustMapClient.CONFIG.getInt("chunk_level_update_interval"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("chunk_level_update_interval", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("chunk_level_update_interval"))
				.setMin(500).setMax(10000).build());
		optimization.addEntry(entryBuilder.startIntField(lang("update_cycle"), JustMapClient.CONFIG.getInt("update_per_cycle"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("update_per_cycle", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("update_per_cycle"))
				.setMin(1).setMax(1000).build());
		optimization.addEntry(entryBuilder.startIntField(lang("purge_delay"), JustMapClient.CONFIG.getInt("purge_delay"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("purge_delay", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("purge_delay"))
				.setMin(10).setMax(600).build());
		optimization.addEntry(entryBuilder.startIntField(lang("purge_amount"), JustMapClient.CONFIG.getInt("purge_amount"))
				.setSaveConsumer(val -> JustMapClient.CONFIG.setRanged("purge_amount", val))
				.setDefaultValue((int) JustMapClient.CONFIG.getDefault("purge_amount"))
				.setMin(100).setMax(5000).build());
		
		configBuilder.setDoesConfirmSave(false);
		configBuilder.setTransparentBackground(true);
		configBuilder.setSavingRunnable(JustMapClient.CONFIG::saveChanges);
	}
}
