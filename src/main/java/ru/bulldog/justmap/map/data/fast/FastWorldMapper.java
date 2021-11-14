package ru.bulldog.justmap.map.data.fast;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.RegionPos;
import ru.bulldog.justmap.map.data.WorldMapper;

public class FastWorldMapper implements WorldMapper {
	private final Map<RegionPos, DrawableMapRegion> registry = new HashMap<>();
	private final World world;

	public FastWorldMapper(World world) {
		this.world = world;
	}

	public World getWorld() {
		return world;
	}

	@Override
	public MapRegion getMapRegion(IMap map, int blockX, int blockZ) {
		RegionPos regionPos = new RegionPos(blockX, blockZ);

		return getOrCreateRegion(regionPos);
	}

	@Override
	public int getMapHeight(Layer mapLayer, int mapLevel, int posX, int posZ) {
		// FIXME: implement
		return 0;
	}

	@Override
	public void onWorldMapperClose() {
		// do nothing
	}

	private DrawableMapRegion getOrCreateRegion(RegionPos regionPos) {
		DrawableMapRegion region = registry.get(regionPos);
		if (region == null) {
			region = new DrawableMapRegion(regionPos);
			registry.put(regionPos, region);
		}
		return region;
	}

	public void updateChunk(WorldChunk worldChunk) {
		RegionPos regionPos = new RegionPos(worldChunk.getPos());
		DrawableMapRegion region = getOrCreateRegion(regionPos);
		region.updateChunk(worldChunk);
	}

	public void updateBlock(BlockPos pos) {
		RegionPos regionPos = new RegionPos(pos.getX(), pos.getZ());
		DrawableMapRegion region = getOrCreateRegion(regionPos);
		region.updateBlock(pos);
	}
}
