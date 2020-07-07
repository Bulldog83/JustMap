package ru.bulldog.justmap.client.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import ru.bulldog.justmap.util.RenderUtil;

public abstract class Image {

	protected static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	
	protected final NativeImage image;
	protected Identifier textureId;
	protected int width;
	protected int height;
	
	protected Image(Identifier id, NativeImage image) {
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.textureId = id;
		this.image = image;
	}
	
	public abstract void draw(MatrixStack matrix, double x, double y, int w, int h);
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public Identifier getId() {
		return this.textureId;
	}
	
	public void bindTexture() {
		textureManager.bindTexture(textureId);
	}
	
	public void draw(double x, double y) {
		MatrixStack matrix = new MatrixStack();
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
		RenderUtil.drawImage(matrix, this, x, y, w, h);
	}
}
