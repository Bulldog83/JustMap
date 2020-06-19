package ru.bulldog.justmap.map.data;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.world.chunk.IdListPalette;
import net.minecraft.world.chunk.Palette;
import net.minecraft.world.chunk.PalettedContainer;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.StateUtil;

public class ChunkLevel {
	
	private final static Palette<BlockState> palette;	
	private volatile PalettedContainer<BlockState> container;
	
	int[] heightmap;
	int[] colormap;
	int[] colordata;
	int[] levelmap;
	int[] topomap;
	
	long updated = 0;
	long refreshed = 0;
	int level;
	
	ChunkLevel(int level) {
		this.container = new PalettedContainer<>(palette, Block.STATE_IDS, NbtHelper::toBlockState, NbtHelper::fromBlockState, StateUtil.AIR);		
		this.heightmap = new int[256];
		this.colormap = new int[256];
		this.colordata = new int[256];
		this.levelmap = new int[256];
		this.topomap = new int[256];
		
		this.level = level;
		
		Arrays.fill(colormap, -1);
		Arrays.fill(heightmap, -1);
		Arrays.fill(levelmap, 0);
		Arrays.fill(topomap, 0);
		Arrays.fill(colordata, Colors.BLACK);
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
		this.topomap[index] = 0;
		this.colordata[index] = Colors.BLACK;
	}
	
	public boolean isEmpty() {
		return this.level == -1;
	}
	
	public void store(CompoundTag tag) {
		tag.putIntArray("Heightmap", heightmap);
		tag.putIntArray("Colormap", colormap);
		tag.putIntArray("Levelmap", levelmap);
		tag.putIntArray("Topomap", levelmap);
	}
	
	public void load(CompoundTag tag) {
		if (tag.isEmpty()) return;
		
		this.heightmap = tag.getIntArray("Heightmap");
		this.colormap = tag.getIntArray("Colormap");
		this.levelmap = tag.getIntArray("Levelmap");
		if (tag.contains("Topomap")) {
			this.topomap = tag.getIntArray("Topomap");
		}
		
		for (int i = 0; i < 256; i++) {
			int color = this.colormap[i];
			if (color != -1) {
				int level = this.levelmap[i];
				this.colordata[i] = ColorUtil.proccessColor(color, level);
			}			
		}
	}
	
	static {
		palette = new IdListPalette<>(Block.STATE_IDS, StateUtil.AIR);
	}
}
