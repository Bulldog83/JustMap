package ru.bulldog.justmap.util.math;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

import java.util.Objects;

public class Point {
	
	public double x;
	public double y;
	
	public static Point fromPos(BlockPos pos) {
		return new Point(pos.getX(), pos.getZ());
	}
	
	public static Point fromPos(ChunkPos pos) {
		return new Point(pos.x, pos.z);
	}
	
//	public static Point fromPos(RegionPos pos) {
//		return new Point(pos.x, pos.z);
//	}
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double distance(Point target) {
		return Line.length(x, y, target.x, target.y);
	}
	
	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if(!(obj instanceof Point)) return false;
		Point point = (Point) obj;
		return x == point.x && y == point.y;
	}
	
	public String shortString() {
		return String.format("[%f, %f]", x, y);
	}
	
	@Override
	public String toString() {
		return String.format("Point %s", this.shortString());
	}
}