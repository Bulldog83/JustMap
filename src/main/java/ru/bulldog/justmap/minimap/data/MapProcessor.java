package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.StateUtil;

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
		
		int posY = mapChunk.heightmap[x + z * 16];
		
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
			y = newChunk.heightmap[x + z * 16];
		}
		
		y = y < 0 ? yStart : y;
		
		if (layer == Layer.CAVES) {
			int lvlSize = ClientParams.levelSize;
			int level = y / lvlSize;
			for (int i = (lvlSize - 1) + level * lvlSize; i >= level * lvlSize; i--) {
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

	public static int surfaceColor(MapChunk mapChunk, int x, int z) {
		WorldChunk worldChunk = mapChunk.getWorldChunk();
		World world = worldChunk.getWorld();
 
		int y = mapChunk.heightmap[x + z * 16];
		
		if (y < 0) {
			y = getTopBlockY(worldChunk, x, z, world.getEffectiveHeight(), false, Layer.SURFACE);
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
		int y = getTopBlockY(worldChunk, x, z, yMax, false, Layer.CAVES);
		
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
