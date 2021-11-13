package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.RegionPos;

public class DrawableMapRegion implements MapRegion {
	private final static int WORLD_HEIGHT = 256;
	private final static int NETHER_LEVELS = WORLD_HEIGHT / Layer.NETHER.height;
	private final static int CAVES_LEVELS = WORLD_HEIGHT / Layer.CAVES.height;
	private final RegionPos regionPos;
	private final MapRegionLayer surfaceLayer;
	private final MapRegionLayer[] cavesLayers = new MapRegionLayer[NETHER_LEVELS];
	private final MapRegionLayer[] netherLayers = new MapRegionLayer[NETHER_LEVELS];

	public DrawableMapRegion(RegionPos regionPos) {
		this.regionPos = regionPos;
		surfaceLayer = new MapRegionLayer(Layer.SURFACE, 0);
		for (int i = 0; i < CAVES_LEVELS; i++) {
			cavesLayers[i] = new MapRegionLayer(Layer.CAVES, i);
		}
		for (int i = 0; i < NETHER_LEVELS; i++) {
			netherLayers[i] = new MapRegionLayer(Layer.NETHER, i);
		}
	}

	private MapRegionLayer getMapRegionLayerLazy(Layer layer, int level) {
		if (layer.equals(Layer.SURFACE)) {
			return surfaceLayer;
		} else if (layer.equals(Layer.CAVES)) {
			assert(level < CAVES_LEVELS);
			MapRegionLayer mapLayer = cavesLayers[level];
			if (mapLayer == null) {
				mapLayer = new MapRegionLayer(layer, level);
				cavesLayers[level] = mapLayer;
			}
			return mapLayer;
		} else if (layer.equals(Layer.NETHER)) {
			assert(level < NETHER_LEVELS);
			MapRegionLayer mapLayer = cavesLayers[level];
			if (mapLayer == null) {
				mapLayer = new MapRegionLayer(layer, level);
				cavesLayers[level] = mapLayer;
			}
			return mapLayer;
		} else {
			assert(false);
			return null;
		}
	}

	private MapRegionLayer getMapRegionLayer(Layer layer, int level) {
		if (layer.equals(Layer.SURFACE)) {
			return surfaceLayer;
		} else if (layer.equals(Layer.CAVES)) {
			assert(level < CAVES_LEVELS);
			return cavesLayers[level];
		} else if (layer.equals(Layer.NETHER)) {
			assert(level < NETHER_LEVELS);
			return netherLayers[level];
		} else {
			assert(false);
			return null;
		}
	}

	@Override
	public void drawLayer(MatrixStack matrices, Layer layer, int level, double x, double y, double width, double height, int imgX, int imgY, int imgW, int imgH) {
		getMapRegionLayer(layer, level).draw(matrices, x, y, width, height, imgX, imgY, imgW, imgH);
	}

	@Override
	public RegionPos getPos() {
		return regionPos;
	}

	public void updateChunk(WorldChunk worldChunk) {
		surfaceLayer.updateChunk(worldChunk);
		// FIXME: other layers except surface should be calculated lazily
		for (MapRegionLayer layer : cavesLayers) {
			layer.updateChunk(worldChunk);
		}
		// FIXME: only update nether if actually in the nether, how to check?
	}

	public void updateBlock(BlockPos pos, BlockState state) {
		// FIXME: should probably check if the block is on the surface here
		surfaceLayer.updateBlock(pos, state);
	}
}
