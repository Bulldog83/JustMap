package ru.bulldog.justmap.map;

import ru.bulldog.justmap.map.data.Layer;

public interface IMap {
	abstract Layer getLayer();
	abstract int getLevel();
	abstract int getWidth();
	abstract int getHeight();
	abstract int getScaledWidth();
	abstract int getScaledHeight();
	abstract float getScale();
}
