package ru.bulldog.justmap.map.data;

import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class BlockMeta {
	
	public final static BlockMeta EMPTY_BLOCK = new BlockMeta(null, null);
	
	private BlockState state;
	private BlockPos pos;	
	private int color = -1;
	private int heightPos = 0;
	
	public BlockMeta(World world, BlockPos pos) {
		if (world == null || pos == null) {
			this.state = null;
			this.pos = null;
		} else {		
			this.state = world.getBlockState(pos);
			this.pos = pos;
		}
	}
	
	public BlockState getState() {
		return this.state;
	}
	
	public BlockPos getPos() {
		return this.pos;
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
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof BlockMeta)) return false;
		
		BlockMeta block = (BlockMeta) obj;
		if(block.isEmpty()) {
			return this.isEmpty();
		}
		return this.pos.equals(block.pos) && this.state.equals(block.state);
	}
}
