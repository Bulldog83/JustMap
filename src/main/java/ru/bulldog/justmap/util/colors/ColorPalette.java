package ru.bulldog.justmap.util.colors;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.world.biome.Biome;

public final class ColorPalette {
	public static final int WHITE = 0xFFFFFFFF;
	public static final int BLACK = 0xFF000000;
	public static final int RED = 0xFFFF0000;
	public static final int GREEN = 0xFF00FF00;
	public static final int BLUE = 0xFF0000FF;
	public static final int DARK_RED = 0xFFAA0000;
	public static final int GOLD = 0xFFFFAA00;
	public static final int YELLOW = 0xFFFFFF00;
	public static final int ORANGE = 0xFFFFA500;
	public static final int DARK_GREEN = 0xFF00AA00;
	public static final int CYAN = 0xFF00FFFF;
	public static final int DARK_AQUA = 0xFF00AAAA;
	public static final int DARK_BLUE = 0xFF000099;
	public static final int PINK = 0xFFFFC0CB;
	public static final int MAGENTA = 0xFFFF00FF;
	public static final int PURPLE = 0xFF6A0DAD;
	public static final int LIGHT_GRAY = 0xFFBBBBBB;
	public static final int GRAY = 0xFF676676;
	
	public static final int GRID = 0xFF858585;
	public static final int LOADED_OVERLAY = 0xFFA264E2;
	public static final int SLIME_OVERLAY = 0xFF00FF00;
	public static final int LIGHT = 0xFFF000F0;
	public static final int SPRUCE_LEAVES = 0xFF619961;
	public static final int BIRCH_LEAVES = 0xFF80A755;
	public static final int LILY_PAD = 0xFF208030;
	public static final int ATTACHED_STEM = 0xFFE0C71C;
	public static final int TRANSPARENT = 0x0;
	
	private final static ColorPalette INSTANCE = new ColorPalette();
	
	public static ColorPalette getInstance() {
		return INSTANCE;
	}
	
	private final Map<String, Integer> blockColors = Maps.newHashMap();
	private final Map<String, Integer> fluidColors = Maps.newHashMap();
	private final Map<String, BiomeColors> biomeColors = Maps.newHashMap();
	
	private ColorPalette() {}
	
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
	
	public BiomeColors getBiomeColors(Biome biome) {
		String biomeStr = biome.toString();
		if (biomeColors.containsKey(biomeStr)) {
			return this.biomeColors.get(biomeStr);
		}
		
		BiomeColors newColors = new BiomeColors();
		this.biomeColors.put(biomeStr, newColors);
		
		return newColors;
	}
	
	public int getFoliageColor(Biome biome) {
		return this.getBiomeColors(biome).foliageColor;
	}
	
	public void addFoliageColor(Biome biome, int color) {
		this.getBiomeColors(biome).foliageColor = color;
	}
	
	public int getGrassColor(Biome biome, int x, int z) {
		return biome.getGrassColorAt(x, z);
	}
	
	public int getWaterColor(Biome biome) {
		return this.getBiomeColors(biome).waterColor;
	}
	
	public void addWaterColor(Biome biome, int color) {
		this.getBiomeColors(biome).waterColor = color;
	}
	
	public static class BiomeColors {
		public int foliageColor = 0x0;
		public int waterColor = 0x0;
	}
}
