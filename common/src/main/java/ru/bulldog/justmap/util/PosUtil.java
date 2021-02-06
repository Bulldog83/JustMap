package ru.bulldog.justmap.util;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.core.BlockPos;
import net.minecraft.util.GsonHelper;

public class PosUtil {
	public static String shortPosString(BlockPos pos) {
		return String.format("%d.%d.%d", pos.getX(), pos.getY(), pos.getZ());
	}

	public static String posToString(BlockPos pos) {
		return PosUtil.posToString(pos.getX(), pos.getY(), pos.getZ());
	}

	public static String posToString(double x, double y, double z) {
		return String.format("%d, %d, %d", (int) x, (int) y, (int) z);
	}

	public static JsonElement toJson(BlockPos blockPos) {
		JsonObject position = new JsonObject();
		position.addProperty("x", blockPos.getX());
		position.addProperty("y", blockPos.getY());
		position.addProperty("z", blockPos.getZ());
		
		return position;
	}

	public static BlockPos fromJson(JsonObject element) {
		int x = GsonHelper.getAsInt(element, "x", 0);
		int y = GsonHelper.getAsInt(element, "y", 0);
		int z = GsonHelper.getAsInt(element, "z", 0);
		
		return new BlockPos(x, y, z);
	}
}
