package ru.bulldog.justmap.map.data;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;

import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.StateUtil;

public class ChunkLevel {
	
	private final static Palette<BlockState> palette;
	
	private volatile PalettedContainer<BlockState> container;
	private volatile NativeImage image;
	
	int[] heightmap;
	int[] colormap;
	int[] levelmap;
	
	long updated = 0;
	long refreshed = 0;
	int level;
	
	ChunkLevel(int level) {
		this.container = new PalettedContainer<>(palette, Block.STATE_IDS, NbtHelper::toBlockState, NbtHelper::fromBlockState, StateUtil.AIR);		
		this.heightmap = new int[256];
		this.colormap = new int[256];
		this.levelmap = new int[256];
		
		this.level = level;
		
		Arrays.fill(heightmap, -1);
		Arrays.fill(colormap, -1);
		Arrays.fill(levelmap, 0);
	}
	
	public PalettedContainer<BlockState> container() {
		synchronized (container) {
			return this.container;
		}
	}
	
	public BlockState getBlockState(int x, int y, int z) {
		return container().get(x, y, z);
	}
	
	public BlockState setBlockState(int x, int y, int z, BlockState blockState) {
		return container().set(x, y, z, blockState);
	}
	
	public void updateHeightmap(int x, int z, int y) {
		int index = x + (z << 4);
		if (heightmap[index] != y) {
			setBlockState(x, heightmap[index] & 15, z, StateUtil.AIR);
			heightmap[index] = y;
		}
	}
	
	public void clear(int x, int z) {
		int index = x + (z << 4);
		if (heightmap[index] != -1) {
			setBlockState(x, heightmap[index] & 15, z, StateUtil.AIR);
			this.heightmap[index] = -1;
		}
		
		this.colormap[index] = -1;
		this.levelmap[index] = 0;
	}
	
	public NativeImage getImage(ChunkPos chunkPos) {
		if (image == null) {
			this.image = loadImage(chunkPos);
		}
		
		synchronized (image) {
			return this.image;
		}
	}
	
	private NativeImage loadImage(ChunkPos chunkPos) {
		MapRegion region = MapCache.get().getRegion(chunkPos);			
		NativeImage image = region.getChunkImage(chunkPos);
		if (image == null) {
			image = new NativeImage(16, 16, false);
			ImageUtil.fillImage(image, Colors.BLACK);
		}
		
		return image;
	}
	
	public boolean isEmpty() {
		return this.level == -1;
	}
	
	public void store(CompoundTag tag) {
		tag.putIntArray("Heightmap", heightmap);
		tag.putIntArray("Colormap", colormap);
		tag.putIntArray("Levelmap", levelmap);
	}
	
	public void load(CompoundTag tag) {
		if (tag.isEmpty()) return;
		
		this.heightmap = tag.getIntArray("Heightmap");
		this.colormap = tag.getIntArray("Colormap");
		this.levelmap = tag.getIntArray("Levelmap");
	}
	
	static {
		palette = new IdListPalette<>(Block.STATE_IDS, StateUtil.AIR);
	}
}
