package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.client.config.ClientParams;

import net.minecraft.block.BlockState;
import net.minecraft.block.Material;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
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
		
		int posY = mapChunk.getHeighmap()[x + (z << 4)];
		
		if (layer == Layer.CAVES) {
			PlayerEntity player = MinecraftClient.getInstance().player;
			posY = getTopBlockY(chunk, x, z, player.getBlockPos().getY(), false, layer);
		} else if (posY < 0) {
			posY = getTopBlockY(chunk, x, z, chunk.getWorld().getEffectiveHeight(), false, layer);
		}		
		
		return new BlockMeta(new BlockPos(posX, posY, posZ));
	}
	
	public static int getTopBlockY(WorldChunk worldChunk, int x, int z, int yStart, int yStop, boolean skipLiquid, Layer layer) {
		World world = worldChunk.getWorld();
		ChunkPos chunkPos = worldChunk.getPos();		
		
		int posX = x + chunkPos.x * 16;
		int posZ = z + chunkPos.z * 16;
		
		BlockPos worldPos = new BlockPos(posX, 0, posZ);
		
		WorldChunk rightChunk;
		if ((x < 0 || x > 15) || (z < 0 || z > 15)) {			
			rightChunk = world.getWorldChunk(worldPos);
		 
			x = x < 0 ? x + 16 : x > 15 ? x - 16 : x;
			z = z < 0 ? z + 16 : z > 15 ? z - 16 : z;
		} else {
			rightChunk = worldChunk;
		}
		
		chunkPos = rightChunk.getPos();
		
		int y = -1;
		if (layer == Layer.CAVES) {
			y = yStart;
		} else {
			MapChunk newChunk = MapCache.get(world).getChunk(chunkPos.x, chunkPos.z, true);
			y = newChunk.getHeighmap()[x + (z << 4)];
		}
		
		y = y < 0 ? yStart : y;
		
		if (layer == Layer.CAVES) {
			int cls = ClientParams.levelSize;
			int level = y >> cls;
			for (int i = ((int) Math.pow(2, cls) - 1) + (level << cls); i >= level << cls; i--) {
				worldPos = loopPos(world, new BlockPos(posX, i, posZ), yStop, skipLiquid);
				BlockPos overPos = new BlockPos(posX, worldPos.getY() + 1, posZ);
				if (world.getBlockState(overPos).isAir()) {
					return worldPos.getY();
				}
			}
		} else {
			worldPos = loopPos(world, new BlockPos(posX, y, posZ), yStop, skipLiquid);
		}
		
		return worldPos.getY();
	}
	
	private static BlockPos loopPos(World world, BlockPos pos, int stop, boolean skipLiquid) {
		boolean loop = false;
		
		BlockState state;
		do {
			state = world.getBlockState(pos);
			loop = skipLiquid ? state.getMaterial().isLiquid() || state.isAir() : state.isAir();
			if (!loop && state.getMaterial() == Material.UNDERWATER_PLANT) {
				loop = true;
			}
			loop &= pos.getY() > stop;
			if (loop) {
				pos = pos.down();
			}
		} while (loop);
		
		return pos;
	}
	
	public static int getTopBlockY(WorldChunk worldChunk, int x, int z, int yStart, boolean skipLiquid, Layer layer) {
		return getTopBlockY(worldChunk, x, z, yStart, 0, skipLiquid, layer);
	}
	
	public static int heightDifference(WorldChunk worldChunk, int x, int z, int y, Layer layer) {
		int current = getTopBlockY(worldChunk, x, z, y, true, layer);
		int east = getTopBlockY(worldChunk, x + 1, z, current, true, layer);
		int south = getTopBlockY(worldChunk, x, z - 1, current, true, layer);

		east -= current;
		south -= current;

		int diff = east - south;
		
		int maxDiff = ClientParams.terrainStrength;
		diff = diff < 0 ? Math.max(-maxDiff, diff) : Math.min(maxDiff, diff);
		
		return diff;
	}
}
