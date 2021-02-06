package ru.bulldog.justmap.util.render;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;

public abstract class Image {

	protected static TextureManager textureManager = Minecraft.getInstance().getTextureManager();
	
	protected final NativeImage image;
	protected ResourceLocation textureId;
	protected int width;
	protected int height;
	
	protected Image(ResourceLocation id, NativeImage image) {
		this.width = image.getWidth();
		this.height = image.getHeight();
		this.textureId = id;
		this.image = image;
	}
	
	public abstract void draw(PoseStack matrices, double x, double y, int w, int h);
	
	public int getWidth() {
		return this.width;
	}
	
	public int getHeight() {
		return this.height;
	}
	
	public ResourceLocation getId() {
		return this.textureId;
	}
	
	public void bindTexture() {
		textureManager.bind(textureId);
	}
	
	public void draw(double x, double y) {
		PoseStack matrices = new PoseStack();
		this.draw(matrices, x, y, this.getWidth(), this.getHeight());
	}
	
	public void draw(double x, double y, int size) {
		PoseStack matrices = new PoseStack();
		this.draw(matrices, x, y, size, size);
	}
	
	public void draw(PoseStack matrices, double x, double y, int size) {
		this.draw(matrices, x, y, size, size);
	}
	
	protected void draw(PoseStack matrices, double x, double y, float w, float h) {
		RenderUtil.drawImage(matrices, this, x, y, w, h);
	}
}
