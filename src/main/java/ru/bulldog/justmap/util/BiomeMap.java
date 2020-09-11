package ru.bulldog.justmap.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeKeys;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.storage.StorageUtil;

public class BiomeMap {
	private final static String SEPARATOR = ";";
	private static final int HORIZONTAL_COUNT;
	private static final int VERTICAL_COUNT;
	private static final int DEFAULT_LENGTH;
	private static final int HORIZONTAL_BIT;
	private static final int VERTICAL_BIT;
	private static Map<String, String[]> biomeMap = Maps.newHashMap();
	private static Identifier plainsId = BiomeKeys.PLAINS.getValue();
	private static File cacheDir;
	
	public static void addBiome(World world, BlockPos pos, Biome biome) {
		int index = makeIndex(pos);
		String posStr = makeKey(pos);
		Identifier biomeId = world.getRegistryManager().get(Registry.BIOME_KEY).getId(biome);
		if (biomeMap.containsKey(posStr)) {
			Identifier hasId = new Identifier(biomeMap.get(posStr)[index]);
			if (!hasId.equals(biomeId) && !biomeId.equals(plainsId)) {
				addValue(posStr, index, biome);
			}
		}
		addValue(posStr, index, biome);
	}
	
	public static Biome getBiome(World world, BlockPos pos) {
		String posStr = makeKey(pos);
		if (biomeMap.containsKey(posStr)) {
			String value = getValue(pos);
			if (value != null) {
				Identifier biomeId = new Identifier(value);
				return world.getRegistryManager().get(Registry.BIOME_KEY).get(biomeId);
			}
		}
		Biome biome = world.getBiome(pos);
		int index = makeIndex(pos);
		addValue(posStr, index, biome);
		
		return biome;
	}
	
	public static void onJoinWorld() {
		if (cacheDir != null) saveData();
		cacheDir = StorageUtil.cacheDir();
		loadData();
	}
	
	public static void saveData() {
		String data = serialize();
		if (data == null || data.equals("")) return;
		File dataFile = new File(cacheDir, "biomemap.dat");
		try (OutputStream fileOut = new FileOutputStream(dataFile)) {
			fileOut.write(data.getBytes());
		} catch (IOException ex) {
			JustMap.LOGGER.warning("Can't save data!", dataFile, ex);
		}
	}
	
	public static void loadData() {
		biomeMap.clear();
		File dataFile = new File(cacheDir, "biomemap.dat");
		if (!dataFile.exists()) return;
		try (InputStream fileIn = new FileInputStream(dataFile)) {
			byte[] data = new byte[fileIn.available()];
			fileIn.read(data);
			deserialize(new String(data));
		} catch (IOException ex) {
			JustMap.LOGGER.warning("Can't load data!", dataFile, ex);
		}
	}
	
	private static String serialize() {
		if (biomeMap.size() == 0) return "";
		StringBuilder builder = new StringBuilder();
		biomeMap.entrySet().forEach(entry -> {
			builder.append(entry)
				   .append(SEPARATOR);
		});
		String data = new String(builder);
		return CompressionUtil.compress(data.getBytes());
	}
	
	private static void deserialize(String data) {
		String dataStr = CompressionUtil.decompress(data);
		if (dataStr == null || !dataStr.contains(SEPARATOR)) return;
		String[] dataArray = dataStr.split(SEPARATOR);
		for (String entry : dataArray) {
			String[] entryData = entry.split("=");
			if (entryData.length < 2 || entryData.length > 2) return;
			//biomeMap.put(entryData[0], entryData[1]);
		}
	}
	
	private static String getValue(BlockPos pos) {
		String key = makeKey(pos);
		if (biomeMap.containsKey(key)) {
			int index = makeIndex(pos);
			return biomeMap.get(key)[index];
		}
		return null;
	}
	
	private static void addValue(String key, int index, Biome biome) {
		
	}
	
	private static String makeKey(BlockPos pos) {
		int x = pos.getX() >> 4;
		int z = pos.getZ() >> 4;
		return String.format("%s.%s", x, z);
	}
	
	private static int makeIndex(BlockPos pos) {
		int x = (pos.getX() >> 2) & HORIZONTAL_BIT;
		int y = MathHelper.clamp(pos.getY() >> 2, 0, VERTICAL_BIT);
		int z = (pos.getZ() >> 2) & HORIZONTAL_BIT;
		return y << HORIZONTAL_COUNT + HORIZONTAL_COUNT | z << HORIZONTAL_COUNT | x;
	}
	
	static {
		HORIZONTAL_COUNT = (int) Math.round(Math.log(16.0D) / Math.log(2.0D)) - 2;
		VERTICAL_COUNT = (int) Math.round(Math.log(256.0D) / Math.log(2.0D)) - 2;
		DEFAULT_LENGTH = 1 << HORIZONTAL_COUNT + HORIZONTAL_COUNT + VERTICAL_COUNT;
		HORIZONTAL_BIT = (1 << HORIZONTAL_COUNT) - 1;
		VERTICAL_BIT = (1 << VERTICAL_COUNT) - 1;
	}
}
