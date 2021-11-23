package ru.bulldog.justmap.util;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.TranslatableText;

import ru.bulldog.justmap.JustMap;

public class LangUtil {
	private final static String MODID = JustMap.MODID;

	public final static String CONFIG_ELEMENT = "configuration";
	public final static String DIMENSION_ELEMENT = "dim";
	public final static String GUI_ELEMENT = "gui";
	public final static String GUI_SCREEN_ELEMENT = "gui.screen";
	public final static String GUI_WORLDMAP_ELEMENT = "gui.worldmap";

	private String element;

	public LangUtil(String element) {
		this.element = element;
	}

	public void setElement(String key) {
		this.element = key;
	}

	public String getString(String key) {
		return getString(element, key);
	}

	public TranslatableText getText(String key) {
		return getText(element, key);
	}

	public static String getString(String element, String key) {
		return I18n.translate(String.format("%s.%s.%s", MODID, element, key));
	}

	public static TranslatableText getText(String element, String key) {
		return new TranslatableText(getString(element, key));
	}
}
