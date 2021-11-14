package ru.bulldog.justmap.map.data.fast;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.RegionPos;

public class DrawableMapRegion implements MapRegion {
	private final RegionPos regionPos;
	private final MapRegionLayer surfaceLayer;
	private final MapRegionLayer[] cavesLayers = new MapRegionLayer[Layer.CAVES.getLevels()];
	private final MapRegionLayer[] netherLayers = new MapRegionLayer[Layer.NETHER.getLevels()];

	public DrawableMapRegion(RegionPos regionPos) {
		this.regionPos = regionPos;
		surfaceLayer = new MapRegionLayer(Layer.SURFACE, 0);
		for (int i = 0; i < cavesLayers.length; i++) {
			cavesLayers[i] = new MapRegionLayer(Layer.CAVES, i);
		}
		for (int i = 0; i < netherLayers.length; i++) {
			netherLayers[i] = new MapRegionLayer(Layer.NETHER, i);
		}
	}

	private MapRegionLayer getMapRegionLayerLazy(Layer layer, int level) {
		if (layer.equals(Layer.SURFACE)) {
			return surfaceLayer;
		} else if (layer.equals(Layer.CAVES)) {
			assert(level < Layer.CAVES.getLevels());
			MapRegionLayer mapLayer = cavesLayers[level];
			if (mapLayer == null) {
				mapLayer = new MapRegionLayer(layer, level);
				cavesLayers[level] = mapLayer;
			}
			return mapLayer;
		} else if (layer.equals(Layer.NETHER)) {
			assert(level < Layer.NETHER.getLevels());
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
		if (level < 0) return null;
		return switch (layer) {
			case SURFACE -> surfaceLayer;
			case NETHER  -> (level >= Layer.NETHER.getLevels()) ? null : cavesLayers[level];
			case CAVES   -> (level >= Layer.CAVES.getLevels())  ? null : netherLayers[level];
		};
	}

	@Override
	public void drawLayer(MatrixStack matrices, Layer layer, int level, double x, double y, double width, double height, int imgX, int imgY, int imgW, int imgH) {
		MapRegionLayer mapLayer = getMapRegionLayer(layer, level);
		if (mapLayer != null) {
			mapLayer.draw(matrices, x, y, width, height, imgX, imgY, imgW, imgH);
		}
	}

	@Override
	public RegionPos getPos() {
		return regionPos;
	}

	public void updateChunk(WorldChunk worldChunk) {
		surfaceLayer.updateChunk(worldChunk);
		// FIXME: other layers except surface should be calculated lazily
		for (MapRegionLayer layer : cavesLayers) {
		// FIXME: disable for now
		//	layer.updateChunk(worldChunk);
		}
		// FIXME: only update nether if actually in the nether, how to check?
	}

	public void updateBlock(BlockPos pos) {
		// FIXME: should probably check if the block is on the surface here
		surfaceLayer.updateBlock(pos);
	}
}
