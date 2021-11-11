package ru.bulldog.justmap.map.data.fast;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapDataManager;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.data.WorldKey;

public class FastMapManager implements MapDataManager {
	public static final FastMapManager MANAGER = new FastMapManager();

	private final Map<World, FastMapWorld> mapWorlds = new HashMap<>();
	private FastMapWorld currentMapWorld;
	World currentWorld;  // package private

	@Override
	public WorldKey getWorldKey() {
		return currentMapWorld.getWorldKey();
	}

	@Override
	public List<WorldKey> registeredWorlds() {
		return mapWorlds.keySet().stream().map(k -> getWorldKey()).toList();
	}

	@Override
	public void setCurrentWorldName(String name) {
		currentMapWorld.setWorldName(name);
	}

	@Override
	public MapRegionProvider getMapRegionProvider() {
		return currentMapWorld.getMapRegionProvider();
	}

	@Override
	public int getMapHeight(Layer mapLayer, int mapLevel, int posX, int posZ) {
		return 0;
	}

	@Override
	public void onServerConnect() {
		JustMapClient.startMapping();


	}

	@Override
	public void onWorldChanged(World world) {
		JustMapClient.startMapping();

		FastMapWorld mapWorld = mapWorlds.get(world);
		if (mapWorld == null) {
			mapWorld = new FastMapWorld(world);
			mapWorlds.put(world, mapWorld);
		}

		currentMapWorld = mapWorld;
		currentWorld = world;
	}

	@Override
	public void onWorldSpawnPosChanged(BlockPos newPos) {

	}

	@Override
	public void onChunkLoad(World world, WorldChunk worldChunk) {
		assert(world == currentWorld);
		currentMapWorld.getMapRegionProvider().updateChunk(worldChunk);
	}

	@Override
	public void onConfigUpdate() {
		JustMapClient.startMapping();


	}

	@Override
	public void onSetBlockState(BlockPos pos, BlockState state, World world) {
		assert(world == currentWorld);
		currentMapWorld.getMapRegionProvider().updateBlock(pos, state);
	}

	@Override
	public void onTick(boolean isServer) {

	}

	@Override
	public void onWorldStop() {
		JustMapClient.stopMapping();
	}
}
