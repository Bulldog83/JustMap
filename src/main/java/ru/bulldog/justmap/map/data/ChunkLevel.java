package ru.bulldog.justmap.map.data;

import java.util.Arrays;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
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
	
	private Object arrayLock = new Object();
	
	ChunkLevel(int level) {
		this.statemap = new int[256];
		this.heightmap = new int[256];
		this.colormap = new int[256];
		this.levelmap = new int[256];
		this.topomap = new int[256];		
		this.level = level;
		
		int airId = Block.getId(StateUtil.AIR);		
		Arrays.fill(statemap, airId);
		Arrays.fill(colormap, -1);
		Arrays.fill(heightmap, -1);
		Arrays.fill(levelmap, 0);
		Arrays.fill(topomap, 0);
	}
	
	public BlockState getBlockState(int x, int z) {
		return Block.stateById(statemap[index(x, z)]);
	}
	
	public void setBlockState(int x, int z, BlockState blockState) {
		synchronized (arrayLock) {
			this.statemap[index(x, z)] = Block.getId(blockState);
		}
	}
	
	public int sampleHeightmap(int x, int z) {
		return this.sampleHeightmap(index(x, z));
	}
	
	public int sampleHeightmap(int index) {
		return this.heightmap[index];
	}
	
	public void updateHeightmap(int x, int z, int y) {
		int index = index(x, z);
		synchronized (arrayLock) {
			if (heightmap[index] != y) {
				this.setBlockState(x, z, StateUtil.AIR);
				heightmap[index] = y;
			}
		}
	}
	
	public void clear(int x, int z) {
		int index = index(x, z);
		synchronized (arrayLock) {
			if (heightmap[index] != -1) {
				this.setBlockState(x, z, StateUtil.AIR);
				this.heightmap[index] = -1;
			}
			
			this.colormap[index] = -1;
			this.levelmap[index] = 0;
			this.topomap[index] = 0;
		}
	}
	
	private int index(int x, int z) {
		return x + (z << 4);
	}
	
	public boolean isEmpty() {
		return this.level == -1;
	}
}
