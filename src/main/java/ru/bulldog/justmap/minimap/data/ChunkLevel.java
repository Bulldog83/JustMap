package ru.bulldog.justmap.minimap.data;

import java.util.Arrays;

import net.minecraft.client.texture.NativeImage;

public class ChunkLevel {
	private BlockMeta[] blocks = new BlockMeta[256];
	
	public final NativeImage image = new NativeImage(16, 16, false);
	public final int[] heightmap = new int[256];
	
	public ChunkLevel() {
		Arrays.fill(blocks, BlockMeta.EMPTY_BLOCK);
	}
	
	public void setBlock(int pos, BlockMeta block) {
		blocks[pos] = block;
	}
	
	public BlockMeta getBlock(int pos) {
		return blocks[pos];
	}
}
