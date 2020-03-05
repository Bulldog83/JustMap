package ru.bulldog.justmap.minimap.data;

import com.mojang.datafixers.Dynamic;
import com.mojang.datafixers.types.DynamicOps;

import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.datafixer.NbtOps;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
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
		DynamicOps<Tag> dynamicOps = NbtOps.INSTANCE;
		
		CompoundTag tag = new CompoundTag();
		
		CompoundTag stateTag = (CompoundTag) BlockState.serialize(dynamicOps, state).getValue();
		CompoundTag posTag = (CompoundTag) pos.serialize(dynamicOps);
		
		tag.put("state", stateTag);
		tag.put("pos", posTag);
		tag.putInt("color", color);
		tag.putInt("heightPos", heightPos);
		
		return tag;
	}
	
	public static BlockMeta fromNBT(CompoundTag tag) {
		DynamicOps<Tag> dynamicOps = NbtOps.INSTANCE;
		
		BlockMeta block = new BlockMeta(null);
		
		block.pos = BlockPos.deserialize(new Dynamic<Tag>(dynamicOps, tag.get("pos")));
		block.state = BlockState.deserialize(new Dynamic<Tag>(dynamicOps, tag.get("state")));
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
