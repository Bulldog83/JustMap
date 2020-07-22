package ru.bulldog.justmap.map;

public interface IMap {
	abstract int getWidth();
	abstract int getHeight();
	abstract int getScaledWidth();
	abstract int getScaledHeight();
	abstract float getScale();
}
