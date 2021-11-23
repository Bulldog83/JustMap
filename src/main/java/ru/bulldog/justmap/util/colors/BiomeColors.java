package ru.bulldog.justmap.util.colors;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.google.gson.JsonObject;
import javax.imageio.ImageIO;
import net.minecraft.util.JsonHelper;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.biome.BiomeEffects;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.mixins.BiomeColorsAccessor;
import ru.bulldog.justmap.util.storage.ResourceLoader;

public class BiomeColors {
	private static int[] foliageMap;
	private static int[] grassMap;

	private Biome biome;
	private Optional<Integer> foliageColor;
	private Optional<Integer> grassColor;
	private int waterColor;

	private BiomeColors() {}

	public BiomeColors(Biome biome) {
		this.biome = biome;
		BiomeColorsAccessor accessor = (BiomeColorsAccessor) biome.getEffects();
		this.foliageColor = accessor.getFoliageColor();
		this.grassColor = accessor.getGrassColor();
		this.waterColor = accessor.getWaterColor();
	}

	public int getWaterColor() {
		return this.waterColor;
	}

	public int getFoliageColor() {
		float temperature = this.biome.getTemperature();
		float humidity = this.biome.getDownfall();
		return this.foliageColor.orElse(getFoliageColor(temperature, humidity));
	}

	public int getGrassColor(int x, int z) {
		float temperature = this.biome.getTemperature();
		float humidity = this.biome.getDownfall();
		int color = this.grassColor.orElse(getGrassColor(temperature, humidity));
		BiomeColorsAccessor accessor = (BiomeColorsAccessor) biome.getEffects();
		BiomeEffects.GrassColorModifier modifier = accessor.getGrassColorModifier();
		switch (modifier) {
			case DARK_FOREST: {
				return (color & 16711422) + 2634762 >> 1;
			}
			case SWAMP: {
				double noise = Biome.FOLIAGE_NOISE.sample(x * 0.0225D, z * 0.0225D, false);
				return noise < -0.1D ? 5011004 : 6975545;
			}
			default: {
				return color;
			}
		}
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
		if (foliageColor.isPresent()) {
			json.addProperty("foliage", Integer.toHexString(foliageColor.get()));
		}
		if (grassColor.isPresent()) {
			json.addProperty("grass", Integer.toHexString(grassColor.get()));
		}
		json.addProperty("water", Integer.toHexString(waterColor));

		return json;
	}

	public static BiomeColors fromJson(Biome biome, JsonObject json) {
		BiomeColors biomeColors = new BiomeColors();
		BiomeColorsAccessor accessor = (BiomeColorsAccessor) biome.getEffects();
		biomeColors.biome = biome;
		if (json.has("foliage")) {
			String hexColor = JsonHelper.getString(json, "foliage");
			biomeColors.foliageColor = Optional.of(ColorUtil.parseHex(hexColor));
		} else {
			biomeColors.foliageColor = accessor.getFoliageColor();
		}
		if (json.has("grass")) {
			String hexColor = JsonHelper.getString(json, "grass");
			biomeColors.grassColor = Optional.of(ColorUtil.parseHex(hexColor));
		} else {
			biomeColors.grassColor = accessor.getGrassColor();
		}
		if (json.has("water")) {
			String hexColor = JsonHelper.getString(json, "water");
			biomeColors.waterColor = ColorUtil.parseHex(hexColor);
		} else {
			biomeColors.waterColor = accessor.getWaterColor();
		}

		return biomeColors;
	}

	public String toString() {
		return "[" + "foliage=" + foliageColor +
				"," + "grass=" + grassColor +
				"," + "water=" + waterColor +
				"]";
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
