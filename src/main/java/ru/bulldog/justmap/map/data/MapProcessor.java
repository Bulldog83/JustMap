package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.StateUtil;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class MapProcessor {	
	public enum Layer {
		SURFACE,
		CAVES
	}
	
	public static int getTopBlockY(MapChunk mapChunk, int x, int y, int z, boolean liquids) {
		WorldChunk worldChunk = mapChunk.getWorldChunk();
		World world = worldChunk.getWorld();
		ChunkPos chunkPos = worldChunk.getPos();		
		
		int posX = x + (chunkPos.x << 4);
		int posZ = z + (chunkPos.z << 4);
		
		if (mapChunk.getLayer() == Layer.CAVES && liquids) {
			int level = mapChunk.getLevel();
			int cls = ClientParams.chunkLevelSize;
			for (int i = ((int) Math.pow(2, cls) - 1) + (level << cls); i >= level << cls; i--) {
				BlockPos worldPos = loopPos(world, new BlockPos(posX, i, posZ), 0, liquids);
				BlockPos overPos = new BlockPos(posX, worldPos.getY() + 1, posZ);
				if (StateUtil.isAir(world.getBlockState(overPos))) {
					return worldPos.getY();
				}
			}
		} else {
			BlockPos worldPos = loopPos(world, new BlockPos(posX, y, posZ), 0, liquids);
			BlockState overState = world.getBlockState(new BlockPos(posX, worldPos.getY() + 1, posZ));
			if (checkBlockState(overState, liquids)) {
				return worldPos.getY();
			}
		}
		
		return -1;
	}
	
	private static BlockPos loopPos(World world, BlockPos pos, int stop, boolean liquids) {
		boolean loop = false;		
		do {
			loop = checkBlockState(world.getBlockState(pos), liquids);
			
			loop &= pos.getY() > stop;
			if (loop) pos = pos.down();
		} while (loop);
		
		return pos;
	}
	
	private static boolean checkBlockState(BlockState state, boolean liquids) {
		return StateUtil.isAir(state) || (!liquids && StateUtil.isUnderwater(state));
	}
	
	private static int checkLiquids(MapChunk mapChunk, int x, int y, int z) {
		if (y == -1) return 0;
		
		World world = mapChunk.getWorldChunk().getWorld();
		BlockPos pos = new BlockPos(x + (mapChunk.getX() << 4), y, z + (mapChunk.getZ() << 4));
		BlockState state = world.getBlockState(pos);
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
