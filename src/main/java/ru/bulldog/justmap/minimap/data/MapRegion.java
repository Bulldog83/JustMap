package ru.bulldog.justmap.minimap.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import net.minecraft.util.math.ChunkPos;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.minimap.data.MapProcessor.Layer;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.StorageUtil;

public class MapRegion {
	
	private static long saved = 0;
	
	public static void saveImages() {
		long time = System.currentTimeMillis();
		if (time - saved < 3000) return;
		
		MapCache data = MapCache.get();
		
		if (data == null) return;
		
		JustMap.EXECUTOR.execute(() -> {
			data.getRegions().forEach((pos, region) -> {
				region.saveImage();
			});
		});
		
		saved = time;
	}
	
	private Map<Layer, RegionLayer> layers = new HashMap<>();
	private final int x;
	private final int z;
	
	private Layer currentLayer;
	private int currentLevel;
	
	public MapRegion(ChunkPos pos) {
		this(pos.getRegionX(),
			 pos.getRegionZ());
	}
	
	public MapRegion(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public void setLevel(int level) {
		this.currentLevel = level;
	}
	
	public void setLayer(Layer layer) {
		this.currentLayer = layer;
	}
	
	public BufferedImage getChunkImage(ChunkPos pos) {
		if (pos.getRegionX() != x || pos.getRegionZ() != z) {
			BufferedImage image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
			ImageUtil.fillImage(image, Colors.BLACK);
			
			return image;
		}
		
		int imgX = (pos.x - (x << 5)) << 4;
		int imgY = (pos.z - (z << 5)) << 4;
		
		return getImage().getSubimage(imgX, imgY, 16, 16);
	}
	
	private BufferedImage getImage() {
		if (!layers.containsKey(currentLayer)) {
			layers.put(currentLayer, new RegionLayer());
		}
		
		return layers.get(currentLayer).getImage();
	}
	
	public void resetRegion() {
		layers = new HashMap<>();
		StorageUtil.clearCache();
	}
	
	public synchronized void storeChunk(MapChunk chunk) {
		ChunkPos pos = chunk.getPos();
		if (pos.getRegionX() != x || pos.getRegionZ() != z) return;
		
		int imgX = (pos.x - (x << 5)) << 4;
		int imgY = (pos.z - (z << 5)) << 4;			
		ImageUtil.writeTile(getImage(), chunk.getImage(), imgX, imgY);
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getZ() {
		return this.z;
	}
	
	public synchronized void saveImage() {
		File png = new File(imagesDir(), String.format("%d.%d.png", x, z));
		
		BufferedImage image = getImage();
		
		try {
			ImageIO.write(image, "png", png);
		} catch (IOException ex) {
			JustMap.LOGGER.catching(ex);
		}
	}
	
	private File imagesDir() {
		int dim = MapCache.get().world.getDimension().getType().getRawId();
		File dimDir = new File(StorageUtil.cacheDir(), String.format("DIM%d/", dim));
		
		File cacheDir;
		if (currentLayer == Layer.CAVES) {
			cacheDir = new File(dimDir, String.format("caves/%d/", currentLevel));
		} else {
			cacheDir = new File(dimDir, "surface/");
		}
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		return cacheDir;
	}
	
	private class RegionLayer {
		private final Map<Integer, BufferedImage> images;
		
		private RegionLayer() {
			images = new HashMap<>();
		}
		
		private BufferedImage getImage() {
			if (images.containsKey(currentLevel)) {
				return images.get(currentLevel);
			}			
			
			BufferedImage image = loadImage();
			images.put(currentLevel, image);
			
			return image;
		}
		
		private BufferedImage loadImage() {
			File png = new File(imagesDir(), String.format("%d.%d.png", x, z));
			try {
				return ImageIO.read(png);
			} catch (IOException ex) {}
			
			BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			ImageUtil.fillImage(image, Colors.BLACK);
			
			return image;
		}
	}
}
