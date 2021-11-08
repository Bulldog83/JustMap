package ru.bulldog.justmap.map.data.fast;

import net.minecraft.world.chunk.WorldChunk;
import ru.bulldog.justmap.map.data.IChunkData;
import ru.bulldog.justmap.map.data.Layer;

public class ChunkData implements IChunkData {
    @Override
    public ChunkLevel getChunkLevel(Layer layer, int level) {
        return null;
    }

    @Override
    public boolean updateChunkArea(Layer layer, int level, boolean forceUpdate, int x, int z, int width, int height) {
        return false;
    }

    @Override
    public boolean updateFullChunk(Layer layer, int level, boolean forceUpdate) {
        return false;
    }

    @Override
    public void updateWorldChunk(WorldChunk lifeChunk) {

    }
}
