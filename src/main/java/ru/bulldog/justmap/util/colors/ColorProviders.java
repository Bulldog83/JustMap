package ru.bulldog.justmap.util.colors;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.client.color.world.FoliageColors;
import net.minecraft.client.color.world.GrassColors;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import ru.bulldog.justmap.util.BiomeMap;

public class ColorProviders implements ColorProvider {
	
	private final ColorPalette colorPalette = ColorPalette.getInstance();
	private final Map<Block, ColorProvider> providers = Maps.newHashMap();

	public static ColorProviders registerProviders() {
		ColorProviders blockColors = new ColorProviders();
		blockColors.registerColorProvider((state, world, pos) -> {
			return blockColors.getGrassColor(world, pos);
		}, Blocks.LARGE_FERN, Blocks.TALL_GRASS, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
		blockColors.registerColorProvider((state, world, pos) -> {
			return FoliageColors.getSpruceColor();
		}, Blocks.SPRUCE_LEAVES);
		blockColors.registerColorProvider((state, world, pos) -> {
			return FoliageColors.getBirchColor();
		}, Blocks.BIRCH_LEAVES);
		blockColors.registerColorProvider((state, world, pos) -> {
			return blockColors.getFoliageColor(world, pos);
		}, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE);
		blockColors.registerColorProvider((state, world, pos) -> {
			return blockColors.getWaterColor(world, pos);
		}, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.CAULDRON);
		blockColors.registerColorProvider((state, world, pos) -> {
			return RedstoneWireBlock.getWireColor(state.get(RedstoneWireBlock.POWER));
		}, Blocks.REDSTONE_WIRE);
		blockColors.registerColorProvider((state, world, pos) -> {
			return blockColors.getGrassColor(world, pos);
		}, Blocks.SUGAR_CANE);
		blockColors.registerColorProvider((state, world, pos) -> {
			return 14731036;
		}, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
		blockColors.registerColorProvider((state, world, pos) -> {
			int i = state.get(StemBlock.AGE);
			int j = i * 32;
			int k = 255 - i * 8;
			int l = i * 4;
			return j << 16 | k << 8 | l;
		}, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
		blockColors.registerColorProvider((state, world, pos) -> {
			return world != null && pos != null ? 2129968 : 7455580;
		}, Blocks.LILY_PAD);
		
		return blockColors;
	}
	
	private void registerColorProvider(ColorProvider provider, Block... blocks) {
		for(int i = 0; i < blocks.length; i++) {
			Block block = blocks[i];
			this.providers.put(block, provider);
		}
	}
	
	public int getGrassColor(World world, BlockPos pos) {
		if (world != null && pos != null) {
			Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES, false);
			if (chunk != null && chunk.getBiomeArray() != null) {
				Biome biome = BiomeMap.getBiome(world, pos);
				return this.colorPalette.getGrassColor(biome, pos.getX(), pos.getZ());
			}
		}
		return GrassColors.getColor(0.5, 1.0);
	}
	
	public int getFoliageColor(World world, BlockPos pos) {
		if (world != null && pos != null) {
			Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES, false);
			if (chunk != null && chunk.getBiomeArray() != null) {
				Biome biome = BiomeMap.getBiome(world, pos);
				int color = this.colorPalette.getFoliageColor(biome);
				if (color == 0x0) {
					color = biome.getFoliageColor();
					this.colorPalette.addFoliageColor(biome, color);
				}
				return color;
			}
		}
		return FoliageColors.getDefaultColor();
	}
	
	public int getWaterColor(World world, BlockPos pos) {
		if (world != null && pos != null) {
			Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES, false);
			if (chunk != null && chunk.getBiomeArray() != null) {
				Biome biome = BiomeMap.getBiome(world, pos);
				int color = this.colorPalette.getWaterColor(biome);
				if (color == 0x0) {
					color = biome.getWaterColor();
					this.colorPalette.addWaterColor(biome, color);
				}
				return color;
			}
		}
		return -1;
	}

	@Override
	public int getColor(BlockState state, World world, BlockPos pos) {
		ColorProvider provider = this.providers.get(state.getBlock());
		if (provider != null) {
			return provider.getColor(state, world, pos);
		}
		return -1;
	}
}
