package ru.bulldog.justmap.util.colors;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkStatus;

import ru.bulldog.justmap.mixins.RedstoneLevelAccessor;
import ru.bulldog.justmap.util.math.MathUtil;

public class ColorProviders implements ColorProvider {
	
	private final ColorPalette colorPalette = ColorPalette.getInstance();
	private final Map<Block, ColorProvider> providers = Maps.newHashMap();

	public static ColorProviders registerProviders() {
		ColorProviders blockColors = new ColorProviders();
		blockColors.registerColorProvider((state, world, pos) -> {
			return blockColors.getGrassColor(world, pos);
		}, Blocks.LARGE_FERN, Blocks.TALL_GRASS, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
		blockColors.registerColorProvider((state, world, pos) -> {
			return ColorPalette.SPRUCE_LEAVES;
		}, Blocks.SPRUCE_LEAVES);
		blockColors.registerColorProvider((state, world, pos) -> {
			return ColorPalette.BIRCH_LEAVES;
		}, Blocks.BIRCH_LEAVES);
		blockColors.registerColorProvider((state, world, pos) -> {
			return blockColors.getFoliageColor(world, pos);
		}, Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE);
		blockColors.registerColorProvider((state, world, pos) -> {
			return blockColors.getWaterColor(world, pos);
		}, Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.CAULDRON);
		blockColors.registerColorProvider((state, world, pos) -> {
			int power = state.get(RedstoneWireBlock.POWER);
			Vector3f powerVector = RedstoneLevelAccessor.getPowerVectors()[power];
			return MathUtil.packRgb(powerVector.getX(), powerVector.getY(), powerVector.getZ());
		}, Blocks.REDSTONE_WIRE);
		blockColors.registerColorProvider((state, world, pos) -> {
			return blockColors.getGrassColor(world, pos);
		}, Blocks.SUGAR_CANE);
		blockColors.registerColorProvider((state, world, pos) -> {
			return ColorPalette.ATTACHED_STEM;
		}, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
		blockColors.registerColorProvider((state, world, pos) -> {
			int age = state.get(StemBlock.AGE);
			int i = age * 32;
			int j = 255 - age * 8;
			int k = age * 4;
			return i << 16 | j << 8 | k;
		}, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
		blockColors.registerColorProvider((state, world, pos) -> {
			return ColorPalette.LILY_PAD;
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
				int bx = pos.getX() >> 2;
				int by = pos.getY() >> 2;
				int bz = pos.getZ() >> 2;
				Biome biome = chunk.getBiomeArray().getBiomeForNoiseGen(bx, by, bz);
				return this.colorPalette.getGrassColor(biome, pos.getX(), pos.getZ());
			}
		}
		return -1;
	}
	
	public int getFoliageColor(World world, BlockPos pos) {
		if (world != null && pos != null) {
			Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES, false);
			if (chunk != null && chunk.getBiomeArray() != null) {
				int bx = pos.getX() >> 2;
				int by = pos.getY() >> 2;
				int bz = pos.getZ() >> 2;
				Biome biome = chunk.getBiomeArray().getBiomeForNoiseGen(bx, by, bz);
				int color = this.colorPalette.getFoliageColor(biome);
				if (color == 0x0) {
					color = biome.getFoliageColor();
					this.colorPalette.addFoliageColor(biome, color);
				}
				return color;
			}
		}
		return -1;
	}
	
	public int getWaterColor(World world, BlockPos pos) {
		if (world != null && pos != null) {
			Chunk chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES, false);
			if (chunk != null && chunk.getBiomeArray() != null) {
				int bx = pos.getX() >> 2;
				int by = pos.getY() >> 2;
				int bz = pos.getZ() >> 2;
				Biome biome = chunk.getBiomeArray().getBiomeForNoiseGen(bx, by, bz);
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
