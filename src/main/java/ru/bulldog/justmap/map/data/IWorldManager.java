package ru.bulldog.justmap.map.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;

public interface IWorldManager {
     // WorldKey management

     WorldKey getWorldKey();

     List<WorldKey> registeredWorlds();

     void setCurrentWorldName(String name);

     // World map management

     IWorldData getData();

     void load();

     void close();

     void update();

     void memoryControl();

     // Callbacks

     void onWorldChanged(World world);

     /** New spawn point set */
     void onWorldPosChanged(BlockPos newPos);

     void onChunkLoad(World world, WorldChunk worldChunk);

     void onConfigUpdate();
}
