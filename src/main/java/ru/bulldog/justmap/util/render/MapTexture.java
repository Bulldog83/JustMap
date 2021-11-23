package ru.bulldog.justmap.util.render;

import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import javax.imageio.ImageIO;
import org.lwjgl.opengl.GL11;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.colors.ColorUtil;

public class MapTexture {

	private File imageFile;
	private final ByteBuffer buffer;
	private byte[] bytes;
	private int glId = -1;
	private final int width;
	private final int height;
	
	public boolean changed = false;
	
	private final Object bufferLock = new Object();
	
	public MapTexture(File imageFile, int width, int height) {
		int size = 4 * width * (height - 1) + 4 * width;		
		this.imageFile = imageFile;
		this.bytes = new byte[size];
		this.buffer = ByteBuffer.allocateDirect(bytes.length).order(ByteOrder.nativeOrder());
		this.width = width;
		this.height = height;
	}
	
	public MapTexture(File imageFile, int width, int height, int color) {
		this(imageFile, width, height);
		this.fill(color);
	}
	
	public MapTexture(File imageFile, MapTexture source) {
		this(imageFile, source.getWidth(), source.getHeight());
		this.copyData(source);
	}
	
	public MapTexture(MapTexture source) {
		this(source.imageFile, source);
	}

	public int getId() {
		return this.glId;
	}
	
