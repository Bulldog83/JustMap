package ru.bulldog.justmap.client.render;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.texture.TextureUtil;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;

public class MapTexture {

	private ByteBuffer buffer;
	private byte[] bytes;
	private int glId = -1;
	private final int width;
	private final int height;
	
	private Object bufferLock = new Object();
	
	public MapTexture(int width, int height) {
		int size = 4 * width * (height - 1) + 4 * width;		
		this.bytes = new byte[size];
		this.buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
		this.width = width;
		this.height = height;
	}
	
	public int getId() {
		return this.glId;
	}
	
	public void upload() {
		if (this.glId == -1) {
			this.glId = TextureUtil.generateTextureId();
		}
		
		this.refillBuffer();
		
		RenderSystem.bindTexture(this.glId);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL12.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL12.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GL11.GL_TEXTURE_2D, GL14.GL_GENERATE_MIPMAP, GL11.GL_TRUE);
		RenderSystem.pixelStore(GL11.GL_UNPACK_ROW_LENGTH, GL11.GL_ZERO);
		RenderSystem.pixelStore(GL11.GL_UNPACK_SKIP_PIXELS, GL11.GL_ZERO);
		RenderSystem.pixelStore(GL11.GL_UNPACK_SKIP_ROWS, GL11.GL_ZERO);
		
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, GL11.GL_ZERO, GL11.GL_RGBA, this.getWidth(), this.getHeight(), GL11.GL_ZERO, GL11.GL_RGBA, GL12.GL_UNSIGNED_INT_8_8_8_8, this.buffer);
	}
	
	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	private byte[] getBytes() {
		synchronized(bufferLock) {
			return Arrays.copyOf(this.bytes, this.bytes.length);
		}
	}
	
	public void copyData(MapTexture image) {
		synchronized(bufferLock) {
			this.bytes = image.getBytes();
		}
	}
	
	public void writeChunkData(int x, int y, int[] colorData) {
		for (int i = 0; i < 16; i++) {
			int px = i + x;
			
			if (px >= this.getWidth()) break;
			if (px < 0) continue;
			
			for (int j = 0; j < 16; j++) {
				int py = j + y;
				
				if (py >= this.getHeight()) break;
				if (py < 0) continue;
				
				int color = colorData[i + (j << 4)];
				if (ClientParams.showGrid && (i == 0 || j == 0)) {
					color = ColorUtil.applyTint(color, 0x66333333);
				}
				this.setRGB(px, py, color);
			}
		}
	}
	
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
	
	public void clear() {
		this.fill(Colors.BLACK);
		this.upload();
	}
	
	public void close() {
		if (this.glId != -1) {
			TextureUtil.releaseTextureId(this.glId);
			this.glId = -1;
		}		
		synchronized(bufferLock) {
			this.buffer.clear();
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
