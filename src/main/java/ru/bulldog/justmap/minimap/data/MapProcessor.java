package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.StateUtil;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

public class MapProcessor {	
	public enum Layer {
		SURFACE,
		CAVES
	}
	
	public static int getTopBlockY(MapChunk mapChunk, int x, int z, int yStart, int yStop, boolean skipLiquid, boolean isCaves) {
		WorldChunk worldChunk = mapChunk.getWorldChunk();
		World world = worldChunk.getWorld();
		ChunkPos chunkPos = worldChunk.getPos();		
		BlockPos worldPos = new BlockPos(x + chunkPos.x * 16, 0, z + chunkPos.z * 16);
		
		WorldChunk rightChunk;
		if ((x < 0 || x > 15) || (z < 0 || z > 15)) {			
			rightChunk = world.getWorldChunk(worldPos);
		 
			x = x < 0 ? x + 16 : x > 15 ? x - 16 : x;
			z = z < 0 ? z + 16 : z > 15 ? z - 16 : z;
		} else {
			rightChunk = worldChunk;
		}
		
		chunkPos = rightChunk.getPos();
		
		MapChunk newChunk = MapCache.get(world).getChunk(chunkPos.x, chunkPos.z, true);
		int y = !isCaves ? newChunk.heightmap[x + z * 16] : -1;
		
		if (worldChunk.isEmpty() || !world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
			return y == -1 ? yStart : y;
		}

		worldPos = new BlockPos(worldPos.getX(), y - 1, worldPos.getZ());

		BlockState state;
		
		if (y < 0) {
			y = yStart;
		}
		
		boolean loop = false;
		do {
			worldPos = new BlockPos(worldPos.getX(), y - 1, worldPos.getZ());
			state = world.getBlockState(worldPos);
			loop = skipLiquid ? state.getMaterial().isLiquid() || state.isAir() : state.isAir();
			if (!loop) {
				if (state.getMaterial() == Material.UNDERWATER_PLANT) {
					loop = true;
				}
			}
			y--;
			loop &= y > yStop;
		} while (loop);
		
		return y;
	}
	
	public static int getTopBlockY(MapChunk mapChunk, int x, int z, int yStart, boolean skipLiquid, boolean isCaves) {
		return getTopBlockY(mapChunk, x, z, yStart, 0, skipLiquid, isCaves);
	}
	
	private static int heightDifference(MapChunk mapChunk, int x, int z, int y, boolean isCaves) {
		int current = getTopBlockY(mapChunk, x, z, y, true, isCaves);
		int east = getTopBlockY(mapChunk, x + 1, z, current, true, isCaves);
		int south = getTopBlockY(mapChunk, x, z - 1, current, true, isCaves);

		east -= current;
		south -= current;

		int diff = east - south;
		
		int maxDiff = ClientParams.terrainStrength;
		diff = diff < 0 ? Math.max(-maxDiff, diff) : Math.min(maxDiff, diff);
		
		return diff;
	}

	public static int surfaceColor(MapChunk mapChunk, int x, int z) {
		WorldChunk worldChunk = mapChunk.getWorldChunk();
		World world = worldChunk.getWorld();
 
		int y = mapChunk.heightmap[x + z * 16];
		
		if (y < 0) {
			y = getTopBlockY(mapChunk, x, z, world.getEffectiveHeight(), false, false);
		}

		BlockPos worldPos = new BlockPos(x + worldChunk.getPos().x * 16, y, z + worldChunk.getPos().z * 16);	
		BlockState state = world.getBlockState(worldPos);
	
		if (!StateUtil.isAir(state)) {
			return ColorUtil.blockColor(world, state, worldPos, heightDifference(mapChunk, x, z, y + 1, false));
		}
	
		return Colors.GRAY;
	}
	
	public static int cavesColor(MapChunk mapChunk, int x, int z, int ceiling) {
		WorldChunk worldChunk = mapChunk.getWorldChunk();
		World world = worldChunk.getWorld();
		
		int pY = (int) MinecraftClient.getInstance().player.getY();
		int yMax = pY + ceiling;
		int y = getTopBlockY(mapChunk, x, z, yMax, false, true);
		
		BlockPos worldPos = new BlockPos(x + worldChunk.getPos().x * 16, y, z + worldChunk.getPos().z * 16);
		BlockPos overPos = new BlockPos(worldPos.getX(), worldPos.getY() + 1, worldPos.getZ());
		BlockState state = world.getBlockState(worldPos);
		BlockState stateOver = world.getBlockState(overPos);
	
		if (!StateUtil.isAir(state) && stateOver.isAir()) {
			return ColorUtil.blockColor(world, state, worldPos, heightDifference(mapChunk, x, z, yMax, true));
		}
		
		return Colors.BLACK;
	}
	
	public static int getHeight(World world, BlockPos pos, boolean ignoreLiquid) {
		return getHeight(world, pos, ignoreLiquid, world.getHeight());
	}
	
	public static int getHeight(World world, BlockPos pos, boolean ignoreLiquid, int startHeight) {
		BlockPos.Mutable checkPos = new BlockPos.Mutable(pos.getX(), startHeight, pos.getZ());
		
		while (checkPos.getY() > 0) {
			BlockState state = world.getBlockState(checkPos);
			if (StateUtil.isAir(state) || (ignoreLiquid && state.getMaterial().isLiquid())) {
				checkPos.setY(checkPos.getY() - 1);
				continue;
			}
			
			return checkPos.getY();
		}
		
		return 0;
	}
}
