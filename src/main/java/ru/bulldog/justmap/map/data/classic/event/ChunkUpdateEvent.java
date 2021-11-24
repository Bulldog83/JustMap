package ru.bulldog.justmap.map.data.classic.event;

import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.classic.ChunkData;
import ru.bulldog.justmap.util.math.Plane;

public class ChunkUpdateEvent {
	public final WorldChunk worldChunk;
	public final ChunkData mapChunk;
	public final Layer layer;
	public final Plane updateArea;
	public final boolean update;
	public final boolean full;
	public final int level;

	public ChunkUpdateEvent(WorldChunk worldChunk, ChunkData mapChunk, Layer layer, int level, int x, int z, int w, int h, boolean update) {
		this.full = (x == 0 && z == 0 && w == 16 && h == 16);
		this.worldChunk = worldChunk;
		this.mapChunk = mapChunk;
		this.layer = layer;
		this.level = level;
		this.update = update;
		this.updateArea = new Plane(x, z, w, h);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof ChunkUpdateEvent)) return false;
		ChunkUpdateEvent event = (ChunkUpdateEvent) obj;
		return this.chunkEquals(event.worldChunk) &&
			   this.layer.equals(event.layer) &&
			   this.level == event.level &&
			   this.full == event.full &&
			   this.updateArea.equals(event.updateArea);
	}

	private boolean chunkEquals(WorldChunk chunk) {
		return this.worldChunk.getPos().equals(chunk.getPos());
	}
}
