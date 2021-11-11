package ru.bulldog.justmap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.resource.Resource;
import net.minecraft.resource.ResourceManager;
import net.minecraft.util.Identifier;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.colors.ColorUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.Point;

public class ImageUtil {
	private ImageUtil() {}	

	private static ResourceManager resourceManager;
	
	private static void checkResourceManager() {
		if (resourceManager == null) resourceManager = MinecraftClient.getInstance().getResourceManager();
	}
	
	public static boolean imageExists(Identifier image) {
		checkResourceManager();		
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
	
	public static void applyColor(NativeImage image, int color) {
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				if (image.getOpacity(i, j) == -1) {
					int newColor = ColorHelper.multiplyColor(image.getColor(i, j), color);
					image.setColor(i, j, ColorUtil.toABGR(newColor));
				}
			}
		}
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
					int pixel = texture.getColor(imgX, imgY);
					squareSkin.setColor(x, y, pixel);
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
					len = (int) Line.length(centerX, centerY, x, y);
				}
				if (len <= rOut && len >= rIn) {							
					int pixel = texture.getColor(imgX, imgY);
					roundSkin.setColor(x, y, pixel);
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
	
	public static NativeImage generateOutline(NativeImage image, int width, int height, int color) {
		NativeImage outline = new NativeImage(width + 4, height + 4, false);
		ImageUtil.fillImage(outline, Colors.TRANSPARENT);
		
		int outWidth = outline.getWidth();
		int outHeight = outline.getHeight();
		int outlineColor = ColorUtil.toABGR(color);
		
		List<Point> outlinePixels = new ArrayList<>();
		for (int x = 0; x < width; x++) {
			int left = x - 1;
			int right = x + 1;
			for (int y = 0; y < height; y++) {
				int alpha = (image.getColor(x, y) >> 24) & 255;
				if (alpha == 0) continue;
				
				outlinePixels.add(new Point(x + 2, y + 2));
				
				int top = y - 1;
				int bottom = y + 1;					
				if (top >= 0) {
					alpha = (image.getColor(x, top) >> 24) & 255;
					if (alpha == 0) {
						Point pixel = new Point(x + 2, y);
						if (!outlinePixels.contains(pixel)) {
							outlinePixels.add(pixel);
							outlinePixels.add(new Point(x + 2, y + 1));
						}
					}
					if (left >= 0) {
						alpha = (image.getColor(left, top) >> 24) & 255;
						if (alpha == 0) {
							Point pixel = new Point(x, y);
							if (!outlinePixels.contains(pixel)) {
								outlinePixels.add(pixel);
								outlinePixels.add(new Point(x, y + 1));
								outlinePixels.add(new Point(x + 1, y));
								outlinePixels.add(new Point(x + 1, y + 1));
							}
						}
					}
					if (right < width) {
						alpha = (image.getColor(right, top) >> 24) & 255;
						if (alpha == 0) {
							Point pixel = new Point(right + 2, y);
							if (!outlinePixels.contains(pixel)) {
								outlinePixels.add(pixel);
								outlinePixels.add(new Point(right + 2, y + 1));
								outlinePixels.add(new Point(right + 3, y));
								outlinePixels.add(new Point(right + 3, y + 1));
							}
						}
					}
				} else if (y == 0){
					Point pixel = new Point(x + 2, 0);
					if (!outlinePixels.contains(pixel)) {
						outlinePixels.add(pixel);
						outlinePixels.add(new Point(x + 2, 1));
					}
				}
				if (bottom < height) {
					alpha = (image.getColor(x, bottom) >> 24) & 255;
					if (alpha == 0) {
						Point pixel = new Point(x + 2, bottom + 1);
						if (!outlinePixels.contains(pixel)) {
							outlinePixels.add(pixel);
							outlinePixels.add(new Point(x + 2, bottom + 2));
						}
					}
					if (left >= 0) {
						alpha = (image.getColor(left, bottom) >> 24) & 255;
						if (alpha == 0) {
							Point pixel = new Point(x, bottom + 2);
							if (!outlinePixels.contains(pixel)) {
								outlinePixels.add(pixel);
								outlinePixels.add(new Point(x, bottom + 3));
								outlinePixels.add(new Point(x + 1, bottom + 2));
								outlinePixels.add(new Point(x + 1, bottom + 3));
							}
						}
					}
					if (right < width) {
						alpha = (image.getColor(right, bottom) >> 24) & 255;
						if (alpha == 0) {
							Point pixel = new Point(right + 2, bottom + 2);
							if (!outlinePixels.contains(pixel)) {
								outlinePixels.add(pixel);
								outlinePixels.add(new Point(right + 2, bottom + 3));
								outlinePixels.add(new Point(right + 3, bottom + 2));
								outlinePixels.add(new Point(right + 3, bottom + 3));
							}
						}
					}
				} else if (y == height - 1) {
					Point pixel = new Point(x + 2, outHeight - 1);
					if (!outlinePixels.contains(pixel)) {
						outlinePixels.add(pixel);
						outlinePixels.add(new Point(x + 2, outHeight - 2));
					}
				}
				if (left >= 0) {
					alpha = (image.getColor(left, y) >> 24) & 255;
					if (alpha == 0) {
						Point pixel = new Point(x, y + 2);
						if (!outlinePixels.contains(pixel)) {
							outlinePixels.add(pixel);
							outlinePixels.add(new Point(x + 1, y + 2));
						}
					}
				} else if (x == 0) {
					Point pixel = new Point(0, y + 2);
					if (!outlinePixels.contains(pixel)) {
						outlinePixels.add(pixel);
						outlinePixels.add(new Point(1, y + 2));
					}
				}
				if (right < width) {
					alpha = (image.getColor(right, y) >> 24) & 255;
					if (alpha == 0) {
						Point pixel = new Point(right + 1, y + 2);
						if (!outlinePixels.contains(pixel)) {
							outlinePixels.add(pixel);
							outlinePixels.add(new Point(right + 2, y + 2));
						}
					}
				} else if (x == width - 1) {
					Point pixel = new Point(outWidth - 1, y + 2);
					if (!outlinePixels.contains(pixel)) {
						outlinePixels.add(pixel);
						outlinePixels.add(new Point(outWidth - 2, y + 2));
					}
				}
			}
		}
		outlinePixels.forEach(pixel -> {
			outline.setColor((int) pixel.x, (int) pixel.y, outlineColor);
		});
		
		return outline;
	}
}
