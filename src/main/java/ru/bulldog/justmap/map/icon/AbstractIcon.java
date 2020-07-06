package ru.bulldog.justmap.map.icon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import ru.bulldog.justmap.util.RenderUtil;

public abstract class AbstractIcon extends Sprite {

	protected static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	
	protected AbstractIcon(SpriteAtlasTexture spriteAtlasTexture, Info info, int i, int j, int k, int l, int m, NativeImage nativeImage) {
		super(spriteAtlasTexture, info, i, j, k, l, m, nativeImage);
	}
	
	public abstract void draw(double x, double y, int w, int h);
	public abstract void draw(MatrixStack matrix, double x, double y, int w, int h);
	
	public void draw(double x, double y) {
		MatrixStack matrix = new MatrixStack();
		this.draw(matrix, x, y, this.getWidth(), this.getHeight());
	}
	
	public void draw(MatrixStack matrix, double x, double y) {
		this.draw(matrix, x, y, this.getWidth(), this.getHeight());
	}
	
	public void draw(double x, double y, int size) {
		MatrixStack matrix = new MatrixStack();
		this.draw(matrix, x, y, size, size);
	}
	
	public void draw(MatrixStack matrix, double x, double y, int size) {
		this.draw(matrix, x, y, size, size);
	}
	
	protected void draw(MatrixStack matrix, double x, double y, float w, float h) {
		RenderUtil.drawSprite(matrix, this, x, y, w, h);
	}
}
