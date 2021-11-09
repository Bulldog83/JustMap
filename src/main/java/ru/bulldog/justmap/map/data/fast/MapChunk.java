package ru.bulldog.justmap.map.data.fast;

import net.minecraft.world.chunk.WorldChunk;

import java.nio.ByteBuffer;
import java.util.Random;

public class MapChunk {
    private final int relRegX;
    private final int relRegZ;

    private byte[][] colorData = new byte[16][16*4];

    public MapChunk(int relRegX, int relRegZ) {
        this.relRegX = relRegX;
        this.relRegZ = relRegZ;
    }

    public void updateChunk(WorldChunk worldChunk) {
        Random rnd = new Random();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16*4; z++) {
                colorData[x][z] = (byte) rnd.nextInt(256);
            }
        }
    }
    
    public void writeToTextureBuffer(ByteBuffer buffer) {
        for (int row = 0; row < 16; row++) {
            buffer.put((relRegZ*16+row)*512*4 + relRegX*16*4, colorData[row], 0, 16*4);
        }
    }
}
