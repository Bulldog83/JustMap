package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import ru.bulldog.justmap.util.colors.ColorUtil;

import java.nio.ByteBuffer;

public class MapChunk {
    private final int relRegX;
    private final int relRegZ;

    private byte[][] colorData = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL];

    public MapChunk(int relRegX, int relRegZ) {
        this.relRegX = relRegX;
        this.relRegZ = relRegZ;
    }

    private int getChunkRelative(int coord) {
        int relCoord = coord % 16;
        return (relCoord < 0) ? relCoord + 16 : relCoord;
    }

    private int getChunkRelativeX(BlockPos blockPos) {
        return getChunkRelative(blockPos.getX());
    }

    private int getChunkRelativeZ(BlockPos blockPos) {
        return getChunkRelative(blockPos.getZ());
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
                int color = blockColor(worldChunk, blockPos);

                setColor(x, z, color);
            }
        }
    }
    
    public void updateBlock(BlockPos blockPos, BlockState blockState) {
        int x = getChunkRelativeX(blockPos);
        int z = getChunkRelativeZ(blockPos);

    //    int color = Colors.INSTANCE.getBlockColor(blockState);
        int color = blockColor(FastMapManager.MANAGER.currentWorld, blockPos, blockState);

        setColor(x, z, color);
    }

    public void writeToTextureBuffer(ByteBuffer buffer) {
        for (int row = 0; row < MapRegionLayer.CHUNK_SIZE; row++) {
            buffer.put((relRegZ * MapRegionLayer.CHUNK_SIZE + row)
                    * MapRegionLayer.REGION_SIZE * MapRegionLayer.BYTES_PER_PIXEL
                    + (relRegX * MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL),
                    colorData[row], 0,
                    MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL);
        }
    }

    private void setColor(int x, int z, int color) {
        int xOffset = x * 4;
        colorData[z][xOffset + 0] = (byte) 0;
        colorData[z][xOffset + 1] = (byte) (color & 255);
        colorData[z][xOffset + 2] = (byte) ((color >> 8) & 255);
        colorData[z][xOffset + 3] = (byte) ((color >> 16) & 255);
    }

    private int blockColor(World world, BlockPos pos, BlockState blockState) {
        return ColorUtil.blockColor(world, blockState, pos);
    }

    private int blockColor(WorldChunk worldChunk, BlockPos pos) {
        World world = worldChunk.getWorld();
        BlockState blockState = worldChunk.getBlockState(pos);

        return blockColor(world, pos, blockState);
    }
}
