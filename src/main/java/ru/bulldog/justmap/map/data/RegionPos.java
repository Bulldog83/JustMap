package ru.bulldog.justmap.map.data;

public class RegionPos {
	public final int x;
	public final int z;
	
	public RegionPos(int blockX, int blockZ) {
		this.x = blockX >> 9;
		this.z = blockZ >> 9;
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
