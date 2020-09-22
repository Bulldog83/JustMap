package ru.bulldog.justmap.util.colors;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

import com.google.gson.JsonObject;

import net.minecraft.util.JsonHelper;
import net.minecraft.world.biome.Biome;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.storage.ResourceLoader;

public class BiomeColors {
	private static int[] foliageMap;
	private static int[] grassMap;
	
	private Biome biome;
	private int foliageColor;
	private int waterColor;
	
	private BiomeColors() {}
	
	public BiomeColors(Biome biome) {
		this.biome = biome;
		this.foliageColor = biome.getFoliageColor();
		this.waterColor = biome.getWaterColor();
	}
	
	public int getWaterColor() {
		return this.waterColor;
	}
	
	public int getFoliageColor() {
		return this.foliageColor;
	}
	
	public int getGrassColor(int x, int z) {
		return biome.getGrassColorAt(x, z);
	}
	
	public static int getGrassColor(double temperature, double humidity) {
		humidity *= temperature;
		int t = (int) ((1.0D - temperature) * 255.0D);
		int h = (int) ((1.0D - humidity) * 255.0D);
		int k = h << 8 | t;
		if (k < 0 || k > grassMap.length) return Colors.GRASS;
		return grassMap[k];
	}
	
	public static int defaultGrassColor() {
		return getGrassColor(0.5, 1.0);
	}
	
	public static int getFoliageColor(double temperature, double humidity) {
		humidity *= temperature;
		int t = (int) ((1.0D - temperature) * 255.0D);
		int h = (int) ((1.0D - humidity) * 255.0D);
		int k = h << 8 | t;
		if (k < 0 || k > foliageMap.length) return Colors.FOLIAGE;
		return foliageMap[k];
	}
	
	public static int defaultFoliageColor() {
		return getFoliageColor(0.5, 1.0);
	}
	
	public JsonObject toJson() {
		JsonObject json = new JsonObject();
		json.addProperty("foliage", Integer.toHexString(foliageColor));
		json.addProperty("water", Integer.toHexString(waterColor));
		
		return json;
	}
	
	public static BiomeColors fromJson(Biome biome, JsonObject json) {
		BiomeColors biomeColors = new BiomeColors();
		biomeColors.biome = biome;
		if (json.has("foliage")) {
			String hexColor = JsonHelper.getString(json, "foliage");
			biomeColors.foliageColor = ColorUtil.parseHex(hexColor);
		} else {
			biomeColors.foliageColor = biome.getFoliageColor();
		}
		if (json.has("water")) {
			String hexColor = JsonHelper.getString(json, "water");
			biomeColors.waterColor = ColorUtil.parseHex(hexColor);
		} else {
			biomeColors.waterColor = biome.getWaterColor();
		}
		
		return biomeColors;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("[")
			   .append("foliage=" + foliageColor)
			   .append(",")
			   .append("water=" + waterColor)
			   .append("]");
		
		return builder.toString();
	}
	
	static {
		ResourceLoader foliageColors = new ResourceLoader("textures/colormap/foliage.png");
		try (InputStream ins = foliageColors.getInputStream()) {
			BufferedImage image = ImageIO.read(ins);
			int width = image.getWidth();
			int height = image.getHeight();
			foliageMap = new int[width * height];
			image.getRGB(0, 0, width, height, foliageMap, 0, width);
		} catch (IOException ex) {
			JustMap.LOGGER.error("Can't load foliage colors texture!");
		}
		ResourceLoader grassColors = new ResourceLoader("textures/colormap/grass.png");
		try (InputStream ins = grassColors.getInputStream()) {
			BufferedImage image = ImageIO.read(ins);
			int width = image.getWidth();
			int height = image.getHeight();
			grassMap = new int[width * height];
			image.getRGB(0, 0, width, height, grassMap, 0, width);
		} catch (IOException ex) {
			JustMap.LOGGER.error("Can't load grass colors texture!");
		}
	}
}