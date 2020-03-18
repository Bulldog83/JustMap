package ru.bulldog.justmap.client.config;

import ru.bulldog.justmap.JustMap;
import io.github.prospector.modmenu.api.ModMenuApi;

import net.minecraft.client.gui.screen.Screen;
import net.fabricmc.api.Environment;
import net.fabricmc.api.EnvType;

import java.util.function.Function;

@Environment(EnvType.CLIENT)
public class ModMenuEntry implements ModMenuApi {
	
	@Override
	public String getModId() {
		return JustMap.MODID;
	}
	
	@Override
	public Function<Screen, ? extends Screen> getConfigScreenFactory() {
		return (parent) -> ConfigFactory.getConfigScreen(parent);
	}
}