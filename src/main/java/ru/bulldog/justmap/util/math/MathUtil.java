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
	
	public static double screenPos(double val, double x1, double x2, double mapWidth) {
		return ((val - x1) / (x2 - x1)) * mapWidth;
	}
	
	public static int worldPos(double val, double x1, double x2, double range) {
		return (int) ((val * (x2 - x1) + range * x1) / range);
	}

	public static Point circlePos(Point pos, Point center, double angle) {
		int posX = (int) (center.x + (pos.x - center.x) * Math.cos(angle) - (pos.y - center.y) * Math.sin(angle));
		int posY = (int) (center.y + (pos.y - center.y) * Math.cos(angle) + (pos.x - center.x) * Math.sin(angle));
		
		return new Point(posX, posY);
	}
	
	public static Point getCross(Line line1, Line line2) {
		double x1 = line1.first.x;
		double x2 = line1.second.x;
		double x3 = line2.first.x;
		double x4 = line2.second.x;
		double y1, y2, y3, y4;
		if (x1 >= x2) {
			x1 = line1.second.x;
			y1 = line1.second.y;
			x2 = line1.first.x;
			y2 = line1.first.y;
		} else {
			y1 = line1.first.y;
			y2 = line1.second.y;
		}
		if (x3 >= x4) {
			x3 = line2.second.x;
			y3 = line2.second.y;
			x4 = line2.first.x;
			y4 = line2.first.y;
		} else {
			y3 = line2.first.y;
			y4 = line2.second.y;
		}
		if (x1 <= x4 && x4 <= x2 || x1 <= x3 && x3 <= x2) {
			float k1, k2;
			if (x1 == x2 || y1 == y2) {
				k1 = 0;
			} else {
				k1 = (float) ((y2 - y1) / (x2 - x1));
			}
			if (x3 == x4 || y3 == y4) {
				k2 = 0;
			} else {
				k2 = (float) ((y4 - y3) / (x4 - x3));
			}
			if (k1 == k2) return null;
			
			double b1 = y1 - k1 * x1;
			double b2 = y3 - k2 * x3;
			double x = (b2 - b1) / (k1 - k2);
			double y = k1 * x + b1;
			
			return new Point(x, y);
		}
		
		return null;
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
