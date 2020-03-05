package ru.bulldog.justmap.util;

import java.awt.image.BufferedImage;
import java.io.IOException;

import ru.bulldog.justmap.JustMap;

import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ImageUtil {
	private ImageUtil() {}	

	private static ResourceManager resourceManager = MinecraftClient.getInstance().getResourceManager();
	
	public static NativeImage loadImage(Identifier image, int w, int h) {
		try (Resource resource = resourceManager.getResource(image)) {
			return NativeImage.read(resource.getInputStream());			
		} catch (IOException e) {
			JustMap.LOGGER.logWarning(String.format("Can't load texture image: %s. Will be created empty image.", image));
			JustMap.LOGGER.logWarning(String.format("Cause: %s.", e.getMessage()));
		}		
		return new NativeImage(w, h, false);
	}
	
	public static NativeImage applyColor(NativeImage image, int color) {
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (image.getPixelOpacity(i, j) == -1) {
					int newColor = ColorHelper.multiplyColor(image.getPixelRgba(i, j), color);
					image.setPixelRgba(i, j, ColorUtil.toABGR(newColor));
				}
			}
		}
		
		return image;
	}
	
	public static void fillImage(NativeImage image, int color) {
		image.fillRect(0, 0, image.getWidth(), image.getHeight(), color);
	}
	
	public static NativeImage readTile(NativeImage image, int x, int y, int w, int h) {
		NativeImage tile = new NativeImage(w, h, false);
		
		for(int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				tile.setPixelRgba(i, j, image.getPixelRgba(x + i, y + j));
			}
		}
		
		return tile;
	}
	
	public static void writeTile(NativeImage image, NativeImage tile, int x, int y) {
		writeIntoImage(tile, image, x, y);
	}
	
	public static NativeImage writeIntoImage(NativeImage toWrite, NativeImage destination, int x, int y) {
		int drawWidth = toWrite.getWidth();
		int drawHeight = toWrite.getHeight();
		int destinationWidth = destination.getWidth();
		int destinationHeight = destination.getHeight();

		if (x + drawWidth >= destinationWidth) {
			drawWidth = destinationWidth - x;
		}
		
		if (y + drawHeight >= destinationHeight) {
			drawHeight = destinationHeight - y;
		}
	
		for (int xOffset = 0; xOffset < drawWidth; xOffset++) {
			int xp = x + xOffset;
			if (xp < 0) {
				continue;
			}
	
			for (int yOffset = 0; yOffset < drawHeight; yOffset++) {
				int yp = y + yOffset;
				if (yp < 0) {
					continue;
				}
				
				destination.setPixelRgba(xp, yp, toWrite.getPixelRgba(xOffset, yOffset));
			}
		}
		
		return destination;
	}
	
	public static NativeImage toNativeImage(BufferedImage image) {
		int w = image.getWidth();
		int h = image.getHeight();
		
		NativeImage nativeImage = new NativeImage(w, h, false);
		
		for (int i = 0; i < w; i++) {
			for(int j = 0; j < h; j++) {
				nativeImage.setPixelRgba(i, j, ColorUtil.toABGR(image.getRGB(i, j)));
			}
		}
		
		return nativeImage;
	}
}