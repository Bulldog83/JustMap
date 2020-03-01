package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.data.MapProcessor.Layer;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.world.Heightmap;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MapChunk {

	private Layer layer;
	private WorldChunk worldChunk;	
	private Map<Layer, ChunkLevel[]> levels = new HashMap<>();
	private ChunkLevel chunkLevel;	
	private boolean empty;	
	private ChunkPos chunkPos;	
	private int level = 0;
	
	public long updated = 0;
	
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
		this.heightmap = new int[16 * 16];
		
		if (!levels.containsKey(layer)) {
			initLayer();
		}
		
		chunkLevel = levels.get(layer)[level];
	}
	
	public void resetChunk() {
		this.levels.clear();
		
		initLayer();
		
		this.chunkLevel = levels.get(layer)[level];
		this.updated = 0;
	}
	
	private void initLayer() {
		int levels;
		if (layer == Layer.CAVES) {
			levels = worldChunk.getWorld().getEffectiveHeight() >> ClientParams.chunkLevelSize;
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
	
	public void updateWorldChunk() {
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
		return chunkLevel.image;
	}
	
	public Layer getLayer() {
		return layer;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int[] getHeighmap() {
		return chunkLevel.heightmap;
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
			this.chunkLevel = new ChunkLevel();
			levels.get(layer)[level] = chunkLevel;
		} else {
			this.chunkLevel = levels.get(layer)[level];
		}
	}
	
	public WorldChunk getWorldChunk() {
		return worldChunk;
	}
	
	public void updateHeighmap() {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int y = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
				chunkLevel.heightmap[x + (z << 4)] = MapProcessor.getTopBlockY(this, x, y, z, true);
			}
		}
	}
	
	public void update() {		
		long currentTime = System.currentTimeMillis();
		if (currentTime - updated < ClientParams.chunkUpdateInterval) return;		
		
		MapChunk eastChunk = MapCache.get(worldChunk.getWorld()).getChunk(chunkPos.x + 1, chunkPos.z, true);
		MapChunk southChunk = MapCache.get(worldChunk.getWorld()).getChunk(chunkPos.x, chunkPos.z - 1, true);
		
		if (currentTime - chunkLevel.updated > ClientParams.chunkLevelUpdateInterval) {
			this.updateHeighmap();
			eastChunk.updateHeighmap();
			southChunk.updateHeighmap();
			
			chunkLevel.updated = currentTime;
		}
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int index = x + (z << 4);
				int posY = chunkLevel.heightmap[index];
				
				BlockMeta currentBlock = chunkLevel.getBlock(index);
				if (posY == -1) {
					if (!currentBlock.isEmpty()) {
						chunkLevel.setBlock(index, BlockMeta.EMPTY_BLOCK);
						chunkLevel.image.setPixelRgba(x, z, Colors.BLACK);
					}
					
					continue;
				}
				
				int posX = x + (chunkPos.x << 4);
				int posZ = z + (chunkPos.z << 4);
				
				BlockMeta block = new BlockMeta(new BlockPos(posX, posY, posZ));
				if(currentBlock.isEmpty() || !currentBlock.equals(block)) {					
					int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
					block.setHeightPos(heightDiff);
					block.setColor(ColorUtil.blockColor(worldChunk, block));
					
					chunkLevel.setBlock(index, block);
					
					int color = ColorUtil.proccessColor(block.getColor(), heightDiff);
					chunkLevel.image.setPixelRgba(x, z, color);
				} else {				
					int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
					if (currentBlock.getHeightPos() != heightDiff) {
						currentBlock.setHeightPos(heightDiff);
						
						int color = ColorUtil.proccessColor(currentBlock.getColor(), heightDiff);
						chunkLevel.image.setPixelRgba(x, z, color);
					}
				}
				
				image.setPixelRgba(x, z, color);
			}
		}
		
		this.empty = false;
		this.updated = currentTime;
	}
	
	private class ChunkLevel {
		private BlockMeta[] blocks = new BlockMeta[256];
		
		private final NativeImage image = new NativeImage(16, 16, false);
		private final int[] heightmap = new int[256];
		
		public long updated = 0;
		
		private ChunkLevel() {
			Arrays.fill(blocks, BlockMeta.EMPTY_BLOCK);
			Arrays.fill(heightmap, -1);
			
			image.fillRect(0, 0, 16, 16, Colors.BLACK);
		}
		
		public void setBlock(int pos, BlockMeta block) {
			blocks[pos] = block;
		}
		
		public BlockMeta getBlock(int pos) {
			return blocks[pos];
		}
	}
}
