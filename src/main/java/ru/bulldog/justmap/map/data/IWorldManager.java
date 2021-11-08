package ru.bulldog.justmap.map.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;

public interface IWorldManager {
     void onConfigUpdate();

     void load();

     void onWorldPosChanged(BlockPos newPos);

     WorldKey getWorldKey();

     IWorldData getData();

     void onChunkLoad(World world, WorldChunk worldChunk);

     void update();

     void memoryControl();

     void close();

     void onWorldChanged(World world);

     List<WorldKey> registeredWorlds();

     void setCurrentWorldName(String name);
}
