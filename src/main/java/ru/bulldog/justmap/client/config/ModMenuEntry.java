package ru.bulldog.justmap.client.config;

import io.github.prospector.modmenu.api.ConfigScreenFactory;
import io.github.prospector.modmenu.api.ModMenuApi;

import net.fabricmc.api.Environment;
import ru.bulldog.justmap.JustMap;
import net.fabricmc.api.EnvType;

@Environment(EnvType.CLIENT)
public class ModMenuEntry implements ModMenuApi {
	@Override
	public ConfigScreenFactory<?> getModConfigScreenFactory() {
		return ConfigFactory::getConfigScreen;
	}

	@Override
	public String getModId() {
		return JustMap.MODID;
	}
}