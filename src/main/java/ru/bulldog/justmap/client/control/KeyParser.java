package ru.bulldog.justmap.client.control;


import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;

public abstract class KeyParser {
	public final KeyMapping keyBinding;
	public static final Minecraft MC = Minecraft.getInstance();
	
	protected KeyParser(KeyMapping keyBinding) {
		this.keyBinding = keyBinding;
	}
	
	public void onKeyDown() {}
	
	public void onKeyUp() {}
	
	public boolean isListening() {
		return true;
	}
}