	public void upload() {
		if (bytes == null) return;
		
		if (this.glId == -1) {
			this.glId = TextureUtil.generateTextureId();
		}
		
		this.refillBuffer();
		
		RenderSystem.bindTexture(this.glId);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_NEAREST);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_NEAREST);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP_TO_EDGE);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_GENERATE_MIPMAP, GLC.GL_TRUE);
		RenderSystem.pixelStore(GLC.GL_UNPACK_ROW_LENGTH, 0);
		RenderSystem.pixelStore(GLC.GL_UNPACK_SKIP_PIXELS, 0);
		RenderSystem.pixelStore(GLC.GL_UNPACK_SKIP_ROWS, 0);
		
		GL11.glTexImage2D(GLC.GL_TEXTURE_2D, 0, GLC.GL_RGBA, this.getWidth(), this.getHeight(), 0, GLC.GL_RGBA, GLC.GL_UNSIGNED_INT_8_8_8_8, this.buffer);
	
		this.changed = false;
	}
	
	public int getHeight() {
		return this.height;
	}

	public int getWidth() {
		return this.width;
	}

	private byte[] getBytes() {
		synchronized(bufferLock) {
			return this.bytes.clone();
		}
	}
	
	public void copyData(MapTexture image) {
		synchronized(bufferLock) {
			this.bytes = image.getBytes();
		}
		this.changed = true;
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
				this.setColor(px, py, color);
			}
		}
	}
	
	public void setColor(int x, int y, int color) {
		if (this.bytes == null) return;
		if (x < 0 || x >= this.getWidth()) return;
		if (y < 0 || y >= this.getHeight()) return;
		
		byte a = (byte) (color >> 24);
		byte r = (byte) (color >> 16);
		byte g = (byte) (color >> 8);
		byte b = (byte) (color >> 0);
		
		int index = (x + y * this.getWidth()) * 4;
		synchronized(bufferLock) {
			if (this.bytes[index] == a &&
				this.bytes[index + 1] == b &&
				this.bytes[index + 2] == g &&
				this.bytes[index + 3] == r) {
				
				return;
			}
		
			this.bytes[index] = a;
			this.bytes[index + 1] = b;
			this.bytes[index + 2] = g;
			this.bytes[index + 3] = r;
		}		
		this.changed = true;
	}
	
	public int getColor(int x, int y) {
		if (this.bytes == null) return -1;		
		if (x < 0 || x >= this.getWidth()) return -1;
		if (y < 0 || y >= this.getHeight()) return -1;
		
		int index = (x + y * this.getWidth()) * 4;		
		synchronized(bufferLock) {
			int a = this.bytes[index] & 255;
			int b = this.bytes[index + 1] & 255;
			int g = this.bytes[index + 2] & 255;
			int r = this.bytes[index + 3] & 255;
			
			return (a << 24) | (r << 16) | (g << 8) | (b << 0);
		}
	}
	
	public int[] getPixels() {
		int[] pixels = new int[width * height];
		for (int x = 0; x < width; x++) {
			for (int y = 0; y < height; y++) {
				int index = y + x * width;
				pixels[index] = this.getColor(x, y);
			}
		}
		return pixels;
	}
	
	public void applyTint(int x, int y, int tint) {
		if (this.bytes == null) return;		
		if (x < 0 || x >= this.getWidth()) return;
		if (y < 0 || y >= this.getHeight()) return;
		
		int color = this.getColor(x, y);
		this.setColor(x, y, ColorUtil.applyTint(color, tint));
	}
	
	public void fill(int color) {
		int width = this.getWidth();
		int height = this.getHeight();
		
		this.fill(0, 0, width, height, color);
	}
	
	public void fill(int x, int y, int w, int h, int color) {
		if (this.bytes == null) return;		
		if (x < 0 || y < 0) return;
		
		int width = this.getWidth();
		int height = this.getHeight();
		
		if (x + w > width) width -= x;
		else width = w;
		if (y + h > height) height -= y;
		else height = h;
		
		if (width <= 0 || height <= 0) return;
		
		synchronized(bufferLock) {
			for(int i = x; i < x + width; i++) {
				for (int j = y; j < y + height; j++) {
					this.setColor(i, j, color);
				}
			}
		}
	}
	
	public void applyOverlay(MapTexture overlay) {
		if (this.bytes == null) return;
		
		int width = Math.min(this.width, overlay.getWidth());
		int height = Math.min(this.height, overlay.getHeight());
		if (width <= 0 || height <= 0) return;
		synchronized(bufferLock) {
			for (int x = 0; x < width; x++) {
				for (int y = 0; y < height; y++) {
					int color = overlay.getColor(x, y);
					int alpha = (color >> 24) & 255;
					if (alpha > 0) {
						this.applyTint(x, y, color);
					}
				}
			}
		}
	}
	
	public void saveImage() {
		if (imageFile == null || bytes == null) return;
		try (OutputStream fileOut = new FileOutputStream(imageFile)) {
			BufferedImage pngImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
			byte[] data = ((DataBufferByte) pngImage.getTile(0, 0).getDataBuffer()).getData();
			byte[] bytes = this.getBytes();
			for (int i = 0; i < bytes.length; i++) {
				data[i] = bytes[i];
			}
			ImageIO.write(pngImage, "png", fileOut);
			JustMap.LOGGER.debug("Image saved: {}", imageFile);
			pngImage.flush();
		} catch (Exception ex) {
			JustMap.LOGGER.warning("Can't save image: " + imageFile.toString());
			JustMap.LOGGER.warning(ex.getLocalizedMessage());
		}
	}
	
	public boolean loadImage(File png) {
		this.imageFile = png;
		if (!png.exists()) return false;
		synchronized (bufferLock) {
			try (InputStream fileInput = new FileInputStream(png)) {
				BufferedImage pngImage = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
				pngImage.setData(ImageIO.read(fileInput).getData());
				this.bytes = ((DataBufferByte) pngImage.getTile(0, 0).getDataBuffer()).getData().clone();
				this.changed = true;
				pngImage.flush();			
				JustMap.LOGGER.debug("Image loaded: {}", png);
				return true;
			} catch (Exception ex) {
				JustMap.LOGGER.warning("Can't load image: " + png);
				JustMap.LOGGER.warning(ex.getLocalizedMessage());
				return false;
			}
		}
	}
	
	public void clear() {
		synchronized(bufferLock) {
			this.buffer.clear();
		}
	}
	
	public void close() {
		this.clearId();
		synchronized(bufferLock) {
			this.bytes = null;
			this.clear();
		}
	}
	
	private void clearId() {
		if (!RenderSystem.isOnRenderThread()) {
			RenderSystem.recordRenderCall(() -> {
				if (this.glId != -1) {
					TextureUtil.releaseTextureId(this.glId);
					this.glId = -1;
				}
			});
		} else if (this.glId != -1) {
			TextureUtil.releaseTextureId(this.glId);
			this.glId = -1;
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
