package ru.bulldog.justmap.util.colors;

import java.io.File;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import com.google.common.collect.Maps;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.minecraft.block.BlockState;
import net.minecraft.state.property.Property;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.Biome;
import org.jetbrains.annotations.Nullable;

import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.JsonFactory;

public class ColorPalette {

	private static final Function<Entry<Property<?>, Comparable<?>>, String> PROPERTY_PRINTER = new Function<Entry<Property<?>, Comparable<?>>, String>() {
		public String apply(@Nullable Entry<Property<?>, Comparable<?>> entry) {
			if (entry == null) {
				return "";
			} else {
				Property<?> property = entry.getKey();
				return property.getName() + "=" + this.nameValue(property, entry.getValue());
			}
		}

		@SuppressWarnings("unchecked")
		private <T extends Comparable<T>> String nameValue(Property<T> property, Comparable<?> value) {
			return property.name((T) value);
		}
	};

	private final BiMap<Set<String>, Integer> blockColors = HashBiMap.create();
	private final BiMap<Set<String>, Integer> fluidColors = HashBiMap.create();
	private final Map<Identifier, Integer> textureColors = Maps.newHashMap();
	private final Map<Identifier, BiomeColors> biomeColors = Maps.newHashMap();

	public int getBlockColor(BlockState block) {
		return getColor(blockColors, block);
	}

	public void addBlockColor(BlockState block, int color) {
		addColor(blockColors, block, color);
	}

	public int getFluidColor(BlockState block) {
		return getColor(fluidColors, block);
	}

	public void addFluidColor(BlockState block, int color) {
		addColor(fluidColors, block, color);
	}

	public int getTextureColor(Identifier texture) {
		if (this.textureColors.containsKey(texture)) {
			return this.textureColors.get(texture);
		}
		return 0x0;
	}

	public void addTextureColor(Identifier texture, int color) {
		synchronized (textureColors) {
			if (this.textureColors.containsKey(texture)) {
				this.textureColors.replace(texture, color);
			} else {
				this.textureColors.put(texture, color);
			}
		}
	}

	public BiomeColors getBiomeColors(Identifier id, Biome biome) {
		if (biomeColors.containsKey(id)) {
			return this.biomeColors.get(id);
		}
		synchronized (biomeColors) {
			BiomeColors newColors = new BiomeColors(biome);
			this.biomeColors.put(id, newColors);

			return newColors;
		}
	}

	public int getFoliageColor(Identifier id, Biome biome) {
		return this.getBiomeColors(id, biome).getFoliageColor();
	}

	public int getGrassColor(Identifier id, Biome biome, int x, int z) {
		return this.getBiomeColors(id, biome).getGrassColor(x, z);
	}

	public int getWaterColor(Identifier id, Biome biome) {
		return this.getBiomeColors(id, biome).getWaterColor();
	}

	public void saveData(File folder) {
		JsonArray blocks = new JsonArray();
		this.blockColors.forEach((keys, value) -> {
			JsonObject block = new JsonObject();
			JsonArray keysArray = new JsonArray();
			keys.forEach(key -> keysArray.add(key));
			block.add("blocks", keysArray);
			block.addProperty("color", Integer.toHexString(value));
			blocks.add(block);
		});
		JsonFactory.storeJson(new File(folder, "blockcolors.json"), blocks);

		JsonArray fluids = new JsonArray();
		this.fluidColors.forEach((keys, value) -> {
			JsonObject fluid = new JsonObject();
			JsonArray keysArray = new JsonArray();
			keys.forEach(key -> keysArray.add(key));
			fluid.add("fluids", keysArray);
			fluid.addProperty("color", Integer.toHexString(value));
			fluids.add(fluid);
		});
		JsonFactory.storeJson(new File(folder, "fluidcolors.json"), fluids);

		JsonObject textures = new JsonObject();
		this.textureColors.forEach((id, color) -> {
			textures.addProperty(id.toString(), Integer.toHexString(color));
		});
		JsonFactory.storeJson(new File(folder, "texturecolors.json"), textures);

		JsonObject biomes = new JsonObject();
		this.biomeColors.forEach((id, biome) -> {
			biomes.add(id.toString(), biome.toJson());
		});
		JsonFactory.storeJson(new File(folder, "biomecolors.json"), biomes);
	}

