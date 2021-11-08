package ru.bulldog.justmap.map.data;

import net.minecraft.world.chunk.WorldChunk;

public interface IChunkData {
    IChunkLevel getChunkLevel(Layer layer, int level);
    void updateWorldChunk(WorldChunk lifeChunk);
    boolean updateFullChunk(Layer layer, int level, boolean forceUpdate);
    boolean updateChunkArea(Layer layer, int level, boolean forceUpdate, int x, int z, int width, int height);
}
