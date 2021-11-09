package ru.bulldog.justmap.map.data.fast;

import net.minecraft.world.chunk.WorldChunk;

import java.nio.ByteBuffer;
import java.util.Random;

public class MapChunk {
    private final int relRegX;
    private final int relRegZ;

    private byte[][] colorData = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL];

    public MapChunk(int relRegX, int relRegZ) {
        this.relRegX = relRegX;
        this.relRegZ = relRegZ;
    }

    public void updateChunk(WorldChunk worldChunk) {
        // FIXME: Read actual colors from chunk
        Random rnd = new Random();
        for (int x = 0; x < MapRegionLayer.CHUNK_SIZE; x++) {
            for (int z = 0; z < MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL; z++) {
                colorData[x][z] = (byte) rnd.nextInt(256);
            }
        }
    }
    
    public void writeToTextureBuffer(ByteBuffer buffer) {
        for (int row = 0; row < MapRegionLayer.CHUNK_SIZE; row++) {
            buffer.put((relRegZ* MapRegionLayer.CHUNK_SIZE + row)* MapRegionLayer.REGION_SIZE * MapRegionLayer.BYTES_PER_PIXEL + (relRegX * MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL), colorData[row], 0, MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL);
        }
    }
}
