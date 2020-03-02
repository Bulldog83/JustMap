package ru.bulldog.justmap.minimap.data;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import com.mojang.datafixers.util.Pair;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.minimap.data.MapProcessor.Layer;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.StorageUtil;

public class MapRegion {
	
	public final static Map<Pair<Integer, Integer>, MapRegion> regions = new HashMap<>();
	
	public static MapRegion getRegion(WorldChunk chunk) {
		Pair<Integer, Integer> pos = new Pair<>(chunk.getPos().getRegionX(),
												chunk.getPos().getRegionZ());
		
		if (regions.containsKey(pos)) {
			return regions.get(pos);
		}
		
		MapRegion region = new MapRegion(chunk);
		regions.put(pos, region);
		
		return region;		
	}
	
	private static long saved = 0;
	
	public static void saveImages() {
		long time = System.currentTimeMillis();
		if (time - saved < 5000) return;
		
		JustMap.EXECUTOR.execute(() -> {
			regions.forEach((pos, region) -> {
				region.layers.forEach((layer, regionLayer) -> {
					regionLayer.images.forEach((level, image) -> {
						region.saveImage(layer, level);
					});
				});
			});
		});
		
		saved = time;
	}
	
	private final World world;
	
	private final int x;
	private final int z;
	
	private final Map<Layer, RegionLayer> layers = new HashMap<>();
	
	public MapRegion(WorldChunk chunk) {
		this(chunk.getWorld(),
			 chunk.getPos().getRegionX(),
			 chunk.getPos().getRegionZ());
	}
	
	public MapRegion(World world, int x, int z) {
		this.x = x;
		this.z = z;
		
		this.world = world;
	}
	
	private BufferedImage getImage(Layer layer, int level) {
		if (!layers.containsKey(layer)) {
			layers.put(layer, new RegionLayer());
		}
		
		return layers.get(layer).getImage(level);
	}
	
	public synchronized void storeChunk(MapChunk chunk) {
		ChunkPos pos = chunk.getPos();
		if (pos.getRegionX() != x || pos.getRegionZ() != z) return;
		
		int imgX = (pos.x - (x << 5)) << 4;
		int imgY = (pos.z - (z << 5)) << 4;			
		ImageUtil.writeIntoImage(chunk.getImage(), getImage(chunk.getLayer(), chunk.getLevel()), imgX, imgY);
	}
	
	public int getX() {
		return this.x;
	}
	
	public int getZ() {
		return this.z;
	}
	
	public synchronized void saveImage(Layer layer, int level) {		
		int dim = world.getDimension().getType().getRawId();
		File dimDir = new File(StorageUtil.cacheDir(), String.format("DIM%d/", dim));
		
		File cacheDir;
		if (layer == Layer.CAVES) {
			cacheDir = new File(dimDir, String.format("caves/%d/", level));
		} else {
			cacheDir = new File(dimDir, "surface/");
		}
		
		File png = new File(cacheDir, String.format("%d.%d.png", x, z));
		
		if (!cacheDir.exists()) {
			cacheDir.mkdirs();
		}
		
		BufferedImage image = getImage(layer, level);
		
		try {
			ImageIO.write(image, "png", png);
		} catch (IOException ex) {
			JustMap.LOGGER.catching(ex);
		}
	}
	
	private class RegionLayer {
		private final Map<Integer, BufferedImage> images;
		
		private RegionLayer() {
			images = new HashMap<>();
		}
		
		private BufferedImage getImage(int level) {
			if (images.containsKey(level)) {
				return images.get(level);
			}
			
			BufferedImage image = new BufferedImage(512, 512, BufferedImage.TYPE_INT_ARGB);
			ImageUtil.fillImage(image, Colors.BLACK);
			
			images.put(level, image);
			
			return image;
		}
	}
}
