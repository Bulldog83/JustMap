package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.minimap.data.MapProcessor.Layer;
import ru.bulldog.justmap.util.ColorUtil;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.world.Heightmap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.common.collect.Lists;

public class MapChunk {

	private Layer layer;
	private WorldChunk worldChunk;
	
	private NativeImage image = new NativeImage(16, 16, false);	
	
	private Map<Layer, List<ChunkLevel>> levels = new HashMap<>();
	
	private ChunkLevel currentLevel;
	
	private boolean updating;
	private boolean empty;
	
	public long updated;
	public int dimension;
	
	private ChunkPos chunkPos;
	private int level = 0;
	
	public int[] heightmap = new int[256];
	
	public MapChunk(WorldChunk chunk, Layer layer) {
		this.chunkPos = chunk.getPos();
		this.layer = layer;
		this.worldChunk = chunk;
		
		if (!levels.containsKey(layer)) {
			initLayer();
		}
		
		currentLevel = getBlocks();
		
		Arrays.fill(this.heightmap, -1);
	}
	
	private void initLayer() {
		levels.put(layer, Lists.newLinkedList());
		levels.get(layer).add(level, new ChunkLevel());
	}
	
	private ChunkLevel getBlocks() {
		return levels.get(layer).get(level);
	}
	
	public void setChunk(WorldChunk chunk) {
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
	
	public Layer getLayer() {
		return layer;
	}
	
	public void setLayer(Layer layer) {
		this.layer = layer;		
		if (!levels.containsKey(layer)) {
			initLayer();			
		}
	}
	
	public void setLevel(int level) {
		this.level = level;
		if (level > 0 && level >= levels.get(layer).size()) {
			this.currentLevel = new ChunkLevel();
			levels.get(layer).add(level, currentLevel);
		} else {
			this.currentLevel = levels.get(layer).get(level);
		}
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
				
				BlockMeta currentBlock = currentLevel.getBlock(index);
				
				BlockMeta block = MapProcessor.getTopBlock(this, layer, x, z);
				if(currentBlock.isEmpty() || !currentBlock.equals(block)) {					
					int heightDiff = MapProcessor.heightDifference(worldChunk, x, z, block.pos.getY() + 1, layer);
					block.setHeightPos(heightDiff);
					if (block.getColor() == -1) {
						block.setColor(ColorUtil.blockColor(worldChunk, block));
					}
					
					currentLevel.setBlock(index, block);
					
					int color = ColorUtil.proccessColor(block.getColor(), heightDiff);
					image.setPixelRgba(x, z, color);
				} else {				
					int heightDiff = MapProcessor.heightDifference(worldChunk, x, z, currentBlock.pos.getY() + 1, layer);
					if (currentBlock.getHeightPos() != heightDiff) {
						currentBlock.setHeightPos(heightDiff);
						
						int color = ColorUtil.proccessColor(currentBlock.getColor(), heightDiff);
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
