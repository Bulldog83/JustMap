package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.data.MapProcessor.Layer;
import ru.bulldog.justmap.util.ColorUtil;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.world.Heightmap;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MapChunk {

	private Layer layer;
	private WorldChunk worldChunk;
	
	private Map<Layer, ChunkLevel[]> levels = new HashMap<>();
	
	private ChunkLevel currentLevel;
	
	private boolean updating;
	private boolean empty;
	
	public long updated;
	
	private ChunkPos chunkPos;
	private int level = 0;
	
	public MapChunk(WorldChunk chunk, Layer layer, int level) {
		this.level = level;
		initChunk(chunk, layer);
	}
	
	public MapChunk(WorldChunk chunk, Layer layer) {
		initChunk(chunk, layer);
	}
	
	private void initChunk(WorldChunk chunk, Layer layer) {
		this.chunkPos = chunk.getPos();
		this.layer = layer;
		this.worldChunk = chunk;
		
		if (!levels.containsKey(layer)) {
			initLayer();
		}
		
		currentLevel = levels.get(layer)[level];
		
		Arrays.fill(currentLevel.heightmap, -1);
	}
	
	private void initLayer() {
		int levels;
		if (layer == Layer.CAVES) {
			levels = worldChunk.getWorld().getEffectiveHeight() >> ClientParams.levelSize;
		} else {
			levels = 1;
		}
		
		this.levels.put(layer, new ChunkLevel[levels]);
		this.levels.get(layer)[level] = new ChunkLevel();
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
		return currentLevel.image;
	}
	
	public Layer getLayer() {
		return layer;
	}
	
	public int[] getHeighmap() {
		return currentLevel.heightmap;
	}
	
	private void setLayer(Layer layer) {
		this.layer = layer;		
		
		if (!levels.containsKey(layer)) {
			initLayer();			
		}
	}
	
	public void setLevel(Layer layer, int level) {
		if (this.layer == layer &&
			this.level == level) return;
		
		this.level = level;		
		this.setLayer(layer);
		
		if (levels.get(layer)[level] == null) {
			this.currentLevel = new ChunkLevel();
			levels.get(layer)[level] = currentLevel;
		} else {
			this.currentLevel = levels.get(layer)[level];
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
				int index = x + (z << 4);
				currentLevel.heightmap[index] = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
				
				BlockMeta currentBlock = currentLevel.getBlock(index);
				
				BlockMeta block = MapProcessor.getTopBlock(this, layer, x, z);
				if(currentBlock.isEmpty() || !currentBlock.equals(block)) {					
					int heightDiff = MapProcessor.heightDifference(worldChunk, x, z, block.pos.getY() + 1, layer);
					block.setHeightPos(heightDiff);
					block.setColor(ColorUtil.blockColor(worldChunk, block));
					
					currentLevel.setBlock(index, block);
					
					int color = ColorUtil.proccessColor(block.getColor(), heightDiff);
					currentLevel.image.setPixelRgba(x, z, color);
				} else {				
					int heightDiff = MapProcessor.heightDifference(worldChunk, x, z, currentBlock.pos.getY() + 1, layer);
					if (currentBlock.getHeightPos() != heightDiff) {
						currentBlock.setHeightPos(heightDiff);
						
						int color = ColorUtil.proccessColor(currentBlock.getColor(), heightDiff);
						currentLevel.image.setPixelRgba(x, z, color);
					}
				}
			}
		}
		
		this.empty = false;
		this.updated = System.currentTimeMillis();
		this.updating = false;
	}
}
