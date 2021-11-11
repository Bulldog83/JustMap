package ru.bulldog.justmap.map.data.fast;

import net.minecraft.world.World;
import ru.bulldog.justmap.map.data.WorldKey;

public class FastMapWorld {
	private final MapRegionRegistry registry = new MapRegionRegistry();
	private final WorldKey worldKey;

	public FastMapWorld(World world) {
		this.worldKey = new WorldKey(world.getRegistryKey().getValue());
	}

	public WorldKey getWorldKey() {
		return worldKey;
	}

	public void setWorldName(String name) {
		// FIXME: should not be a no-op
	}

	public MapRegionRegistry getMapRegionProvider() {
		return registry;
	}
}
