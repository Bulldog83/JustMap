package ru.bulldog.justmap.map.data.fast;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.WorldChunk;
import org.jetbrains.annotations.NotNull;

import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.data.RegionPos;

public class MapRegionRegistry implements MapRegionProvider {
	private final Map<RegionPos, DrawableMapRegion> registry = new HashMap<>();

	@Override
	public MapRegion getMapRegion(IMap map, int blockX, int blockZ) {
		RegionPos regionPos = new RegionPos(blockX, blockZ);

		return getOrCreateRegion(regionPos);
	}

	@NotNull
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

	public void updateBlock(BlockPos pos, BlockState state) {
		RegionPos regionPos = new RegionPos(pos.getX(), pos.getZ());
		DrawableMapRegion region = getOrCreateRegion(regionPos);
		region.updateBlock(pos, state);
	}
}
