package ru.bulldog.justmap.map.data;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtHelper;
import net.minecraft.util.math.BlockPos;

public class BlockMeta {
	
	public final static BlockMeta EMPTY_BLOCK = new BlockMeta(null);
	
	private BlockState state;
	private BlockPos pos;	
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
	
	public CompoundTag toNBT() {
		CompoundTag tag = new CompoundTag();
		
		if (isEmpty()) return tag;
		
		CompoundTag stateTag = NbtHelper.fromBlockState(state);
		CompoundTag posTag = NbtHelper.fromBlockPos(pos);
		
		tag.put("state", stateTag);
		tag.put("pos", posTag);
		tag.putInt("color", color);
		tag.putInt("heightPos", heightPos);
		
		return tag;
	}
	
	public static BlockMeta fromNBT(CompoundTag tag) {
		BlockMeta block = new BlockMeta(null);
		
		if (tag.isEmpty()) return block;
		
		block.pos = NbtHelper.toBlockPos(tag.getCompound("pos"));
		block.state = NbtHelper.toBlockState(tag.getCompound("state"));
		block.color = tag.getInt("color");
		block.heightPos = tag.getInt("heightPos");
		
		return block;
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
