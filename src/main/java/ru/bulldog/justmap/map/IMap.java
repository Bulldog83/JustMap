package ru.bulldog.justmap.map;

import net.minecraft.core.BlockPos;
import ru.bulldog.justmap.client.screen.Worldmap;
import ru.bulldog.justmap.map.data.Layer;

public interface IMap {
	abstract int getWidth();
	abstract int getHeight();
	abstract float getScale();
	abstract Layer getLayer();
	abstract int getLevel();
	abstract BlockPos getCenter();
	abstract boolean isRotated();
	
	default boolean isWorldmap() {
		return this instanceof Worldmap;
	}
}
