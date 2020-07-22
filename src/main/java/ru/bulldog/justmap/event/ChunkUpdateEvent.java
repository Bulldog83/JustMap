package ru.bulldog.justmap.event;

import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.map.data.ChunkData;
import ru.bulldog.justmap.map.data.Layer;

public class ChunkUpdateEvent {
	public final Class<?> source;
	public final WorldChunk worldChunk;
	public final ChunkData mapChunk;
	public final Layer layer;
	public final int level;
	
	public ChunkUpdateEvent(Class<?> source, WorldChunk worldChunk, ChunkData mapChunk, Layer layer, int level) {
		this.source = source;
		this.worldChunk = worldChunk;
		this.mapChunk = mapChunk;
		this.layer = layer;
		this.level = level;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof ChunkUpdateEvent)) return false;		
		ChunkUpdateEvent event = (ChunkUpdateEvent) obj;
		return this.worldChunk.equals(event.worldChunk);
	}
}
