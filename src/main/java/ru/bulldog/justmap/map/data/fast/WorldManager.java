package ru.bulldog.justmap.map.data.fast;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import ru.bulldog.justmap.map.data.IWorldManager;
import ru.bulldog.justmap.map.data.WorldKey;

import java.util.List;

public class WorldManager implements IWorldManager {
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
    public WorldData getData() {
        return null;
    }

    @Override
    public void onWorldLoad() {

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
