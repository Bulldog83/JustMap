package ru.bulldog.justmap.map.data;

import net.minecraft.world.chunk.WorldChunk;

public interface IChunkData {
    IChunkLevel getChunkLevel(Layer layer, int level);

    boolean updateChunkArea(Layer layer, int level, boolean forceUpdate, int x, int z, int width, int height);

    boolean updateFullChunk(Layer layer, int level, boolean forceUpdate);

    void updateWorldChunk(WorldChunk lifeChunk);
}
