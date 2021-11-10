package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.chunk.WorldChunk;
import ru.bulldog.justmap.util.StateUtil;
import ru.bulldog.justmap.util.colors.ColorUtil;

import java.nio.ByteBuffer;
import java.util.Random;

public class MapChunk {
    private final int relRegX;
    private final int relRegZ;
    Random rnd = new Random();

    private byte[][] colorData = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL];

    public MapChunk(int relRegX, int relRegZ) {
        this.relRegX = relRegX;
        this.relRegZ = relRegZ;
    }

    private int getChunkRelativeX(BlockPos blockPos) {
        int x = blockPos.getX() % 16;
        if (x < 0) x += 16;
        return x;
    }

    private int getChunkRelativeZ(BlockPos blockPos) {
        int z = blockPos.getZ() % 16;
        if (z < 0) z += 16;
        return z;
    }

    private int getTopBlockY(WorldChunk worldChunk, int x, int z) {
        return worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
    }

    public void updateChunk(WorldChunk worldChunk) {
        BlockPos.Mutable blockPos = new BlockPos.Mutable();

        for (int x = 0; x < MapRegionLayer.CHUNK_SIZE; x++) {
            blockPos.setX(x);
            for (int z = 0; z < MapRegionLayer.CHUNK_SIZE; z++) {
                blockPos.setZ(z);
                int y = getTopBlockY(worldChunk, x, z);
                blockPos.setY(y);
           //     BlockState blockState = worldChunk.getBlockState(blockPos);
           //     int color = Colors.INSTANCE.getBlockColor(blockState);
                int color = ColorUtil.blockColor(worldChunk, blockPos);

                setColor(x, z, color);
            }
        }
    }
    
    public void updateBlock(BlockPos blockPos, BlockState blockState) {
        int x = getChunkRelativeX(blockPos);
        int z = getChunkRelativeZ(blockPos);

    //    int color = Colors.INSTANCE.getBlockColor(blockState);
        int color = ColorUtil.blockColor(FastMapManager.MANAGER.currentWorld, blockPos, blockState, StateUtil.AIR);

        setColor(x, z, color);
    }

    public void writeToTextureBuffer(ByteBuffer buffer) {
        // FIXME: this is mis-handling x and z. What have we done wrong?
        for (int row = 0; row < MapRegionLayer.CHUNK_SIZE; row++) {
            buffer.put((relRegZ* MapRegionLayer.CHUNK_SIZE + row)* MapRegionLayer.REGION_SIZE * MapRegionLayer.BYTES_PER_PIXEL + (relRegX * MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL), colorData[row], 0, MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL);
        }
    }


    private void setColor(int x, int z, int color) {
        if (color == 0) {
            color = rnd.nextInt();
        }

        int xOffset = x * 4;
        colorData[z][xOffset + 0] = (byte) (255);
        colorData[z][xOffset + 1] = (byte) (color & 255);
        colorData[z][xOffset + 2] = (byte) ((color >> 8) & 255);
        colorData[z][xOffset + 3] = (byte) ((color >> 16) & 255);
    }
}
