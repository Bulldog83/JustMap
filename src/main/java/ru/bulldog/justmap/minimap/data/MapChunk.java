package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.util.Colors;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.world.Heightmap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;

import java.util.Arrays;

public class MapChunk {

	private MapProcessor.Layer layer;
	private WorldChunk worldChunk;
	
	private NativeImage image = new NativeImage(16, 16, false);
	
	private boolean updating;
	private boolean empty;
	
	public int[] heightmap;
	public long updated;
	
	private ChunkPos chunkPos;
	
	public MapChunk(WorldChunk chunk, MapProcessor.Layer layer) {
		this.chunkPos = chunk.getPos();
		this.layer = layer;
		this.worldChunk = chunk;
		this.heightmap = new int[16 * 16];
		
		Arrays.fill(heightmap, -1);
	}
	
	void setChunk(WorldChunk chunk) {
		this.worldChunk = chunk;
		chunkPos = chunk.getPos();
	}
	
	public void updateChunk() {
		this.worldChunk = worldChunk.getWorld().getChunk(chunkPos.x, chunkPos.z);
	}
	
	public void setPos(ChunkPos chunkPos) {
		this.chunkPos = chunkPos;
	}
	
	public ChunkPos getPos() {
		return this.chunkPos;
	}
	
	public int getX() {
		return this.chunkPos.x;
	}
	
	public int getZ() {
		return this.chunkPos.z;
	}
	
	public void setEmpty(boolean empty) {
		this.empty = empty;
	}
	
	public boolean isEmpty() {
		return this.empty;
	}
	
	public NativeImage getImage() {
		return image;
	}
	
	public MapProcessor.Layer getLayer() {
		return layer;
	}
	
	public WorldChunk getWorldChunk() {
		return worldChunk;
	}
	
	public void update() {
		if (updating) { return; }
		
		long currentTime = System.currentTimeMillis();
		
		updating = true;		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int color = Colors.BLACK;
				switch (layer) {
					case SURFACE:
						heightmap[x + z * 16] = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
						color = MapProcessor.surfaceColor(this, x, z);
					break;
					case CAVES:
						int ceiling = worldChunk.getWorld().getDimension().getType() == DimensionType.THE_NETHER ? 3 : 2;
						color = MapProcessor.cavesColor(this, x, z, ceiling);
					break;
				}
				
				image.setPixelRgba(x, z, color);
			}
		}
		
		empty = false;
		updated = currentTime;
		updating = false;
	}
}
