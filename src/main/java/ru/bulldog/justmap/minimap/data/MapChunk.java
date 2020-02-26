package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.util.ColorUtil;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.world.Heightmap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import java.util.Arrays;

public class MapChunk {

	private MapProcessor.Layer layer;
	private WorldChunk worldChunk;
	
	private NativeImage image = new NativeImage(16, 16, false);	
	
	private BlockMeta[] blocks = new BlockMeta[256];
	public int[] heightmap = new int[256];
	
	private boolean updating;
	private boolean empty;
	
	public long updated;
	public int dimension;
	
	private ChunkPos chunkPos;
	
	public MapChunk(WorldChunk chunk, MapProcessor.Layer layer) {
		this.chunkPos = chunk.getPos();
		this.layer = layer;
		this.worldChunk = chunk;
		
		Arrays.fill(this.heightmap, -1);
		Arrays.fill(this.blocks, BlockMeta.EMPTY_BLOCK);
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
		if (this.updating) { return; }
		
		this.updating = true;		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int index = x + z * 16;
				heightmap[index] = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
				
				BlockMeta block = MapProcessor.getTopBlock(this, layer, x, z);
				if(blocks[index].isEmpty() || !blocks[index].equals(block)) {					
					int heightDiff = MapProcessor.heightDifference(worldChunk, x, z, block.pos.getY() + 1, false);
					block.setHeightPos(heightDiff);
					block.setColor(ColorUtil.blockColor(worldChunk, block));
					
					blocks[index] = block;
					
					int color = ColorUtil.proccessColor(block.getColor(), heightDiff);
					image.setPixelRgba(x, z, color);
				} else {				
					int heightDiff = MapProcessor.heightDifference(worldChunk, x, z, blocks[index].pos.getY() + 1, false);
					if (blocks[index].getHeightPos() != heightDiff) {
						blocks[index].setHeightPos(heightDiff);
						
						int color = ColorUtil.proccessColor(blocks[index].getColor(), heightDiff);
						image.setPixelRgba(x, z, color);
					}
				}
			}
		}
		
		this.empty = false;
		this.updated = System.currentTimeMillis();
		this.updating = false;
	}
}
