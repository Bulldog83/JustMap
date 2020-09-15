package ru.bulldog.justmap.util.colors;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import ru.bulldog.justmap.util.DataUtil;

public final class Colors {
	public static final int TRANSPARENT = 0x0;
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
	public static final int GRASS = BiomeColors.defaultGrassColor();
	public static final int FOLIAGE = BiomeColors.defaultFoliageColor();
	public static final int ATTACHED_STEM = 0xFFE0C71C;
	
	private final static Colors INSTANCE = new Colors();
	
	public static Colors getInstance() {
		return INSTANCE;
	}
	
	private final Map<String, ColorPalette> palettes = Maps.newHashMap();
	
	private Colors() {}
	
	private ColorPalette getPalette(String key) {
		if (palettes.containsKey(key)) {
			return this.palettes.get(key);
		}
		ColorPalette palette = new ColorPalette();
		this.palettes.put(key, palette);
		
		return palette;
	}
	
	public int getBlockColor(BlockState block) {
		Identifier stateId = Registry.BLOCK.getId(block.getBlock());
		return this.getPalette(stateId.getNamespace()).getBlockColor(block);
	}
	
	public void addBlockColor(BlockState block, int color) {
		Identifier stateId = Registry.BLOCK.getId(block.getBlock());
		this.getPalette(stateId.getNamespace()).addBlockColor(block, color);
	}
	
	public int getFluidColor(BlockState block) {
		Identifier stateId = Registry.BLOCK.getId(block.getBlock());
		return this.getPalette(stateId.getNamespace()).getFluidColor(block);
	}
	
	public void addFluidColor(BlockState block, int color) {
		Identifier stateId = Registry.BLOCK.getId(block.getBlock());
		this.getPalette(stateId.getNamespace()).addFluidColor(block, color);
	}
	
	public int getFoliageColor(World world, Biome biome) {
		Identifier biomeId = DataUtil.getBiomeRegistry(world).getId(biome);
		return this.getPalette(biomeId.getNamespace()).getFoliageColor(biomeId, biome);
	}
	
	public int getGrassColor(World world, Biome biome, int x, int z) {
		Identifier biomeId = DataUtil.getBiomeRegistry(world).getId(biome);
		return this.getPalette(biomeId.getNamespace()).getGrassColor(biomeId, biome, x, z);
	}
	
	public int getWaterColor(World world, Biome biome) {
		Identifier biomeId = DataUtil.getBiomeRegistry(world).getId(biome);
		return this.getPalette(biomeId.getNamespace()).getWaterColor(biomeId, biome);
	}
}
