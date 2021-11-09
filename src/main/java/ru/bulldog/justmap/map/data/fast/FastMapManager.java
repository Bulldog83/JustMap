package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapDataManager;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.data.WorldKey;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class FastMapManager implements MapDataManager {
    public static final FastMapManager MANAGER = new FastMapManager();

    private final Map<World, FastMapWorld> mapWorlds = new HashMap();
    private FastMapWorld currentMapWorld;
    private World currentWorld;

    @Override
    public WorldKey getWorldKey() {
        return currentMapWorld.getWorldKey();
    }

    @Override
    public List<WorldKey> registeredWorlds() {
        return mapWorlds.keySet().stream().map(k -> getWorldKey()).collect(Collectors.toUnmodifiableList());
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

    }

    @Override
    public void onWorldChanged(World world) {
        FastMapWorld mapWorld = mapWorlds.get(world);
        if (mapWorld == null) {
            mapWorld = new FastMapWorld();
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

    }
}
