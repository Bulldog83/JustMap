package ru.bulldog.justmap.map.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.levelgen.Heightmap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.StateUtil;

public class MapProcessor {
	
	public static int getTopBlockY(LevelChunk worldChunk, Layer layer, int level, int x, int z, boolean liquids) {
		int yws = worldChunk.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
		int ymb = worldChunk.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
		int y = Math.max(yws, ymb);
		
		if (y < 0) return -1;
		
		return getTopBlockY(worldChunk, layer, level, x, y, z, liquids);
	}
	
	public static int getTopBlockY(LevelChunk worldChunk, Layer layer, int level, int x, int y, int z, boolean liquids) {
		if (worldChunk == null || worldChunk.isEmpty()) return -1;
		
		ChunkPos chunkPos = worldChunk.getPos();
		int posX = x + (chunkPos.x << 4);
		int posZ = z + (chunkPos.z << 4);
		
		boolean plants = !ClientSettings.hidePlants;
		if ((layer.equals(Layer.NETHER) || layer.equals(Layer.CAVES))) {
			int floor = level * layer.height;
			for (int i = floor + (layer.height - 1); i >= floor; i--) {
				BlockPos worldPos = loopPos(worldChunk, new BlockPos(posX, i, posZ), 0, liquids, plants);
				BlockPos overPos = new BlockPos(posX, worldPos.getY() + 1, posZ);
				if (StateUtil.checkState(worldChunk.getBlockState(overPos), liquids, plants)) {
					return worldPos.getY();
				}
			}
		} else {
			BlockPos worldPos = loopPos(worldChunk, new BlockPos(posX, y, posZ), 0, liquids, plants);
			BlockState overState = worldChunk.getBlockState(new BlockPos(posX, worldPos.getY() + 1, posZ));
			if (StateUtil.checkState(overState, liquids, plants)) {
				return worldPos.getY();
			}
		}
		
		return -1;
	}
	
	private static BlockPos loopPos(LevelChunk worldChunk, BlockPos pos, int stop, boolean liquids, boolean plants) {
		boolean loop = false;		
		do {
			loop = StateUtil.checkState(worldChunk.getBlockState(pos), liquids, plants);			
			loop &= pos.getY() > stop;
			if (loop) pos = pos.below();
		} while (loop);
		
		return pos;
	}
	
	private static int checkLiquids(LevelChunk worldChunk, Layer layer, int level, int x, int y, int z) {
		if (y == -1 || worldChunk == null || worldChunk.isEmpty()) return 0;
		
		BlockPos pos = new BlockPos(x + (worldChunk.getPos().x << 4), y, z + (worldChunk.getPos().z << 4));
		BlockState state = worldChunk.getBlockState(pos);
		if (StateUtil.isLiquid(state, false)) {
			y = getTopBlockY(worldChunk, layer, level, x, y, z, false);
		}
		
		return y;
	}
	
	public static int heightDifference(ChunkData mapChunk, Layer layer, int level, int x, int y, int z, boolean skipWater) {
		int ex = x + 1;
		int sz = z - 1;
		
		int east, south;
		ChunkPos pos = mapChunk.getPos();
		Level world = mapChunk.getWorldChunk().getLevel();
		ChunkLevel chunkLevel = mapChunk.getChunkLevel(layer, level);
		if (ex > 15) {			
			ex -= 16;
			LevelChunk eastChunk = world.getChunk(pos.x + 1, pos.z);
			east = getTopBlockY(eastChunk, layer, level, ex, z, skipWater);
			east = checkLiquids(eastChunk, layer, level, ex, east, z);
		} else {
			east = chunkLevel.sampleHeightmap(ex, z);
			east = checkLiquids(mapChunk.getWorldChunk(), layer, level, ex, east, z);
		}
		if (sz < 0) {
			sz += 16;
			LevelChunk southChunk = world.getChunk(pos.x, pos.z - 1);
			south = getTopBlockY(southChunk, layer, level, x, sz, skipWater);
			south = checkLiquids(southChunk, layer, level, x, south, sz);
		} else {			
			south = chunkLevel.sampleHeightmap(x, sz);
			south = checkLiquids(mapChunk.getWorldChunk(), layer, level, x, south, sz);
		}
		
		y = checkLiquids(mapChunk.getWorldChunk(), layer, level, x, y, z);
		
		east = east > 0 ? east - y : 0;
		south = south > 0 ? south - y : 0;

		int diff = east - south;
		if (diff == 0) return 0;
		
		int maxDiff = ClientSettings.terrainStrength;
		diff = diff < 0 ? Math.max(-maxDiff, diff) : Math.min(maxDiff, diff);
		
		return diff;
	}
}
