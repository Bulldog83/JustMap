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
	
	public static BlockMeta getTopBlock(MapChunk mapChunk, Layer layer, int x, int z) {
		WorldChunk chunk = mapChunk.getWorldChunk();
		
		int posX = x + chunk.getPos().x * 16;
		int posZ = z + chunk.getPos().z * 16;
		
		int posY = mapChunk.heightmap[x + z * 16];
		
		if (posY < 0) {
			posY = getTopBlockY(chunk, x, z, chunk.getWorld().getEffectiveHeight(), false, false);
		}
		
		return new BlockMeta(new BlockPos(posX, posY, posZ));
	}
	
	public static int getTopBlockY(WorldChunk worldChunk, int x, int z, int yStart, int yStop, boolean skipLiquid, boolean isCaves) {
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
		
		int y;
		if (isCaves) {
			y = -1;
		} else {
			MapChunk newChunk = MapCache.get(world).getChunk(chunkPos.x, chunkPos.z, true);
			y = newChunk.heightmap[x + z * 16];
		}
		
		if (worldChunk.isEmpty() || !world.getChunkManager().isChunkLoaded(chunkPos.x, chunkPos.z)) {
			return y == -1 ? yStart : y;
		}

		if (y < 0) {
			y = yStart;
		}
		
		worldPos = new BlockPos(worldPos.getX(), y, worldPos.getZ());
		
		boolean loop = false;
		
		BlockState state;
		do {
			state = world.getBlockState(worldPos);
			loop = skipLiquid ? state.getMaterial().isLiquid() || state.isAir() : state.isAir();
			if (!loop) {
				if (state.getMaterial() == Material.UNDERWATER_PLANT) {
					loop = true;
				}
			}
			worldPos = worldPos.down();
			loop &= worldPos.getY() > yStop;
		} while (loop);
		
		return y;
	}
	
	public static int getTopBlockY(WorldChunk worldChunk, int x, int z, int yStart, boolean skipLiquid, boolean isCaves) {
		return getTopBlockY(worldChunk, x, z, yStart, 0, skipLiquid, isCaves);
	}
	
	public static int heightDifference(WorldChunk worldChunk, int x, int z, int y, boolean isCaves) {
		int current = getTopBlockY(worldChunk, x, z, y, true, isCaves);
		int east = getTopBlockY(worldChunk, x + 1, z, current, true, isCaves);
		int south = getTopBlockY(worldChunk, x, z - 1, current, true, isCaves);

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
			y = getTopBlockY(worldChunk, x, z, world.getEffectiveHeight(), false, false);
		}

		BlockPos worldPos = new BlockPos(x + worldChunk.getPos().x * 16, y, z + worldChunk.getPos().z * 16);	
		BlockState state = world.getBlockState(worldPos);
	
		if (!StateUtil.isAir(state)) {
			return ColorUtil.blockColor(world, state, worldPos);
		}
	
		return Colors.GRAY;
	}
	
	public static int cavesColor(MapChunk mapChunk, int x, int z, int ceiling) {
		WorldChunk worldChunk = mapChunk.getWorldChunk();
		World world = worldChunk.getWorld();
		
		int pY = (int) MinecraftClient.getInstance().player.getY();
		int yMax = pY + ceiling;
		int y = getTopBlockY(worldChunk, x, z, yMax, false, true);
		
		BlockPos worldPos = new BlockPos(x + worldChunk.getPos().x * 16, y, z + worldChunk.getPos().z * 16);
		BlockPos overPos = new BlockPos(worldPos.getX(), worldPos.getY() + 1, worldPos.getZ());
		BlockState state = world.getBlockState(worldPos);
		BlockState stateOver = world.getBlockState(overPos);
	
		if (!StateUtil.isAir(state) && stateOver.isAir()) {
			return ColorUtil.blockColor(world, state, worldPos);
		}
		
		return Colors.BLACK;
	}
}
