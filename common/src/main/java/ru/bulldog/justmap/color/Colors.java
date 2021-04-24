package ru.bulldog.justmap.color;

import com.google.common.collect.Maps;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.FoliageColor;
import net.minecraft.world.level.GrassColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.state.BlockState;
import old_files.justmap.JustMap;
import old_files.justmap.util.DataUtil;
import old_files.justmap.util.colors.ColorPalette;
import ru.bulldog.justmap.util.StorageUtil;

import java.io.File;
import java.util.Map;

public final class Colors {
	public final static int TRANSPARENT = 0x0;
	public final static int WHITE = 0xFFFFFFFF;
	public final static int BLACK = 0xFF000000;
	public final static int RED = 0xFFFF0000;
	public final static int GREEN = 0xFF00FF00;
	public final static int BLUE = 0xFF0000FF;
	public final static int DARK_RED = 0xFFAA0000;
	public final static int GOLD = 0xFFFFAA00;
	public final static int YELLOW = 0xFFFFFF00;
	public final static int ORANGE = 0xFFFFA500;
	public final static int DARK_GREEN = 0xFF00AA00;
	public final static int CYAN = 0xFF00FFFF;
	public final static int DARK_AQUA = 0xFF00AAAA;
	public final static int DARK_BLUE = 0xFF000099;
	public final static int PINK = 0xFFFFC0CB;
	public final static int MAGENTA = 0xFFFF00FF;
	public final static int PURPLE = 0xFF6A0DAD;
	public final static int LIGHT_GRAY = 0xFFBBBBBB;
	public final static int GRAY = 0xFF676676;

	public final static int GRID = 0xFF858585;
	public final static int LOADED_OVERLAY = 0xFFA264E2;
	public final static int SLIME_OVERLAY = 0xFF00FF00;
	public final static int LIGHT = 0xFFF000F0;
	public final static int SPRUCE_LEAVES = 0xFF619961;
	public final static int BIRCH_LEAVES = 0xFF80A755;
	public final static int LILY_PAD = 0xFF208030;
	public final static int ATTACHED_STEM = 0xFFE0C71C;
	public final static int GRASS = GrassColor.get(0.5, 1.0);
	public final static int FOLIAGE = FoliageColor.getDefaultColor();
	
	private static Colors instance;

	public static Colors getInstance() {
		if (instance == null) {
			instance = new Colors();
		}
		return instance;
	}
	
	private final Map<String, ColorPalette> palettes = Maps.newHashMap();
	
	private Colors() {}
	
	private ColorPalette getPalette(String key) {
		if (palettes.containsKey(key)) {
			return palettes.get(key);
		}
		ColorPalette palette = new ColorPalette();
		palettes.put(key, palette);
		
		return palette;
	}
	
	public int getBlockColor(BlockState block) {
		ResourceLocation stateId = Registry.BLOCK.getKey(block.getBlock());
		return getPalette(stateId.getNamespace()).getBlockColor(block);
	}
	
	public void addBlockColor(BlockState block, int color) {
		ResourceLocation stateId = Registry.BLOCK.getKey(block.getBlock());
		getPalette(stateId.getNamespace()).addBlockColor(block, color);
	}
	
	public int getFluidColor(BlockState block) {
		ResourceLocation stateId = Registry.BLOCK.getKey(block.getBlock());
		return getPalette(stateId.getNamespace()).getFluidColor(block);
	}
	
	public void addFluidColor(BlockState block, int color) {
		ResourceLocation stateId = Registry.BLOCK.getKey(block.getBlock());
		getPalette(stateId.getNamespace()).addFluidColor(block, color);
	}
	
	public int getTextureColor(BlockState block, ResourceLocation texture) {
		ResourceLocation stateId = Registry.BLOCK.getKey(block.getBlock());
		return getPalette(stateId.getNamespace()).getTextureColor(texture);
	}
	
	public void addTextureColor(BlockState block, ResourceLocation texture, int color) {
		ResourceLocation stateId = Registry.BLOCK.getKey(block.getBlock());
		getPalette(stateId.getNamespace()).addTextureColor(texture, color);
	}
	
	public int getFoliageColor(Level world, Biome biome) {
		ResourceLocation biomeId = DataUtil.getBiomeId(world, biome);
		return getPalette(biomeId.getNamespace()).getFoliageColor(biome);
	}
	
	public int getGrassColor(Level world, Biome biome, int x, int z) {
		ResourceLocation biomeId = DataUtil.getBiomeId(world, biome);
		return getPalette(biomeId.getNamespace()).getGrassColor(biome, x, z);
	}
	
	public int getWaterColor(Level world, Biome biome) {
		ResourceLocation biomeId = DataUtil.getBiomeId(world, biome);
		return getPalette(biomeId.getNamespace()).getWaterColor(biome);
	}
	
	
	
	public void saveData() {
		File dir = new File(StorageUtil.mapDir(), "palettes");
		palettes.forEach((mod, palette) -> {
			File saveDir = new File(dir, mod);
			if (!saveDir.exists()) saveDir.mkdirs();
			palette.saveData(saveDir);
		});
	}
	
	public void loadData() {
		File dir = new File(StorageUtil.mapDir(), "palettes");
		if (!dir.exists()) return;
		File[] files = dir.listFiles();
		if (files == null) return;
		for (File paletteDir : files) {
			if (paletteDir.isFile()) continue;
			String name = paletteDir.getName();
			try {
				if (palettes.containsKey(name)) {
					palettes.get(name).loadData(paletteDir);
				} else {
					ColorPalette palette = new ColorPalette();
					palette.loadData(paletteDir);
					palettes.put(name, palette);
				}
			} catch (Exception ex) {
				JustMap.LOGGER.warning("Error while loading palette!", name, ex);
			}
		}
	}
}
