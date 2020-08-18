package ru.bulldog.justmap.util.math;

import net.minecraft.util.math.BlockPos;

public class MathUtil {
	public static int clamp(int val, int min, int max) {
		return val < min ? min : val > max ? max : val;
	}
	
	public static double clamp(double val, double min, double max) {
		return val < min ? min : val > max ? max : val;
	}
	
	public static float clamp(float val, float min, float max) {
		return val < min ? min : val > max ? max : val;
	}
	
	public static double logn(double n, double a) {
		return Math.log(a) / Math.log(n); 
	}
	
	public static double pow2(double n) {
		return n * n;
	}
	
	public static boolean isEven(int num) {
		return num % 2 == 0;
	}
	
	public static boolean isOdd(int num) {
		return !isEven(num);
	}
	
	public static int floor(double val) {
		if (val < 0.0) {
			--val;
		}
        return (int) val;
	}
	
	public static double min(double... args) {
		if (args.length == 0) return 0.0;
		double min = Double.POSITIVE_INFINITY;
		for(double arg : args) {
			min = Math.min(min, arg);
		}
		
		return min;
	}
	
	public static double max(double... args) {
		if (args.length == 0) return 0.0;
		double max = Double.NEGATIVE_INFINITY;
		for(double arg : args) {
			max = Math.max(max, arg);
		}
		
		return max;
	}
	
	public static float correctAngle(float angle) {
		int turns = (int) Math.abs(angle / 360);		
		if(angle >= 360) return angle - 360 * turns;
		if(angle < 0) return angle + 360 * (turns + 1);
		
		return angle;
	}
	
	public static double screenPos(double worldPos, double anchorWorld, double anchorScreen, float scale) {
		return anchorScreen + (worldPos - anchorWorld) / scale;
	}
	
	public static double worldPos(double screenPos, double anchorWorld, double anchorScreen, float scale) {
		return anchorWorld + (screenPos - anchorScreen) * scale;
	}

	public static Point circlePos(Point pos, Point center, double angle) {
		int posX = (int) (center.x + (pos.x - center.x) * Math.cos(angle) - (pos.y - center.y) * Math.sin(angle));
		int posY = (int) (center.y + (pos.y - center.y) * Math.cos(angle) + (pos.x - center.x) * Math.sin(angle));
		
		return new Point(posX, posY);
	}
	
	public static double getDistance(BlockPos a, BlockPos b) {
		return getDistance(a, b, false);
	}
	
	public static double getDistance(BlockPos a, BlockPos b, boolean horizontalOnly) {
		int dist;
		int distX = (a.getX() - b.getX());
		int distZ = (a.getZ() - b.getZ());
		
		dist = distX * distX + distZ * distZ;
		if (!horizontalOnly) {
			int distY = (a.getY() - b.getY());
			dist += distY * distY;
		}
		
		return Math.sqrt(dist);
	}
}
