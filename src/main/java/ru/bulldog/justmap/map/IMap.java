package ru.bulldog.justmap.map;

import net.minecraft.util.math.BlockPos;

import ru.bulldog.justmap.map.data.Layer;

public interface IMap {
	int getWidth();
	int getHeight();
	float getScale();
	Layer getLayer();
	int getLevel();
	BlockPos getCenter();
	boolean isRotated();
}
