package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapDataManager;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.data.WorldKey;

import java.util.List;

public class FastMapManager implements MapDataManager {
    @Override
    public WorldKey getWorldKey() {
        return null;
    }

    @Override
    public List<WorldKey> registeredWorlds() {
        return null;
    }

    @Override
    public void setCurrentWorldName(String name) {

    }

    @Override
    public MapRegionProvider getMapRegionProvider() {
        return null;
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

    }

    @Override
    public void onWorldSpawnPosChanged(BlockPos newPos) {

    }

    @Override
    public void onChunkLoad(World world, WorldChunk worldChunk) {

    }

    @Override
    public void onConfigUpdate() {

    }

    @Override
    public void onSetBlockState(BlockPos pos, BlockState state, World world) {

    }

    @Override
    public void onTick(boolean isServer) {

    }

    @Override
    public void onWorldStop() {

    }
}