	public void loadData(File folder) {
		if (!folder.exists()) return;
		for (File dataFile : folder.listFiles()) {
			if (dataFile.isDirectory()) continue;
			switch(dataFile.getName()) {
				case "blockcolors.json": {
					JsonArray blocks = JsonFactory.loadJson(dataFile).getAsJsonArray();
					if (blocks.size() == 0) continue;
					blocks.forEach(obj -> {
						JsonObject entry = obj.getAsJsonObject();
						if (!entry.has("blocks") || !entry.has("color")) return;
						JsonArray keysArray = entry.get("blocks").getAsJsonArray();
						if (keysArray.size() == 0) return;
						Set<String> keySet = new HashSet<>();
						keysArray.forEach(key -> {
							keySet.add(key.getAsString());
						});
						String hexColor = entry.get("color").getAsString();
						int color = ColorUtil.parseHex(hexColor);
						this.blockColors.put(keySet, color);
					});
					continue;
				}
				case "fluidcolors.json": {
					JsonArray fluids = JsonFactory.loadJson(dataFile).getAsJsonArray();
					if (fluids.size() == 0) continue;
					fluids.forEach(obj -> {
						JsonObject entry = obj.getAsJsonObject();
						if (!entry.has("fluids") || !entry.has("color")) return;
						JsonArray keysArray = entry.get("fluids").getAsJsonArray();
						if (keysArray.size() == 0) return;
						Set<String> keySet = new HashSet<>();
						keysArray.forEach(key -> {
							keySet.add(key.getAsString());
						});
						String hexColor = entry.get("color").getAsString();
						int color = ColorUtil.parseHex(hexColor);
						this.fluidColors.put(keySet, color);
					});
					continue;
				}
				case "texturecolors.json": {
					JsonObject textures = JsonFactory.getJsonObject(dataFile);
					textures.entrySet().forEach(entry -> {
						String key = entry.getKey();
						String hexColor = entry.getValue().getAsString();
						int color = ColorUtil.parseHex(hexColor);
						this.textureColors.put(new Identifier(key), color);
					});
					continue;
				}
				case "biomecolors.json": {
					JsonObject biomes = JsonFactory.getJsonObject(dataFile);
					biomes.entrySet().forEach(entry -> {
						String key = entry.getKey();
						JsonObject biomeJson = entry.getValue().getAsJsonObject();
						Identifier biomeId = new Identifier(key);
						Biome biome = DataUtil.getBiomeRegistry().get(biomeId);
						this.biomeColors.put(biomeId, BiomeColors.fromJson(biome, biomeJson));
					});
				}
			}
		}
	}

	private static int getColor(BiMap<Set<String>, Integer> map, BlockState block) {
		String stateKey = makeKey(block);
		Set<String> key = getKey(stateKey, map);
		if (map.containsKey(key)) {
			return map.get(key);
		}
		return 0x0;
	}

	private static void addColor(BiMap<Set<String>, Integer> map, BlockState block, int color) {
		String blockKey = makeKey(block);
		synchronized (map) {
			Set<String> key = getKey(blockKey, map);
			if (map.containsValue(color)) {
				Set<String> hasKey = map.inverse().get(color);
				if (hasKey.equals(key)) return;
				if (map.containsKey(key)) {
					key.remove(blockKey);
				}
				hasKey.add(blockKey);
				return;
			}
			map.put(key, color);
		}
	}

	private static Set<String> getKey(String key, Map<Set<String>, Integer> map) {
		synchronized (map) {
			for (Set<String> entry : map.keySet()) {
				if (entry.contains(key)) {
					return entry;
				}
			}
		}
		Set<String> keySet = new HashSet<>();
		keySet.add(key);

		return keySet;
	}

	private static String makeKey(BlockState block) {
		StringBuilder stringBuilder = new StringBuilder();
		Identifier stateId = Registry.BLOCK.getId(block.getBlock());
		stringBuilder.append(stateId);

		Map<Property<?>, Comparable<?>> properties = block.getEntries();
		if (!properties.isEmpty()) {
			stringBuilder.append('[')
						 .append(properties.entrySet().stream().map(PROPERTY_PRINTER)
								 		   .collect(Collectors.joining(",")))
						 .append(']');
		}
		return stringBuilder.toString();
	}
}
