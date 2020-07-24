package ru.bulldog.justmap.map.data;

import java.util.Arrays;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

import ru.bulldog.justmap.util.StateUtil;

public class ChunkLevel {
	
	int[] statemap;
	int[] heightmap;
	int[] colormap;
	int[] levelmap;
	int[] topomap;
	
	boolean updating = false;
	long updated = 0;
	long refreshed = 0;
	int level;
	
	ChunkLevel(int level) {
		this.statemap = new int[256];
		this.heightmap = new int[256];
		this.colormap = new int[256];
		this.levelmap = new int[256];
		this.topomap = new int[256];		
		this.level = level;
		
		int airId = Block.getRawIdFromState(StateUtil.AIR);		
		Arrays.fill(statemap, airId);
		Arrays.fill(colormap, -1);
		Arrays.fill(heightmap, -1);
		Arrays.fill(levelmap, 0);
		Arrays.fill(topomap, 0);
	}
	
	public BlockState getBlockState(int x, int z) {
		int index = index(x, z);
		return Block.getStateFromRawId(statemap[index]);
	}
	
	public void setBlockState(int x, int z, BlockState blockState) {
		int index = index(x, z);
		this.statemap[index] = Block.getRawIdFromState(blockState);
	}
	
	public void updateHeightmap(int x, int z, int y) {
		int index = index(x, z);
		if (heightmap[index] != y) {
			this.setBlockState(x, z, StateUtil.AIR);
			heightmap[index] = y;
		}
	}
	
	public void clear(int x, int z) {
		int index = index(x, z);
		if (heightmap[index] != -1) {
			this.setBlockState(x, z, StateUtil.AIR);
			this.heightmap[index] = -1;
		}
		
		this.colormap[index] = -1;
		this.levelmap[index] = 0;
		this.topomap[index] = 0;
	}
	
	private int index(int x, int z) {
		return x + (z << 4);
	}
	
	public boolean isEmpty() {
		return this.level == -1;
	}
}
