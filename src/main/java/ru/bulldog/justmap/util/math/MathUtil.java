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
			val--;
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
	
	public static double correctAngle(double d) {
		int turns = (int) Math.abs(d / 360);		
		if(d >= 360) return d - 360 * turns;
		if(d < 0) return d + 360 * (turns + 1);
		
		return d;
	}
	
	public static double screenPos(double worldPos, double anchorWorld, double anchorScreen, double scale) {
		return anchorScreen + (worldPos - anchorWorld) / scale;
	}
	
	public static double worldPos(double screenPos, double anchorWorld, double anchorScreen, double scale) {
		return anchorWorld + (screenPos - anchorScreen) * scale;
	}
	
	public static int screenPos(int worldPos, int anchorWorld, int anchorScreen, double scale) {
		return (int) (anchorScreen + (worldPos - anchorWorld) / scale);
	}
	
	public static int worldPos(int screenPos, int anchorWorld, int anchorScreen, double scale) {
		return (int) (anchorWorld + (screenPos - anchorScreen) * scale);
	}

	public static Point circlePos(Point pos, Point center, double angle) {
		double posX = center.x + (pos.x - center.x) * Math.cos(angle) - (pos.y - center.y) * Math.sin(angle);
		double posY = center.y + (pos.y - center.y) * Math.cos(angle) + (pos.x - center.x) * Math.sin(angle);
		
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
