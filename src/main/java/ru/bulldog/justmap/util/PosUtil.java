package ru.bulldog.justmap.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import ru.bulldog.justmap.client.JustMapClient;

public class PosUtil {
	private static MinecraftClient minecraft = JustMapClient.MINECRAFT;
	private static BlockPos.Mutable currentPos = new BlockPos.Mutable();

	public static int coordX() {
		if (minecraft.getCameraEntity() == null) return 0;
		return (int) Math.ceil(minecraft.getCameraEntity().getX() < 0.0 ? minecraft.getCameraEntity().getX() - 1.0 : minecraft.getCameraEntity().getX());
	}

	public static int coordZ() {
		if (minecraft.getCameraEntity() == null) return 0;
		return (int) Math.ceil(minecraft.getCameraEntity().getZ() < 0.0 ? minecraft.getCameraEntity().getZ() - 1.0 : minecraft.getCameraEntity().getZ());
	}

	public static int coordY() {
		if (minecraft.getCameraEntity() == null) return 0;
		return (int) Math.ceil(minecraft.getCameraEntity().getY());
	}

	public static BlockPos currentPos() {
		return currentPos.set(coordX(), coordY(), coordZ());
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
