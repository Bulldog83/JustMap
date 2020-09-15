package ru.bulldog.justmap.util.colors;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.world.biome.Biome;

public class ColorPalette {
	private final Map<String, Integer> blockColors = Maps.newHashMap();
	private final Map<String, Integer> fluidColors = Maps.newHashMap();
	private final Map<Identifier, BiomeColors> biomeColors = Maps.newHashMap();
	
	public int getBlockColor(BlockState block) {
		String state = block.toString();
		if (blockColors.containsKey(state)) {
			return this.blockColors.get(state);
		}
		return 0x0;
	}
	
	public void addBlockColor(BlockState block, int color) {
		this.blockColors.put(block.toString(), color);
	}
	
	public int getFluidColor(BlockState block) {
		String state = block.toString();
		if (fluidColors.containsKey(state)) {
			return this.fluidColors.get(state);
		}
		return 0x0;
	}
	
	public void addFluidColor(BlockState block, int color) {
		this.fluidColors.put(block.toString(), color);
	}
	
	public BiomeColors getBiomeColors(Identifier id, Biome biome) {
		if (biomeColors.containsKey(id)) {
			return this.biomeColors.get(id);
		}
		
		BiomeColors newColors = new BiomeColors(biome);
		this.biomeColors.put(id, newColors);
		
		return newColors;
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
}
