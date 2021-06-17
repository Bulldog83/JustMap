package ru.bulldog.justmap.util.colors;

import com.google.common.collect.Maps;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.level.block.StemBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkStatus;
import net.minecraft.world.phys.Vec3;
import ru.bulldog.justmap.mixins.RedstoneLevelAccessor;
import ru.bulldog.justmap.util.math.MathUtil;

import java.util.Map;

public class ColorProviders implements ColorProvider {
	
	public final static ColorProviders INSTANCE = registerProviders();
	
	private final Colors colorPalette = Colors.INSTANCE;
	private final Map<Block, ColorProvider> providers = Maps.newHashMap();
	
	private ColorProviders() {}

	private static ColorProviders registerProviders() {
		ColorProviders blockColors = new ColorProviders();
		blockColors.registerColorProvider((state, world, pos) -> blockColors.getGrassColor(world, pos), Blocks.LARGE_FERN, Blocks.TALL_GRASS, Blocks.GRASS_BLOCK, Blocks.FERN, Blocks.GRASS, Blocks.POTTED_FERN);
		blockColors.registerColorProvider((state, world, pos) -> blockColors.getGrassColor(world, pos), Blocks.SUGAR_CANE);
		blockColors.registerColorProvider((state, world, pos) -> Colors.SPRUCE_LEAVES, Blocks.SPRUCE_LEAVES);
		blockColors.registerColorProvider((state, world, pos) -> Colors.BIRCH_LEAVES, Blocks.BIRCH_LEAVES);
		blockColors.registerColorProvider((state, world, pos) -> blockColors.getFoliageColor(world, pos), Blocks.OAK_LEAVES, Blocks.JUNGLE_LEAVES, Blocks.ACACIA_LEAVES, Blocks.DARK_OAK_LEAVES, Blocks.VINE);
		blockColors.registerColorProvider((state, world, pos) -> blockColors.getWaterColor(world, pos), Blocks.WATER, Blocks.BUBBLE_COLUMN, Blocks.CAULDRON);
		blockColors.registerColorProvider((state, world, pos) -> {
			int power = state.getValue(RedStoneWireBlock.POWER);
			Vec3 powerVector = RedstoneLevelAccessor.getColors()[power];
			return MathUtil.packRgb((float) powerVector.x(), (float) powerVector.y(), (float) powerVector.z());
		}, Blocks.REDSTONE_WIRE);
		blockColors.registerColorProvider((state, world, pos) -> Colors.ATTACHED_STEM, Blocks.ATTACHED_MELON_STEM, Blocks.ATTACHED_PUMPKIN_STEM);
		blockColors.registerColorProvider((state, world, pos) -> {
			int age = state.getValue(StemBlock.AGE);
			int i = age * 32;
			int j = 255 - age * 8;
			int k = age * 4;
			return i << 16 | j << 8 | k;
		}, Blocks.MELON_STEM, Blocks.PUMPKIN_STEM);
		blockColors.registerColorProvider((state, world, pos) -> Colors.LILY_PAD, Blocks.LILY_PAD);
		
		return blockColors;
	}
	
	public void registerColorProvider(ColorProvider provider, Block... blocks) {
		for (Block block : blocks) {
			providers.put(block, provider);
		}
	}
	
	public int getGrassColor(Level world, BlockPos pos) {
		if (world != null && pos != null) {
			ChunkAccess chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES, false);
			if (chunk != null && chunk.getBiomes() != null) {
				int bx = pos.getX() >> 2;
				int by = pos.getY() >> 2;
				int bz = pos.getZ() >> 2;
				Biome biome = chunk.getBiomes().getNoiseBiome(bx, by, bz);
				return colorPalette.getGrassColor(world, biome, pos.getX(), pos.getZ());
			}
		}
		return Colors.GRASS;
	}
	
	public int getFoliageColor(Level world, BlockPos pos) {
		if (world != null && pos != null) {
			ChunkAccess chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES, false);
			if (chunk != null && chunk.getBiomes() != null) {
				int bx = pos.getX() >> 2;
				int by = pos.getY() >> 2;
				int bz = pos.getZ() >> 2;
				Biome biome = chunk.getBiomes().getNoiseBiome(bx, by, bz);
				return colorPalette.getFoliageColor(world, biome);
			}
		}
		return Colors.FOLIAGE;
	}
	
	public int getWaterColor(Level world, BlockPos pos) {
		if (world != null && pos != null) {
			ChunkAccess chunk = world.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.BIOMES, false);
			if (chunk != null && chunk.getBiomes() != null) {
				int bx = pos.getX() >> 2;
				int by = pos.getY() >> 2;
				int bz = pos.getZ() >> 2;
				Biome biome = chunk.getBiomes().getNoiseBiome(bx, by, bz);
				return colorPalette.getWaterColor(world, biome);
			}
		}
		return -1;
	}

	@Override
	public int getColor(BlockState state, Level world, BlockPos pos) {
		ColorProvider provider = this.providers.get(state.getBlock());
		if (provider != null) {
			return provider.getColor(state, world, pos);
		}
		return -1;
	}
}
