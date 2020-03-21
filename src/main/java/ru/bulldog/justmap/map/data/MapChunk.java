package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;

import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class MapChunk {

	public final ChunkLevel INVALID_LEVEL = new ChunkLevel();
	
	private volatile Map<Layer, ChunkLevel[]> levels;
	
	private WorldChunk worldChunk;
	private ChunkPos chunkPos;
	private Layer layer;
	private int level = 0;
	private boolean empty = true;
	
	public long updated = 0;
	
	public MapChunk(World world, ChunkPos pos, Layer layer, int level) {
		this(world, pos, layer);
		this.level = level > 0 ? level : 0;
	}
	
	public MapChunk(World world, ChunkPos pos, Layer layer) {
		this.worldChunk = world.getChunk(pos.x, pos.z);
		this.chunkPos = pos;
		this.layer = layer;
		this.levels = new HashMap<>();
		
		loadFromNBT();
	}
	
	public void resetChunk() {
		this.levels.clear();
		this.updated = 0;
	}
	
	private void initLayer() {
		initLayer(layer);
	}
	
	private void initLayer(Layer layer) {
		int levels = worldChunk.getHeight() / layer.height;		
		this.levels.put(layer, new ChunkLevel[levels]);
	}
	
	private synchronized ChunkLevel getChunkLevel() {
		return getChunkLevel(layer, level);
	}
	
	private synchronized ChunkLevel getChunkLevel(Layer layer, int level) {
		if (!levels.containsKey(layer)) {
			initLayer();
		}
		
		try {
			ChunkLevel chunkLevel;
			if (this.levels.get(layer)[level] == null) {
				chunkLevel = new ChunkLevel();
				this.levels.get(layer)[level] = chunkLevel;
			} else {
				chunkLevel = this.levels.get(layer)[level];
			}
			
			return chunkLevel;
		} catch (ArrayIndexOutOfBoundsException ex) {
			return INVALID_LEVEL;
		}
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
	
	public synchronized NativeImage getImage() {
		return getChunkLevel().getImage();
	}
	
	public Layer getLayer() {
		return layer;
	}
	
	public int getLevel() {
		return level;
	}
	
	public synchronized int[] getHeighmap() {
		return getChunkLevel().heightmap;
	}
	
	public void setLevel(Layer layer, int level) {
		if (this.layer == layer &&
			this.level == level) return;
		
		this.level = level;		
		this.layer = layer;
	}
	
	public WorldChunk getWorldChunk() {
		return worldChunk;
	}
	
	public void updateHeighmap() {
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int y = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
				y = MapProcessor.getTopBlockY(this, x, y, z, true);
				if (y != -1) {
					getHeighmap()[x + (z << 4)] = y;
				}
			}
		}
	}
	
	public void update() {
		long currentTime = System.currentTimeMillis();
		if (currentTime - updated < ClientParams.chunkUpdateInterval) return;		
		
		MapChunk eastChunk = MapCache.get().getChunk(chunkPos.x + 1, chunkPos.z, true);
		MapChunk southChunk = MapCache.get().getChunk(chunkPos.x, chunkPos.z - 1, true);
		
		ChunkLevel chunkLevel = getChunkLevel();
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
						chunkLevel.getImage().setPixelRgba(x, z, Colors.BLACK);
						
						updateRegionData();
					}
					
					continue;
				}
				
				int posX = x + (chunkPos.x << 4);
				int posZ = z + (chunkPos.z << 4);
				
				BlockMeta block = new BlockMeta(new BlockPos(posX, posY, posZ));
				if(currentBlock.isEmpty() || !currentBlock.equals(block)) {					
					int color = ColorUtil.blockColor(worldChunk, block);
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						
						block.setColor(color);
						block.setHeightPos(heightDiff);
						chunkLevel.setBlock(index, block);
						
						color = ColorUtil.proccessColor(color, heightDiff);
						chunkLevel.getImage().setPixelRgba(x, z, color);
					
						updateRegionData();
					}
				} else {				
					int color = currentBlock.getColor();					
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						if (currentBlock.getHeightPos() != heightDiff) {
							currentBlock.setHeightPos(heightDiff);
						
							color = ColorUtil.proccessColor(color, heightDiff);
							chunkLevel.getImage().setPixelRgba(x, z, color);
						
							updateRegionData();
						}
					}
				}
			}
		}
		
		this.empty = false;
		this.updated = currentTime;
	}
	
	private void updateRegionData() {
		MapCache.get().getRegion(chunkPos).storeChunk(this);
	}
	
	public void saveToNBT(CompoundTag data) {
		levels.forEach((layer, levels) -> {
			ListTag levelsTag = new ListTag();
			for (int i = 0; i < levels.length; i++) {
				levelsTag.addTag(i, levels[i].toNBT());
			}
			
			data.put(layer.name, levelsTag);
		});
	}
	
	public void loadFromNBT() {
		CompoundTag chunkData = MapCache.get().getRegion(chunkPos).getChunkData(chunkPos);
		if (chunkData.isEmpty()) return;
		
		levels.forEach((layer, levels) -> {
			if (chunkData.contains(layer.name)) {
				ListTag levelsTag = (ListTag) chunkData.get(layer.name);
				for (int i = 0; i < levels.length; i++) {
					CompoundTag level = levelsTag.getCompound(i);
					if (level != null) {
						levels[i] = new ChunkLevel();
						levels[i].fromNBT(level);
					}
				}
			}			
		});
	}
	
	private class ChunkLevel {
		
		private final BlockMeta[] blocks;
		private int[] heightmap;
		private NativeImage image;
		
		public long updated = 0;
		
		private ChunkLevel() {
			blocks = new BlockMeta[256];
			heightmap = new int[256];
			
			Arrays.fill(blocks, BlockMeta.EMPTY_BLOCK);
			Arrays.fill(heightmap, -1);
		}
		
		public NativeImage getImage() {
			if (image == null) {
				image = loadImage();
			}
			
			return image;
		}
		
		public void setBlock(int pos, BlockMeta block) {
			blocks[pos] = block;
		}
		
		public BlockMeta getBlock(int pos) {
			return blocks[pos];
		}
		
		private NativeImage loadImage() {
			MapRegion region = MapCache.get().getRegion(chunkPos);			
			NativeImage image = region.getChunkImage(chunkPos);
			if (image == null) {
				image = new NativeImage(16, 16, false);
				ImageUtil.fillImage(image, Colors.BLACK);
			}
			
			return image;
		}
		
		public CompoundTag toNBT() {
			CompoundTag tag = new CompoundTag();
			
			ListTag tagBlocks = new ListTag();
			for (int i = 0; i < blocks.length; i++) {
				tagBlocks.add(i, blocks[i].toNBT());
			}
			
			tag.put("blocks", tagBlocks);
			tag.putIntArray("heightmap", heightmap);
			
			return tag;
		}
		
		public void fromNBT(CompoundTag tag) {
			ListTag tagBlocks = (ListTag) tag.get("blocks");
			if (!tagBlocks.isEmpty()) {
				for (int i = 0; i < this.blocks.length; i++) {
					CompoundTag block = tagBlocks.getCompound(i);
					if (!block.isEmpty()) {
						this.blocks[i] = BlockMeta.fromNBT(block);
					}
				}				
				this.heightmap = tag.getIntArray("heightmap");
			}
		}
	}
}
