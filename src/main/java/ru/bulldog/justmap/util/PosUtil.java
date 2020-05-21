package ru.bulldog.justmap.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class PosUtil {
	private static MinecraftClient minecraft = MinecraftClient.getInstance();

	public static int coordX() {
		return (int) (minecraft.getCameraEntity().getX() < 0.0 ? minecraft.getCameraEntity().getX() - 1.0 : minecraft.getCameraEntity().getX());
	}

	public static int coordZ() {
		return (int) (minecraft.getCameraEntity().getZ() < 0.0 ? minecraft.getCameraEntity().getZ() - 1.0 : minecraft.getCameraEntity().getZ());
	}

	public static int coordY() {
		return (int) Math.ceil(minecraft.getCameraEntity().getY());
	}

	public static BlockPos currentPos() {
		return new BlockPos(coordX(), coordY(), coordZ());
	}
	
	public static double doubleCoordX() {
		return minecraft.getCameraEntity().prevX + (minecraft.getCameraEntity().getX() - minecraft.getCameraEntity().prevX) * (double) minecraft.getTickDelta();
	}

	public static double doubleCoordZ() {
		return minecraft.getCameraEntity().prevZ + (minecraft.getCameraEntity().getZ() - minecraft.getCameraEntity().prevZ) * (double) minecraft.getTickDelta();
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
