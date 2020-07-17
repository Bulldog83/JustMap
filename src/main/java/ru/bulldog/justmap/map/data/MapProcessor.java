package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.StateUtil;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

public class MapProcessor {
	
	public static int getTopBlockY(MapChunk mapChunk, int x, int y, int z, boolean liquids) {
		WorldChunk worldChunk = mapChunk.getWorldChunk();
		if (worldChunk == null || worldChunk.isEmpty()) return -1;
		
		ChunkPos chunkPos = worldChunk.getPos();
		int posX = x + (chunkPos.x << 4);
		int posZ = z + (chunkPos.z << 4);
		
		boolean plants = !ClientParams.hidePlants;
		
		Layer.Type layer = mapChunk.getLayer();
		if ((layer.equals(Layer.Type.NETHER) || layer.equals(Layer.Type.CAVES))) {
			int level = mapChunk.currentLevel();
			int floor = level * layer.value.height;
			for (int i = floor + (layer.value.height - 1); i >= floor; i--) {
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
	
	private static BlockPos loopPos(WorldChunk worldChunk, BlockPos pos, int stop, boolean liquids, boolean plants) {
		boolean loop = false;		
		do {
			loop = StateUtil.checkState(worldChunk.getBlockState(pos), liquids, plants);			
			loop &= pos.getY() > stop;
			if (loop) pos = pos.down();
		} while (loop);
		
		return pos;
	}
	
	private static int checkLiquids(MapChunk mapChunk, int x, int y, int z) {
		WorldChunk worldChunk = mapChunk.getWorldChunk();
		if (y == -1 || worldChunk == null || worldChunk.isEmpty()) return 0;
		
		BlockPos pos = new BlockPos(x + (mapChunk.getX() << 4), y, z + (mapChunk.getZ() << 4));
		BlockState state = worldChunk.getBlockState(pos);
		if (StateUtil.isLiquid(state, false)) {
			y = getTopBlockY(mapChunk, x, y, z, false);
		}
		
		return y;
	}
	
	public static int heightDifference(MapChunk mapChunk, MapChunk eastChunk, MapChunk southChunk, int x, int y, int z) {
		int ex = x + 1;
		int sz = z - 1;
		
		int east, south;
		if (ex > 15) {			
			ex -= 16;
			east = eastChunk.getHeighmap()[ex + (z << 4)];			
			east = checkLiquids(eastChunk, ex, east, z);
		} else {
			east = mapChunk.getHeighmap()[ex + (z << 4)];
			east = checkLiquids(mapChunk, ex, east, z);
		}
		if (sz < 0) {			
			sz += 16;
			south = southChunk.getHeighmap()[x + (sz << 4)];
			south = checkLiquids(southChunk, x, south, sz);
		} else {			
			south = mapChunk.getHeighmap()[x + (sz << 4)];
			south = checkLiquids(mapChunk, x, south, sz);
		}
		
		y = checkLiquids(mapChunk, x, y, z);
		
		east = east > 0 ? east - y : 0;
		south = south > 0 ? south - y : 0;

		int diff = east - south;
		if (diff == 0) return 0;
		
		int maxDiff = ClientParams.terrainStrength;
		diff = diff < 0 ? Math.max(-maxDiff, diff) : Math.min(maxDiff, diff);
		
		return diff;
	}
}
