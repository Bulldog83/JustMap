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
		return Math.pow(n, 2);
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
		return (int) Math.round((val * (x2 - x1) + range * x1) / range);
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
	
	public static String posToString(BlockPos pos) {
		return posToString(pos.getX(), pos.getY(), pos.getZ());
	}
	
	public static String posToString(double x, double y, double z) {
		int posX = (int) Math.round(x);
		int posY = (int) Math.round(y);
		int posZ = (int) Math.round(z);
		
		return String.format("%d, %d, %d", posX, posY, posZ);
	}
}
