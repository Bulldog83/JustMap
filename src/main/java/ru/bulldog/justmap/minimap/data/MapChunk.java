package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.data.MapProcessor.Layer;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;

import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.awt.image.BufferedImage;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MapChunk {

	private Layer layer;
	private WorldChunk worldChunk;
	private Map<Layer, ChunkLevel[]> levels;
	private ChunkLevel chunkLevel;
	private boolean empty;
	private ChunkPos chunkPos;
	private int level = 0;
	
	public long updated = 0;
	
	public MapChunk(World world, ChunkPos pos, Layer layer, int level) {
		this.level = level;
		initChunk(world, pos, layer);
	}
	
	public MapChunk(World world, ChunkPos pos, Layer layer) {
		initChunk(world, pos, layer);
	}
	
	private void initChunk(World world, ChunkPos pos, Layer layer) {
		this.worldChunk = world.getChunk(pos.x, pos.z);
		this.chunkPos = pos;
		this.layer = layer;		
		levels = new HashMap<>();
		
		initLayer();
	}
	
	public void resetChunk() {
		this.levels.clear();
		this.updated = 0;
		
		initLayer();
	}
	
	private void initLayer() {
		int levels;
		if (layer == Layer.CAVES) {
			levels = worldChunk.getHeight() >> ClientParams.chunkLevelSize;
		} else {
			levels = 1;
		}
		
		this.levels.put(layer, new ChunkLevel[levels]);
		
		this.chunkLevel = new ChunkLevel();
		this.levels.get(layer)[level] = chunkLevel;
	}
	
	public void setChunk(WorldChunk chunk) {
		this.worldChunk = chunk;
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
	
	public synchronized BufferedImage getImage() {
		return chunkLevel.image;
	}
	
	public Layer getLayer() {
		return layer;
	}
	
	public int getLevel() {
		return level;
	}
	
	public synchronized int[] getHeighmap() {
		return chunkLevel.heightmap;
	}
	
	public void setLevel(Layer layer, int level) {
		if (this.layer == layer &&
			this.level == level) return;
		
		this.level = level;		
		this.layer = layer;
		
		if (!levels.containsKey(layer)) {
			initLayer();			
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
				getHeighmap()[x + (z << 4)] = MapProcessor.getTopBlockY(this, x, y, z, true);
			}
		}
	}
	
	public void update() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - updated < ClientParams.chunkUpdateInterval) return;		
		
		MapChunk eastChunk = MapCache.get().getChunk(chunkPos.x + 1, chunkPos.z, true);
		MapChunk southChunk = MapCache.get().getChunk(chunkPos.x, chunkPos.z - 1, true);
		
		if (currentTime - chunkLevel.updated > ClientParams.chunkLevelUpdateInterval) {
			this.updateHeighmap();
			eastChunk.updateHeighmap();
			southChunk.updateHeighmap();
			
			chunkLevel.updated = currentTime;
		}
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int index = x + (z << 4);
				int posY = getHeighmap()[index];
				
				BlockMeta currentBlock = chunkLevel.getBlock(index);
				if (posY == -1) {
					if (!currentBlock.isEmpty()) {
						chunkLevel.setBlock(index, BlockMeta.EMPTY_BLOCK);
						getImage().setRGB(x, z, Colors.BLACK);
						
						updateRegionImage();
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
					getImage().setRGB(x, z, color);
					
					updateRegionImage();
				} else {				
					int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
					if (currentBlock.getHeightPos() != heightDiff) {
						currentBlock.setHeightPos(heightDiff);
						
						int color = ColorUtil.proccessColor(currentBlock.getColor(), heightDiff);
						getImage().setRGB(x, z, color);
						
						updateRegionImage();
					}
				}
			}
		}
		
		this.empty = false;
		this.updated = currentTime;
	}
	
	private void updateRegionImage() {
		MapCache.get().getRegion(chunkPos).storeChunk(this);
	}
	
	private class ChunkLevel {
		private final BlockMeta[] blocks;
		private final int[] heightmap;
		private BufferedImage image;
		
		public long updated = 0;
		
		private ChunkLevel() {
			blocks = new BlockMeta[256];
			heightmap = new int[256];
			
			Arrays.fill(blocks, BlockMeta.EMPTY_BLOCK);
			Arrays.fill(heightmap, -1);
			
			image = loadImage();
		}
		
		public void setBlock(int pos, BlockMeta block) {
			blocks[pos] = block;
		}
		
		public BlockMeta getBlock(int pos) {
			return blocks[pos];
		}
		
		private BufferedImage loadImage() {
			MapRegion region = MapCache.get().getRegion(chunkPos);			
			BufferedImage image = region.getChunkImage(chunkPos);
			if (image == null) {
				image = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
				ImageUtil.fillImage(image, Colors.BLACK);
			}
			
			return image;
		}
	}
}
