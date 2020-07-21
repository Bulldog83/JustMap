package ru.bulldog.justmap.event;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class ChunkUpdateEvent {
	public final World world;
	public final ChunkPos chunkPos;
	
	public ChunkUpdateEvent(World world, ChunkPos updatedPos) {
		this.world = world;
		this.chunkPos = updatedPos;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChunkUpdateEvent)) return false;		
		ChunkUpdateEvent event = (ChunkUpdateEvent) obj;
		return this.world.equals(event.world) &&
			   this.chunkPos.equals(event.chunkPos);
	}
}
