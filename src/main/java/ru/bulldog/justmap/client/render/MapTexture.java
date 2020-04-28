package ru.bulldog.justmap.client.render;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.texture.TextureUtil;

public class MapTexture extends BufferedImage {

	private ByteBuffer buffer;
	private byte[] bytes;
	private int glId = -1;
	
	private Object bufferLock = new Object();
	
	public MapTexture(int width, int height) {
		super(width, height, TYPE_4BYTE_ABGR);
		
		this.bytes = ((DataBufferByte) ((DataBufferByte) this.getRaster().getDataBuffer())).getData();
		this.buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
	}
	
	public void upload() {
		if (this.glId == -1) {
			this.glId = TextureUtil.generateTextureId();
		}
		
		this.refillBuffer();
		
		RenderSystem.bindTexture(this.glId);
		RenderSystem.texParameter(3553, 10241, 9728);
		RenderSystem.texParameter(3553, 10240, 9728);
		RenderSystem.texParameter(3553, 10242, 33071);
		RenderSystem.texParameter(3553, 10243, 33071);
		RenderSystem.pixelStore(3314, 0);
		RenderSystem.pixelStore(3316, 0);
		RenderSystem.pixelStore(3315, 0);
		
		GL11.glTexImage2D(3553, 0, 6408, this.getWidth(), this.getHeight(), 0, 6408, 32821, this.buffer);
	}
	
	private byte[] getBytes() {
		synchronized(bufferLock) {
			return Arrays.copyOf(this.bytes, this.bytes.length);
		}
	}
	
	public void copyImage(MapTexture image) {
		synchronized(bufferLock) {
			this.bytes = image.getBytes();
		}
	}
	
	@Override
	public void setRGB(int x, int y, int color) {
		if (x < 0 || x >= this.getWidth()) return;
		if (y < 0 || y >= this.getHeight()) return;
		
		int index = (x + y * this.getWidth()) * 4;
		synchronized(bufferLock) {
			this.bytes[index] = (byte) (color >> 24);
			this.bytes[index + 1] = (byte) (color >> 0);
			this.bytes[index + 2] = (byte) (color >> 8);
			this.bytes[index + 3] = (byte) (color >> 16);
		}
	}
	
	public void fill(int color) {
		int width = this.getWidth();
		int height = this.getHeight();
		
		synchronized(bufferLock) {
			for(int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					this.setRGB(x, y, color);
				}
			}
		}
	}
	
	public void close() {
		if (this.glId != -1) {
			TextureUtil.releaseTextureId(this.glId);
			this.glId = -1;
		}
		
		synchronized(bufferLock) {
			Arrays.fill(this.bytes, (byte) 0);
			this.buffer.clear();
			this.flush();
		}
	}
	
	private void refillBuffer() {
		synchronized(bufferLock) {
			this.buffer.clear();
			this.buffer.put(this.bytes);
			this.buffer.position(0).limit(bytes.length);
		}
	}
}
