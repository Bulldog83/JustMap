package ru.bulldog.justmap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.math.Line;

import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

public class ImageUtil {
	private ImageUtil() {}	

	private static ResourceManager resourceManager;
	
	private static void checkResourceManager() {
		if (resourceManager == null) resourceManager = DataUtil.getMinecraft().getResourceManager();
	}
	
	public static boolean imageExists(Identifier image) {
		if (image == null) return false;		
		try {
			return resourceManager.containsResource(image);
		} catch(Exception ex) {
			JustMap.LOGGER.catching(ex);
			return false;
		}
	}
	
	public static NativeImage loadImage(File image, int w, int h) {
		if (image.exists()) {
			try (InputStream fis = new FileInputStream(image)) {
				return NativeImage.read(fis);
			} catch (IOException ex) {
				JustMap.LOGGER.warning(String.format("Can't load texture image: %s. Will be created empty image.", image));
				JustMap.LOGGER.warning(String.format("Cause: %s.", ex.getMessage()));
			}
		}		
		return new NativeImage(w, h, false);
	}
	
	public static NativeImage loadImage(Identifier image, int w, int h) {
		checkResourceManager();		
		if (imageExists(image)) {
			try (Resource resource = resourceManager.getResource(image)) {
				return NativeImage.read(resource.getInputStream());			
			} catch (IOException e) {
				JustMap.LOGGER.warning(String.format("Can't load texture image: %s. Will be created empty image.", image));
				JustMap.LOGGER.warning(String.format("Cause: %s.", e.getMessage()));
			}
		}		
		return new NativeImage(w, h, false);
	}
	
	public static NativeImage applyColor(NativeImage image, int color) {
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (image.getPixelOpacity(i, j) == -1) {
					int newColor = ColorHelper.multiplyColor(image.getPixelColor(i, j), color);
					image.setPixelColor(i, j, ColorUtil.toABGR(newColor));
				}
			}
		}
		
		return image;
	}
	
	public static void fillImage(NativeImage image, int color) {
		image.fillRect(0, 0, image.getWidth(), image.getHeight(), color);
	}
	
	public static NativeImage createSquareSkin(NativeImage texture, int width, int height, int border) {
		NativeImage squareSkin = new NativeImage(width, height, false);
		int imgW = texture.getWidth();
		int imgH = texture.getHeight();
		int imgX = 0, imgY = 0;
		int y = 0;
		while(y < height) {
			int x = 0;
			while(x < width) {
				if (imgX >= imgW) imgX = 0;
				if ((x >= width - border || x <= border) ||
					(y >= height - border || y <= border)) {							
					int pixel = texture.getPixelColor(imgX, imgY);
					squareSkin.setPixelColor(x, y, pixel);
				}				
				imgX++;
				if (imgX >= imgW) imgX = 0;
				x++;
			}
			imgY++;
			if (imgY >= imgH) imgY = 0;
			y++;
		}
		
		return squareSkin;
	}
	
	public static NativeImage createRoundSkin(NativeImage texture, int width, int height, int border) {
		NativeImage roundSkin = new NativeImage(width, height, false);
		int imgW = texture.getWidth();
		int imgH = texture.getHeight();
		int imgX = 0, imgY = 0;
		int centerX = width / 2;
		int centerY = height / 2;
		int rOut = centerX;
		int rIn = rOut - border;
		int y = 0;
		while(y < height) {
			int x = 0;
			while(x < width) {
				if (imgX >= imgW) imgX = 0;
				int len = 0;
				if (centerX != x || centerY != y) {
					len = (int) new Line(centerX, centerY, x, y).lenght();
				}
				if (len <= rOut && len >= rIn) {							
					int pixel = texture.getPixelColor(imgX, imgY);
					roundSkin.setPixelColor(x, y, pixel);
				}
				imgX++;
				if (imgX >= imgW) imgX = 0;
				x++;
			}
			imgY++;
			if (imgY >= imgH) imgY = 0;
			y++;
		}
		
		return roundSkin;
	}
}