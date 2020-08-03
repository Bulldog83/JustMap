package ru.bulldog.justmap.util.math;

import java.util.Arrays;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import ru.bulldog.justmap.map.data.RegionPos;

public class Point {
	
	public double x;
	public double y;
	
	public static Point fromPos(BlockPos pos) {
		return new Point(pos.getX(), pos.getZ());
	}
	
	public static Point fromPos(ChunkPos pos) {
		return new Point(pos.x, pos.z);
	}
	
	public static Point fromPos(RegionPos pos) {
		return new Point(pos.x, pos.z);
	}
	
	public Point(double x, double y) {
		this.x = x;
		this.y = y;
	}
	
	public double distance(Point target) {
		return Line.length(x, y, target.x, target.y);
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(new double[]{x, y});
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if(!(obj instanceof Point)) return false;
		
		Point point = (Point) obj;
		return this.x == point.x && this.y == point.y;
	}
	
	@Override
	public String toString() {
		return String.format("Point [%f, %f]", x, y);
	}
}