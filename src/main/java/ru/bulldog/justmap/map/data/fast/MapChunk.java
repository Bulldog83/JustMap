package ru.bulldog.justmap.map.data.fast;

import java.nio.ByteBuffer;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.MapColor;
import net.minecraft.tag.FluidTags;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;
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

	private final byte[][] colorData = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE * MapRegionLayer.BYTES_PER_PIXEL];
	private final byte[][] heightData = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];
	private final int[][] blockStateData = new int[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];

	private final byte[][] solidY = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];
	private final byte[][] transparentY = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];
	private final byte[][] waterY = new byte[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];

	private final BlockState[][] solidBlock = new BlockState[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];
	private final BlockState[][] transparentBlock = new BlockState[MapRegionLayer.CHUNK_SIZE][MapRegionLayer.CHUNK_SIZE];

	public MapChunk(int relRegX, int relRegZ, Layer layer, int level) {
		this.relRegX = relRegX;
		this.relRegZ = relRegZ;
		this.layer = layer;
		this.level = level;
	}

	private int getChunkRelative(int coord) {
		return coord & 15;
	}

	private int getChunkRelativeX(BlockPos blockPos) {
		return getChunkRelative(blockPos.getX());
	}

	private int getChunkRelativeZ(BlockPos blockPos) {
		return getChunkRelative(blockPos.getZ());
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

		solidBlock[xOffset][zOffset] = solid_block;
		transparentBlock[xOffset][zOffset] = transparent_block;
	}

	private int blockColorChunkFromCache(World world, WorldChunk worldChunk, BlockPos pos) {
		int xOffset = pos.getX() & 15;
		int zOffset = pos.getZ() & 15;
		int solid_y = solidY[xOffset][zOffset];
		int transparent_y = transparentY[xOffset][zOffset];
		int water_y = waterY[xOffset][zOffset];

		BlockState solid_block = solidBlock[xOffset][zOffset];
		BlockState transparent_block = transparentBlock[xOffset][zOffset];

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
			// with north (z-1) neighbor
			// FIXME: this breaks at chunk border!
			int north_solid_y;
			if (zOffset > 0) {
				north_solid_y = solidY[xOffset][zOffset - 1];
			} else {
				// FIXME: fake! use our own height
				north_solid_y = solidY[xOffset][zOffset];
			}

			double shadeArg = (solid_y - north_solid_y) * 4/5.0d
					+ ((xOffset + zOffset & 1) - 0.5d) * 0.4d;
			if (shadeArg > 0.6d) {
				shade = 2;
			} else if (shadeArg < -0.6d) {
				shade = 0;
			} else {
				shade = 1;
			}
			mapColor = solid_block.getMapColor(world, pos);
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

				int color = blockColorChunkFromCache(world, worldChunk, blockPos);
				setColor(x, z, color);
			}
		}
	}

	public void updateBlock(BlockPos blockPos, BlockState blockState) {
		int x = getChunkRelativeX(blockPos);
		int z = getChunkRelativeZ(blockPos);

	//    int color = Colors.INSTANCE.getBlockColor(blockState);
		int color = blockColorPos(FastMapManager.MANAGER.getFastWorldMapper().getWorld(), blockPos, blockState);

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

	private void setHeight(int x, int z, int height) {
		heightData[z][x] = (byte) height;
	}

	private int getHeight(int x, int z) {
		if (x < 0 || z < 0) return 0;

		return heightData[z][x];
	}

	public BlockState getBlockState(int x, int z) {
		return Block.getStateFromRawId(blockStateData[z][x]);
	}

	public void setBlockState(int x, int z, BlockState blockState) {
		blockStateData[z][x] = Block.getRawIdFromState(blockState);
	}


	private int blockColorChunk(WorldChunk worldChunk, BlockPos pos) {
		// return ColorUtil.blockColor(world, blockState, pos);
		return blockColorVanilla(FastMapManager.MANAGER.getFastWorldMapper().getWorld(), worldChunk, pos);
	}

	private int blockColorPos(World world, BlockPos pos, BlockState blockState) {
		WorldChunk worldChunk = world.getWorldChunk(pos);
		return blockColorVanilla(world, worldChunk, pos);
	}

	private int blockColorVanilla(World world, WorldChunk worldChunk, BlockPos pos) {
		ChunkPos chunkPos = worldChunk.getPos();
		int chunkRelX = getChunkRelativeX(pos);
		int chunkRelZ = getChunkRelativeZ(pos);
		int shade = 0;

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
				if (blockState.getFluidState().isIn(FluidTags.WATER)) {
					// Calculate water depth
					BlockPos.Mutable fluidBlockPos = new BlockPos.Mutable();
					fluidBlockPos.set(thisBlockPos);
					int fluidMaxY = maxY;
					BlockState fluidBlockState;
					do {
						fluidMaxY--;
						fluidBlockPos.setY(fluidMaxY);
						fluidBlockState = worldChunk.getBlockState(fluidBlockPos);
					} while (fluidMaxY > world.getBottomY() && !fluidBlockState.getFluidState().isEmpty());

					// For water, calculate shading according to depth
					int waterDepth = maxY - fluidMaxY;
					double shadeArg = waterDepth * 0.1d + (chunkRelX + chunkRelZ & 1) * 0.2d;
					if (shadeArg < 0.5d) {
						shade = 2;
					} else if (shadeArg > 0.9d) {
						shade = 0;
					} else {
						shade = 1;
					}
				}

				// Set block state from fluid instead, if applicable
				if (!blockState.isSideSolidFullSquare(world, thisBlockPos, Direction.UP)) {
					blockState = blockState.getFluidState().getBlockState();
				}
			} else {
				// For terrain, calculate shading according to height difference
				// with north (z-1) neighbor
				// FIXME: this breaks at chunk border!
				double northMaxY = getHeight(chunkRelX, chunkRelZ - 1);
				double shadeArg = (maxY - northMaxY) * 4/5.0d
						+ ((chunkRelX + chunkRelZ & 1) - 0.5d) * 0.4d;
				if (shadeArg > 0.6d) {
					shade = 2;
				} else if (shadeArg < -0.6d) {
					shade = 0;
				} else {
					shade = 1;
				}
			}
			mapColor = blockState.getMapColor(world, thisBlockPos);
			setBlockState(chunkRelX, chunkRelZ, blockState);
		}

		if (mapColor == MapColor.CLEAR) {
			return Colors.BLACK;
		} else {
			return ABGRtoARGB(MapColor.COLORS[mapColor.id].getRenderColor(shade));
		}
	}
}
