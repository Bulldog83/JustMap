package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import ru.bulldog.justmap.util.colors.Colors;

import java.nio.ByteBuffer;

import static ru.bulldog.justmap.util.colors.ColorUtil.ABGRtoARGB;

public class MapChunk {
    private final int relRegX;
    private final int relRegZ;

    private final byte[][] colorData = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL];

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
                int color = blockColorChunk(worldChunk, blockPos);

                setColor(x, z, color);
            }
        }
    }
    
    public void updateBlock(BlockPos blockPos, BlockState blockState) {
        int x = getChunkRelativeX(blockPos);
        int z = getChunkRelativeZ(blockPos);

    //    int color = Colors.INSTANCE.getBlockColor(blockState);
        int color = blockColorPos(FastMapManager.MANAGER.currentWorld, blockPos, blockState);

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

    private int blockColorChunk(WorldChunk worldChunk, BlockPos pos) {
        // return ColorUtil.blockColor(world, blockState, pos);
        return blockColorVanilla(FastMapManager.MANAGER.currentWorld, worldChunk, pos);
    }

    private int blockColorPos(World world, BlockPos pos, BlockState blockState) {
        WorldChunk worldChunk = world.getWorldChunk(pos);
        return blockColorVanilla(world, worldChunk, pos);
    }

    private int blockColorVanilla(World world, WorldChunk worldChunk, BlockPos pos) {
        ChunkPos chunkPos = worldChunk.getPos();
        int chunkRelX = getChunkRelativeX(pos);
        int chunkRelZ = getChunkRelativeZ(pos);

        // FIXME: Redundant if coming here through chunk updates
        int maxY = getTopBlockY(worldChunk, chunkRelX, chunkRelZ);
        MapColor mapColor;
        if (maxY <= world.getBottomY()) {
            mapColor = MapColor.CLEAR;
        } else {
            BlockPos.Mutable thisBlockPos = new BlockPos.Mutable();
            thisBlockPos.set(pos);

            // Find top-most solid block (without mapcolor "clear")
            BlockState blockState = worldChunk.getBlockState(thisBlockPos);
            while (blockState.getMapColor(world, thisBlockPos) == MapColor.CLEAR && maxY > world.getBottomY()) {
                maxY--;
                thisBlockPos.set(chunkPos.getStartX() + chunkRelX, maxY, chunkPos.getStartZ() + chunkRelZ);
                blockState = worldChunk.getBlockState(thisBlockPos);
            }

            // Is top-most block a fluid?
            if (maxY > world.getBottomY() && !blockState.getFluidState().isEmpty()) {
                // Set block state from fluid instead, if applicable
                if (!blockState.isSideSolidFullSquare(world, thisBlockPos, Direction.UP)) {
                    blockState = blockState.getFluidState().getBlockState();
                }
            }
            mapColor = blockState.getMapColor(world, thisBlockPos);
        }

        if (mapColor == MapColor.CLEAR) {
            return Colors.BLACK;
        } else {
            return ABGRtoARGB(MapColor.COLORS[mapColor.id].getRenderColor(0));
        }
    }
}
