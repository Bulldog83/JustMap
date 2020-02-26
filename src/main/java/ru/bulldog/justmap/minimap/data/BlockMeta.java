package ru.bulldog.justmap.minimap.data;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

public class BlockMeta {
	
	public final static BlockMeta EMPTY_BLOCK = new BlockMeta(null);
	
	public final BlockState state;
	public final BlockPos pos;
	
	private int color = -1;
	private int heightPos = 0;
	
	public BlockMeta(BlockPos pos) {
		if (pos == null) {
			this.state = null;
			this.pos = null;
		} else {		
			this.state = MinecraftClient.getInstance().world.getBlockState(pos);
			this.pos = pos;
		}
	}
	
	public void setColor(int color) {
		this.color = color;
	}
	
	public int getColor() {
		return this.color;
	}
	
	public void setHeightPos(int pos) {
		this.heightPos = pos;
	}
	
	public int getHeightPos() {
		return this.heightPos;
	}
	
	public boolean isEmpty() {
		return this.state == null;
	}
	
	public boolean equals(BlockMeta block) {
		if(block.isEmpty()) {
			return this.isEmpty();
		}
		return this.pos.equals(block.pos) && this.state.equals(block.state);
	}
}
