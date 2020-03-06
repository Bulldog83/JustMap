package ru.bulldog.justmap.util;

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
	
	public static NativeImage writeTile(NativeImage image, NativeImage tile, int x, int y) {
		int tileWidth = tile.getWidth();
		int tileHeight = tile.getHeight();
		int imageWidth = image.getWidth();
		int imageHeight = image.getHeight();

		if (tileWidth + x <= 0 || tileHeight + y <= 0) return image;
		
		if (x + tileWidth > imageWidth) {
			tileWidth = imageWidth - x;
		}		
		if (y + tileHeight > imageHeight) {
			tileHeight = imageHeight - y;
		}
	
		for (int i = 0; i < tileWidth; i++) {
			int xp = x + i;
			if (xp < 0) continue;
	
			for (int j = 0; j < tileHeight; j++) {
				int yp = y + j;
				if (yp < 0) continue;
				
				image.setPixelRgba(xp, yp, tile.getPixelRgba(i, j));
			}
		}
		
		return image;
	}
}