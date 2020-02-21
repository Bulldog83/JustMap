package ru.bulldog.justmap.util;

import java.io.File;
import java.io.IOException;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import ru.bulldog.justmap.JustMap;

public class CacheUtil {
	
	public final static File CACHE_DIR = new File(JustMap.MAP_DIR, "chache/");
	
	public static void storeData(File file, CompoundTag data) {
		try {
			NbtIo.safeWrite(data, file);
		} catch (IOException ex) {
			JustMap.LOGGER.logError("Can't save file: " + file.getAbsolutePath());
			JustMap.LOGGER.catching(ex);
		}
	}
	
	public static CompoundTag loadData(File file) {
		try {
			return NbtIo.read(file);
		} catch (IOException ex) {
			JustMap.LOGGER.logError("Can't load file: " + file.getAbsolutePath());
			JustMap.LOGGER.catching(ex);
		}
		
		return null;
	}
}
