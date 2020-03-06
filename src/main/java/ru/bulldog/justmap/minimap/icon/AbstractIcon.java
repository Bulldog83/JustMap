package ru.bulldog.justmap.minimap.icon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;

import ru.bulldog.justmap.util.DrawHelper;

public abstract class AbstractIcon extends Sprite {

	protected static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	
	protected AbstractIcon(SpriteAtlasTexture spriteAtlasTexture, Info info, int i, int j, int k, int l, int m, NativeImage nativeImage) {
		super(spriteAtlasTexture, info, i, j, k, l, m, nativeImage);
	}
	
	public abstract void draw(double x, double y, int w, int h);
	
	public void draw(double x, double y, int size) {
		this.draw(x, y, size, size);
	}
	
	protected void draw(double x, double y, float w, float h) {
		DrawHelper.draw(x, y, w, h, this);
	}
}
