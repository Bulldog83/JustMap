package ru.bulldog.justmap.minimap.data;

import java.util.Arrays;

public class ChunkLevel {
	private BlockMeta[] blocks = new BlockMeta[256];
	
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
