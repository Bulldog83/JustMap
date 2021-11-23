package ru.bulldog.justmap.util.colors;

import java.io.File;
import java.util.Map;

import com.google.common.collect.Maps;
import net.minecraft.block.BlockState;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.storage.StorageUtil;

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
	public final static int GRASS = BiomeColors.defaultGrassColor();
	public final static int FOLIAGE = BiomeColors.defaultFoliageColor();

	public final static Colors INSTANCE = new Colors();

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

	public int getTextureColor(BlockState block, Identifier texture) {
		Identifier stateId = Registry.BLOCK.getId(block.getBlock());
		return this.getPalette(stateId.getNamespace()).getTextureColor(texture);
	}

	public void addTextureColor(BlockState block, Identifier texture, int color) {
		Identifier stateId = Registry.BLOCK.getId(block.getBlock());
		this.getPalette(stateId.getNamespace()).addTextureColor(texture, color);
	}

	public int getFoliageColor(World world, Biome biome) {
		Identifier biomeId = DataUtil.getBiomeId(world, biome);
		return this.getPalette(biomeId.getNamespace()).getFoliageColor(biomeId, biome);
	}

	public int getGrassColor(World world, Biome biome, int x, int z) {
		Identifier biomeId = DataUtil.getBiomeId(world, biome);
		return this.getPalette(biomeId.getNamespace()).getGrassColor(biomeId, biome, x, z);
	}

	public int getWaterColor(World world, Biome biome) {
		Identifier biomeId = DataUtil.getBiomeId(world, biome);
		return this.getPalette(biomeId.getNamespace()).getWaterColor(biomeId, biome);
	}



	public void saveData() {
		File dir = new File(StorageUtil.mapDir(), "palettes");
		this.palettes.forEach((mod, palette) -> {
			File saveDir = new File(dir, mod);
			if (!saveDir.exists()) saveDir.mkdirs();
			palette.saveData(saveDir);
		});
	}

	public void loadData() {
		File dir = new File(StorageUtil.mapDir(), "palettes");
		if (!dir.exists()) return;
		for (File paletteDir : dir.listFiles()) {
			if (paletteDir.isFile()) continue;
			String name = paletteDir.getName();
			try {
				if (palettes.containsKey(name)) {
					this.palettes.get(name).loadData(paletteDir);
				} else {
					ColorPalette palette = new ColorPalette();
					palette.loadData(paletteDir);
					this.palettes.put(name, palette);
				}
			} catch (Exception ex) {
				JustMap.LOGGER.warning("Error while loading palette!", name, ex);
			}
		}
	}
}
