package ru.bulldog.justmap.map.data;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;

public class RegionPos {
	public final int x;
	public final int z;
	
	public RegionPos(BlockPos blockPos) {
		this(new ChunkPos(blockPos));
	}
	
	public RegionPos(ChunkPos pos) {
		this(pos.getRegionX(),
			 pos.getRegionZ());
	}
	
	public RegionPos(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	@Override
	public String toString() {
		return String.format("r.%d.%d", x, z);
	}
	
	@Override
	public int hashCode() {
		return ((x & 65535) << 16) | (z & 65535);
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof RegionPos)) return false;
		
		RegionPos pos = (RegionPos) obj;
		return this.x == pos.x && this.z == pos.z;
	}
}
