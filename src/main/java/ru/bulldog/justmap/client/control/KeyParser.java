package ru.bulldog.justmap.client.control;


import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;

public abstract class KeyParser {
	public final KeyBinding keyBinding;
	public static final MinecraftClient MC = MinecraftClient.getInstance();

	protected KeyParser(KeyBinding keyBinding) {
		this.keyBinding = keyBinding;
	}

	public void onKeyDown() {}

	public void onKeyUp() {}

	public boolean isListening() {
		return true;
	}
}
