package ru.bulldog.justmap.config;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.minimap.MapPosition;
import ru.bulldog.justmap.minimap.MapSkin;
import io.github.prospector.modmenu.api.ModMenuApi;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.clothconfig2.impl.builders.EnumSelectorBuilder;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.resource.language.I18n;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

import java.util.Optional;
import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class ModMenuEntry implements ModMenuApi {
	
	@Override
	public String getModId() {
		return JustMap.MODID;
	}
	
	private String lang(String key) {
		return I18n.translate("justmap.configuration." + key);
	}
	
	private Optional<String[]> getTooltip(String tooltip, boolean condition) {
		return condition ? Optional.empty() : Optional.ofNullable(new String[] {
			tooltip
		});
	}
	
	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return (parent) -> {
			ConfigBuilder builder = ConfigBuilder.create().setParentScreen(parent).setTitle("Just Map Configuration");
			ConfigEntryBuilder entryBuilder = ConfigEntryBuilder.create();
			
			ConfigCategory general = builder.getOrCreateCategory(lang("category.general"));
			
			@SuppressWarnings("unchecked")
			EnumEntry<MapPosition> drawPosConfig = (EnumEntry<MapPosition>) JustMap.CONFIG.getEntry("map_position");			
			
			EnumSelectorBuilder<MapPosition> drawPosEntry = entryBuilder.startEnumSelector(lang("map_position"), MapPosition.class, drawPosConfig.getValue());
			drawPosEntry.setSaveConsumer(val -> drawPosConfig.setValue(val))
						.setDefaultValue(drawPosConfig.getDefault());
			general.addEntry(drawPosEntry.build());
			general.addEntry(entryBuilder.startIntField(lang("map_offset"), JustMap.CONFIG.getInt("map_offset"))
					.setSaveConsumer(val -> JustMap.CONFIG.setInt("map_offset", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("map_offset"))
					.build());			
			general.addEntry(entryBuilder.startIntField(lang("map_size"), JustMap.CONFIG.getInt("map_size"))
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("map_size", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("map_size"))
					.setMin(32).setMax(480).build());
			general.addEntry(entryBuilder.startBooleanToggle(lang("show_in_chat"), JustMap.CONFIG.getBoolean("show_in_chat"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_in_chat", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_in_chat"))
					.build());
			general.addEntry(entryBuilder.startBooleanToggle(lang("show_caves"), JustMap.CONFIG.getBoolean("show_caves"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_caves", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_caves"))
					.build());
			general.addEntry(entryBuilder.startBooleanToggle(lang("show_terrain"), JustMap.CONFIG.getBoolean("show_terrain"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_terrain", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_terrain"))
					.build());
			general.addEntry(entryBuilder.startIntSlider(lang("terrain_strength"), JustMap.CONFIG.getInt("terrain_strength"), 2, 9)
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("terrain_strength", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("terrain_strength"))
					.build());
			general.addEntry(entryBuilder.startBooleanToggle(lang("move_effects"), JustMap.CONFIG.getBoolean("move_effects"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("move_effects", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("move_effects"))
					.build());
			general.addEntry(entryBuilder.startBooleanToggle(lang("show_effect_timers"), JustMap.CONFIG.getBoolean("show_effect_timers"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_effect_timers", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_effect_timers"))
					.build());
			
			ConfigCategory mapAppearance = builder.getOrCreateCategory(lang("category.appearance"));
			mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("alternate_color_render"), JustMap.CONFIG.getBoolean("alternate_color_render"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("alternate_color_render", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("alternate_color_render"))
					.build());
			mapAppearance.addEntry(entryBuilder.startIntSlider(lang("map_saturation"), JustMap.CONFIG.getInt("map_saturation"), -50, 50)
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("map_saturation", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("map_saturation"))
					.setTooltipSupplier(() -> {
						return getTooltip(lang("tooltip_color_config"), JustMap.CONFIG.getBoolean("alternate_color_render"));
					}).build());
			mapAppearance.addEntry(entryBuilder.startIntSlider(lang("map_brightness"), JustMap.CONFIG.getInt("map_brightness"), -50, 50)
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("map_brightness", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("map_brightness"))
					.setTooltipSupplier(() -> {
						return getTooltip(lang("tooltip_color_config"), JustMap.CONFIG.getBoolean("alternate_color_render"));
					}).build());
			mapAppearance.addEntry(entryBuilder.startBooleanToggle(lang("use_skins"), JustMap.CONFIG.getBoolean("use_skins"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("use_skins", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("use_skins"))
					.build());
			mapAppearance.addEntry(entryBuilder.startDropdownMenu(lang("current_skin"), MapSkin.getCurrentSkin(), MapSkin::getSkinByName, MapSkin::getName)
					.setSaveConsumer(val -> JustMap.CONFIG.setInt("current_skin", val.id))
					.setDefaultValue(MapSkin.getSkin((int) JustMap.CONFIG.getDefault("current_skin")))
					.setSelections(MapSkin.getSkins())
					.build());
			mapAppearance.addEntry(entryBuilder.startIntSlider(lang("current_skin"), JustMap.CONFIG.getInt("current_skin") + 1, 1, MapSkin.getSkins().size())
					.setSaveConsumer(val -> JustMap.CONFIG.setInt("current_skin", val - 1))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("current_skin") + 1)
					.build());
			
			ConfigCategory waypoints = builder.getOrCreateCategory(lang("category.waypoints"));
			waypoints.addEntry(entryBuilder.startBooleanToggle(lang("waypoints_tracking"), JustMap.CONFIG.getBoolean("waypoints_tracking"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("waypoints_tracking", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("waypoints_tracking"))
					.build());
			waypoints.addEntry(entryBuilder.startBooleanToggle(lang("waypoints_render"), JustMap.CONFIG.getBoolean("waypoints_world_render"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("waypoints_world_render", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("waypoints_world_render"))
					.build());
			waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_light_beam"), JustMap.CONFIG.getBoolean("render_light_beam"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("render_light_beam", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("render_light_beam"))
					.build());
			waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_markers"), JustMap.CONFIG.getBoolean("render_markers"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("render_markers", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("render_markers"))
					.build());
			waypoints.addEntry(entryBuilder.startBooleanToggle(lang("render_animation"), JustMap.CONFIG.getBoolean("render_animation"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("render_animation", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("render_animation"))
					.build());
			waypoints.addEntry(entryBuilder.startIntSlider(lang("min_render_dist"), JustMap.CONFIG.getInt("min_render_dist"), 1, 100)
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("min_render_dist", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("min_render_dist"))
					.build());
			waypoints.addEntry(entryBuilder.startIntSlider(lang("max_render_dist"), JustMap.CONFIG.getInt("max_render_dist"), 10, 3000)
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("max_render_dist", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("max_render_dist"))
					.build());
			
			ConfigCategory entityRadar = builder.getOrCreateCategory(lang("category.entity_radar"));
			entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_entities"), JustMap.CONFIG.getBoolean("show_entities"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_entities", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_entities"))
					.build());
			entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_entity_heads"), JustMap.CONFIG.getBoolean("show_entity_heads"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_entity_heads", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_entity_heads"))
					.build());
			entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_icon_size"), JustMap.CONFIG.getInt("entity_icon_size"), 2, 16)
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("entity_icon_size", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("entity_icon_size"))
					.build());
			entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_hostile"), JustMap.CONFIG.getBoolean("show_hostile"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_hostile", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_hostile"))
					.build());
			entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_creatures"), JustMap.CONFIG.getBoolean("show_creatures"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_creatures", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_creatures"))
					.build());
			entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_players"), JustMap.CONFIG.getBoolean("show_players"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_players", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_players"))
					.build());
			entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_player_heads"), JustMap.CONFIG.getBoolean("show_player_heads"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_player_heads", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_player_heads"))
					.build());
			entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("show_player_names"), JustMap.CONFIG.getBoolean("show_player_names"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_player_names", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_player_names"))
					.build());
			entityRadar.addEntry(entryBuilder.startBooleanToggle(lang("render_entity_model"), JustMap.CONFIG.getBoolean("render_entity_model"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("render_entity_model", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("render_entity_model"))
					.build());
			entityRadar.addEntry(entryBuilder.startIntSlider(lang("entity_model_size"), JustMap.CONFIG.getInt("entity_model_size"), 2, 16)
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("entity_model_size", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("entity_model_size"))
					.build());
			
			ConfigCategory mapInfo = builder.getOrCreateCategory(lang("category.info"));
			mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_grid"), JustMap.CONFIG.getBoolean("draw_chunk_grid"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("draw_chunk_grid", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("draw_chunk_grid"))
					.build());
			mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_position"), JustMap.CONFIG.getBoolean("show_position"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_position", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_position"))
					.build());
			mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_biome"), JustMap.CONFIG.getBoolean("show_biome"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_biome", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_biome"))
					.build());
			mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_fps"), JustMap.CONFIG.getBoolean("show_FPS"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_FPS", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_FPS"))
					.build());
			mapInfo.addEntry(entryBuilder.startBooleanToggle(lang("show_game_time"), JustMap.CONFIG.getBoolean("show_time"))
					.setSaveConsumer(val -> JustMap.CONFIG.setBoolean("show_time", val))
					.setDefaultValue((boolean) JustMap.CONFIG.getDefault("show_time"))
					.build());
		
			ConfigCategory optimization = builder.getOrCreateCategory(lang("category.optimization"));
			optimization.addEntry(entryBuilder.startIntField(lang("update_cycle"), JustMap.CONFIG.getInt("update_per_cycle"))
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("update_per_cycle", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("update_per_cycle"))
					.setMin(1).setMax(1000).build());
			optimization.addEntry(entryBuilder.startIntField(lang("purge_delay"), JustMap.CONFIG.getInt("purge_delay"))
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("purge_delay", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("purge_delay"))
					.setMin(10).setMax(600).build());
			optimization.addEntry(entryBuilder.startIntField(lang("purge_amount"), JustMap.CONFIG.getInt("purge_amount"))
					.setSaveConsumer(val -> JustMap.CONFIG.setRanged("purge_amount", val))
					.setDefaultValue((int) JustMap.CONFIG.getDefault("purge_amount"))
					.setMin(100).setMax(5000).build());
			
			builder.setDoesConfirmSave(false);		
			builder.setSavingRunnable(JustMap.CONFIG::saveChanges);
			
			return builder.build();
		};
	}
}