package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.RegionPos;

public class DrawableMapRegion implements MapRegion {
    private final RegionPos regionPos;
    private final MapRegionLayer surfaceLayer;

    public DrawableMapRegion(RegionPos regionPos) {
        this.regionPos = regionPos;
        surfaceLayer = new MapRegionLayer();
    }

    private MapRegionLayer getMapRegionLayer(Layer layer, int level) {
        if (layer.equals(Layer.SURFACE)) {
            return surfaceLayer;
        } else {
            // FIXME: implement nether and caves, using a level-based array
            throw new UnsupportedOperationException();
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
        // FIXME: other layers except surface should probably be calculated lazily..?
        surfaceLayer.updateChunk(worldChunk);
    }

    public void updateBlock(BlockPos pos, BlockState state) {
        // FIXME: should probably check if this is on the surface here
        surfaceLayer.updateBlock(pos, state);
    }
}
