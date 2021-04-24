package ru.bulldog.justmap.util;

import com.sun.org.apache.xml.internal.security.utils.I18n;
import net.minecraft.network.chat.TranslatableComponent;
import old_files.justmap.JustMap;

public class LangUtil {
	private final static String MOD_ID = JustMap.MOD_ID;
	
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
	
	public TranslatableComponent getText(String key) {
		return getText(element, key);
	}
	
	public static String getString(String element, String key) {
		return I18n.translate(String.format("%s.%s.%s", MOD_ID, element, key));
	}
	
	public static TranslatableComponent getText(String element, String key) {
		return new TranslatableComponent(getString(element, key));
	}
}
