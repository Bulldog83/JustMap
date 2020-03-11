package ru.bulldog.justmap.minimap.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.texture.NativeImage;
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

	private final RegionPos pos;
	
	private Map<Layer, RegionLayer> layers = new HashMap<>();	
	private Layer currentLayer;
	private int currentLevel;
	
	public long updated = 0;
	
	public MapRegion(ChunkPos pos) {
		this(pos.getRegionX(),
			 pos.getRegionZ());
	}
	
	public MapRegion(int x, int z) {
		this.pos = new RegionPos(x, z);
	}
	
	public void setLevel(int level) {
		this.currentLevel = level;
	}
	
	public void setLayer(Layer layer) {
		this.currentLayer = layer;
	}
	
	public NativeImage getChunkImage(ChunkPos chunkPos) {
		if (chunkPos.getRegionX() != this.pos.x ||
			chunkPos.getRegionZ() != this.pos.z) {
			
			return null;
		}
		
		int imgX = (chunkPos.x - (this.pos.x << 5)) << 4;
		int imgY = (chunkPos.z - (this.pos.z << 5)) << 4;
		
		return ImageUtil.readTile(getImage(), imgX, imgY, 16, 16);
	}
	
	public synchronized NativeImage getImage() {
		if (!layers.containsKey(currentLayer)) {
			layers.put(currentLayer, new RegionLayer());
		}
		
		return layers.get(currentLayer).getImage();
	}
	
	public void resetRegion() {
		layers.clear();
		StorageUtil.clearCache();
	}
	
	public synchronized void storeChunk(MapChunk chunk) {
		ChunkPos chunkPos = chunk.getPos();
		if (chunkPos.getRegionX() != this.pos.x || chunkPos.getRegionZ() != this.pos.z) return;
		
		int imgX = (chunkPos.x - (this.pos.x << 5)) << 4;
		int imgY = (chunkPos.z - (this.pos.z << 5)) << 4;			
		ImageUtil.writeTile(getImage(), chunk.getImage(), imgX, imgY);
		
		this.updated = System.currentTimeMillis();
	}
	
	public int getX() {
		return this.pos.x;
	}
	
	public int getZ() {
		return this.pos.z;
	}
	
	public synchronized void saveImage() {
		File png = new File(imagesDir(), String.format("%d.%d.png", this.pos.x, this.pos.z));		
		NativeImage image = getImage();
		
		try {
			image.writeFile(png);
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
		private final Map<Integer, NativeImage> images;
		
		private RegionLayer() {
			images = new HashMap<>();
		}
		
		private NativeImage getImage() {
			if (images.containsKey(currentLevel)) {
				return images.get(currentLevel);
			}			
			
			NativeImage image = loadImage();
			images.put(currentLevel, image);
			
			return image;
		}
		
		private NativeImage loadImage() {
			File png = new File(imagesDir(), String.format("%d.%d.png", pos.x, pos.z));
			try (FileInputStream fis = new FileInputStream(png)) {
				return NativeImage.read(fis);
			} catch (IOException ex) {}
			
			NativeImage image = new NativeImage(512, 512, false);
			ImageUtil.fillImage(image, Colors.BLACK);
			
			return image;
		}
	}
}
