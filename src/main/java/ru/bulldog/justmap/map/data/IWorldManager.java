package ru.bulldog.justmap.map.data;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import java.util.List;

public interface IWorldManager {
    static void onConfigUpdate() {
        WorldManager.onConfigUpdate();
    }

    static void load() {
        WorldManager.load();
    }

    static void onWorldPosChanged(BlockPos newPos) {
        WorldManager.onWorldPosChanged(newPos);
    }

    static WorldKey getWorldKey() {
        return WorldManager.getWorldKey();
    }

    static WorldData getData() {
        return WorldManager.getData();
    }

    static void onChunkLoad(World world, WorldChunk worldChunk) {
        WorldManager.onChunkLoad(world, worldChunk);
    }

    static void update() {
        WorldManager.update();
    }

    static void memoryControl() {
        WorldManager.memoryControl();
    }

    static void close() {
        WorldManager.close();
    }

    static void onWorldChanged(World world) {
        WorldManager.onWorldChanged(world);
    }

    static List<WorldKey> registeredWorlds() {
        return WorldManager.registeredWorlds();
    }

    static void setCurrentWorldName(String name) {
        WorldManager.setCurrentWorldName(name);
    }
}
