package ru.bulldog.justmap.map.data.fast;

import java.nio.ByteBuffer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.util.BlockStateUtil;
import ru.bulldog.justmap.util.colors.Colors;

import static ru.bulldog.justmap.util.colors.ColorUtil.ABGRtoARGB;

public class MapChunk {
	private final int relRegX;
	private final int relRegZ;
	private final Layer layer;
	private final int level;

	// Note that color data has a different layout than the scouted data
	private final byte[][] colorData = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL];

	private final byte[][] solidY = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];
	private final byte[][] transparentY = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];
	private final byte[][] waterY = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];
	private final byte[][] delta = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];

	private final int[][] solidBlock = new int[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];
	private final int[][] transparentBlock = new int[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];

	public MapChunk(int relRegX, int relRegZ, Layer layer, int level) {
		this.relRegX = relRegX;
		this.relRegZ = relRegZ;
		this.layer = layer;
		this.level = level;
	}

	private void examinePos(World world, WorldChunk worldChunk, BlockPos.Mutable thisBlockPos) {
		int posX = thisBlockPos.getX();
		int posZ = thisBlockPos.getZ();

		// Get the highest non-air block
		int y =  worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, posX, posZ);

		int solid_y = -1;
		int transparent_y = -1;
		int water_y = -1;
		BlockState solid_block = BlockStateUtil.AIR;
		BlockState transparent_block = BlockStateUtil.AIR;
		while (solid_y < 0) {
			thisBlockPos.setY(y);
			BlockState blockState = worldChunk.getBlockState(thisBlockPos);
			if (water_y < 0 &&
					(!blockState.getFluidState().isEmpty() && blockState.getFluidState().isIn(FluidTags.WATER))) {
				water_y = y;
			}
			if (transparent_y < 0 &&
					blockState.getMapColor(world, thisBlockPos) == MapColor.CLEAR) {
				transparent_y = y;
				transparent_block = blockState;
			}

			if (blockState.getMapColor(world, thisBlockPos) != MapColor.CLEAR
					&& blockState.getFluidState().isEmpty() && !blockState.getFluidState().isIn(FluidTags.WATER)) {
				solid_y = y;
				solid_block = blockState;
			}
			y--;
			if (y < 0) {
				// got just void
				solid_y = 0;
				solid_block = BlockStateUtil.AIR;
			}
		}

		int xOffset = posX & 15;
		int zOffset = posZ & 15;
		solidY[xOffset][zOffset] = (byte) solid_y;
		transparentY[xOffset][zOffset] = (byte) transparent_y;
		waterY[xOffset][zOffset] = (byte) water_y;

		solidBlock[xOffset][zOffset] = Block.getRawIdFromState(solid_block);
		transparentBlock[xOffset][zOffset] = Block.getRawIdFromState(transparent_block);
	}

	private void calculateDelta(World world, WorldChunk worldChunk, int xOffset, int zOffset) {
		int solid_y = solidY[xOffset][zOffset];

		// For terrain, Vanilla calculate shading according to height difference
		// with north (z-1) neighbor

		// FIXME: this breaks at chunk border!
		int north_solid_y;
		if (zOffset > 0) {
			north_solid_y = solidY[xOffset][zOffset - 1];
		} else {
			// FIXME: fake! use our own height
			north_solid_y = solidY[xOffset][zOffset];
		}

		int my_delta = solid_y - north_solid_y;
		delta[xOffset][zOffset] = (byte) my_delta;
	}

	private int blockColorChunkFromCache(int xOffset, int zOffset) {
		int solid_y = solidY[xOffset][zOffset];
		int transparent_y = transparentY[xOffset][zOffset];
		int water_y = waterY[xOffset][zOffset];
		int y_delta = delta[xOffset][zOffset];

		BlockState solid_block = Block.getStateFromRawId(solidBlock[xOffset][zOffset]);
		BlockState transparent_block =  Block.getStateFromRawId(transparentBlock[xOffset][zOffset]);

		int shade = 0;

		MapColor mapColor;

		if (water_y > solid_y) {
			// For water, calculate shading according to depth
			int waterDepth = water_y - solid_y;
			double shadeArg = waterDepth * 0.1d + (xOffset + zOffset & 1) * 0.2d;
			if (shadeArg < 0.5d) {
				shade = 2;
			} else if (shadeArg > 0.9d) {
				shade = 0;
			} else {
				shade = 1;
			}
			mapColor = MapColor.WATER_BLUE;
		} else {
			// For terrain, calculate shading according to height difference
			// with neighbor
			double shadeArg = y_delta * 4/5.0d + ((xOffset + zOffset & 1) - 0.5d) * 0.4d;
			if (shadeArg > 0.6d) {
				shade = 2;
			} else if (shadeArg < -0.6d) {
				shade = 0;
			} else {
				shade = 1;
			}
			try {
				// We're taking a bit of a chance here. In practice, the
				// implementation of getMapColor() ignores its arguments, but
				// that might change in the future
				mapColor = solid_block.getMapColor(null, null);
			} catch (NullPointerException e) {
				mapColor = MapColor.CLEAR;
			}
		}

		if (mapColor == MapColor.CLEAR) {
			return Colors.BLACK;
		} else {
			return ABGRtoARGB(MapColor.COLORS[mapColor.id].getRenderColor(shade));
		}
	}

	private int getTopBlockY(WorldChunk worldChunk, int x, int z) {
		if (layer == Layer.SURFACE) {
			return getTopBlockYOnSurface(worldChunk, x, z);
		} else {
			return getTopBlockYInLeveledLayer(worldChunk, x, z, false, true);
		}
	}

	private int getTopBlockYOnSurface(WorldChunk worldChunk, int x, int z) {
		return worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
	}

	private int getTopBlockYInLeveledLayer(WorldChunk worldChunk, int posX, int posZ, boolean hideWater, boolean hidePlants) {
		int floor = layer.getHeight() * level;
		int ceiling = layer.getHeight() * (level + 1);
		int y = ceiling;
		BlockPos.Mutable pos = new BlockPos.Mutable();
		pos.set(posX, y, posZ);
		boolean caveFound;
		do {
			if (y < floor) {
				return -1;
			}
			caveFound = BlockStateUtil.isSkippedBlockState(worldChunk.getBlockState(pos), !hideWater, !hidePlants);
			y--;
			pos.set(posX, y, posZ);
		} while (!caveFound);

		boolean bottomFound;
		do {
			if (y < 0) {
				return -1;
			}
			bottomFound = !BlockStateUtil.isSkippedBlockState(worldChunk.getBlockState(pos), !hideWater, !hidePlants);
			y--;
			pos.set(posX, y, posZ);
		} while (!bottomFound);

		// We overstepped by one
		return y+1;
	}

	public void updateChunk(WorldChunk worldChunk) {
		World world = FastMapManager.MANAGER.getFastWorldMapper().getWorld();
		BlockPos.Mutable blockPos = new BlockPos.Mutable();

		for (int x = 0; x < MapRegionLayer.CHUNK_SIZE; x++) {
			blockPos.setX(worldChunk.getPos().getStartX() + x);
			for (int z = 0; z < MapRegionLayer.CHUNK_SIZE; z++) {
				blockPos.setZ(worldChunk.getPos().getStartZ() + z);
				examinePos(world, worldChunk, blockPos);
			}
		}

		for (int x = 0; x < MapRegionLayer.CHUNK_SIZE; x++) {
			for (int z = 0; z < MapRegionLayer.CHUNK_SIZE; z++) {
				calculateDelta(world, worldChunk, x, z);
			}
		}

		for (int x = 0; x < MapRegionLayer.CHUNK_SIZE; x++) {
			for (int z = 0; z < MapRegionLayer.CHUNK_SIZE; z++) {
				int color = blockColorChunkFromCache(x, z);
				setColor(x, z, color);
			}
		}
	}

	public void updateBlock(BlockPos blockPos) {
		int x = blockPos.getX() & 15;
		int z = blockPos.getZ() & 15;
		int relevantY = solidY[x][z];
		if (blockPos.getY() >= relevantY) {
			// Only look at changes to blocks that could possibly affect the map
			BlockPos.Mutable pos = new BlockPos.Mutable();
			pos.set(blockPos);
			World world = FastMapManager.MANAGER.getFastWorldMapper().getWorld();
			WorldChunk worldChunk = world.getWorldChunk(pos);
			examinePos(world, worldChunk, pos);
			calculateDelta(world, worldChunk, x, z);
			// FIXME: We also need to calculate delta on it's neighbor!
			int color = blockColorChunkFromCache(x, z);
			setColor(x, z, color);
		}
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
}
