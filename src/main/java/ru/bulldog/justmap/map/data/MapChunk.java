package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.data.Layers.Layer;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.StateUtil;
import ru.bulldog.justmap.util.StorageUtil;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
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
	
	public final static ChunkLevel EMPTY_LEVEL = new ChunkLevel(-1);
	
	private volatile Map<Layers, ChunkLevel[]> levels;
	
	private WorldChunk worldChunk;
	private ChunkPos chunkPos;
	private Layers layer;
	private int dimension;
	private int level = 0;
	private boolean empty = true;
	private boolean saved = true;
	
	public long updated = 0;
	
	public MapChunk(World world, ChunkPos pos, Layers layer, int level) {
		this(world, pos, layer);
		this.level = level > 0 ? level : 0;
	}
	
	public MapChunk(World world, ChunkPos pos, Layers layer) {
		this.worldChunk = world.getChunk(pos.x, pos.z);
		this.dimension = world.getDimension().getType().getRawId();
		this.chunkPos = pos;
		this.layer = layer;
		this.levels = new HashMap<>();
		
		this.init();
	}
	
	private void init() {
		if (dimension == -1) {
			initLayer(Layer.NETHER.value);
		} else {
			initLayer(Layer.SURFACE.value);
			initLayer(Layer.CAVES.value);
		}
		
		loadFromNBT();
	}
	
	public void resetChunk() {
		this.levels.clear();
		this.updated = 0;
	}
	
	private void initLayer() {
		initLayer(layer);
	}
	
	private void initLayer(Layers layer) {
		int levels = worldChunk.getHeight() / layer.height;		
		this.levels.put(layer, new ChunkLevel[levels]);
	}
	
	private synchronized ChunkLevel getChunkLevel() {
		return getChunkLevel(layer, level);
	}
	
	private synchronized ChunkLevel getChunkLevel(Layers layer, int level) {
		if (!levels.containsKey(layer)) {
			initLayer();
		}
		
		try {
			ChunkLevel chunkLevel;
			if (this.levels.get(layer)[level] == null) {
				chunkLevel = new ChunkLevel(level);
				this.levels.get(layer)[level] = chunkLevel;
			} else {
				chunkLevel = this.levels.get(layer)[level];
			}
			
			return chunkLevel;
		} catch (ArrayIndexOutOfBoundsException ex) {
			return EMPTY_LEVEL;
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
		return getChunkLevel().getImage(chunkPos);
	}
	
	public Layers getLayer() {
		return layer;
	}
	
	public int getLevel() {
		return level;
	}
	
	public synchronized int[] getHeighmap() {
		return getChunkLevel().heightmap;
	}
	
	public void setLevel(Layers layer, int level) {
		if (this.layer == layer &&
			this.level == level) return;
		
		this.level = level;		
		this.layer = layer;
	}
	
	public WorldChunk getWorldChunk() {
		return worldChunk;
	}
	
	public BlockState getBlockState(BlockPos pos) {
		return getChunkLevel().getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}
	
	public BlockState setBlockState(BlockPos pos, BlockState blockState) {
		return getChunkLevel().setBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, blockState);
	}
	
	public void updateHeighmap() {
		if (worldChunk.isEmpty()) return;
		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int y = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
				y = MapProcessor.getTopBlockY(this, x, y, z, true);
				if (y != -1) {
					getHeighmap()[x + (z << 4)] = y;
				} else if (getHeighmap()[x + (z << 4)] != -1) {
					ChunkLevel chunkLevel = getChunkLevel();
					chunkLevel.getImage(chunkPos).setPixelRgba(x, z, Colors.BLACK);
					chunkLevel.clear(x, z);
					
					updateRegionData();
					
					this.saved = false;
				}
			}
		}
	}
	
	public void update() {
		if (worldChunk.isEmpty()) return;
		
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
				
				int posX = x + (chunkPos.x << 4);
				int posZ = z + (chunkPos.z << 4);
				int posY = getHeighmap()[index];
				
				BlockPos blockPos = new BlockPos(posX, posY, posZ);
				BlockState blockState = getBlockState(blockPos);
				BlockState worldState = worldChunk.getBlockState(blockPos);
				if(StateUtil.isAir(blockState) || !blockState.equals(worldState)) {
					int color = ColorUtil.blockColor(worldChunk, blockPos);
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						
						setBlockState(blockPos, worldState);
						
						chunkLevel.colormap[index] = color;
						chunkLevel.levelmap[index] = heightDiff;
						
						color = ColorUtil.proccessColor(color, heightDiff);
						chunkLevel.getImage(chunkPos).setPixelRgba(x, z, color);
					
						updateRegionData();
						
						this.saved = false;
					}
				} else {				
					int color = chunkLevel.colormap[index];					
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						if (chunkLevel.levelmap[index] != heightDiff) {
							chunkLevel.levelmap[index] = heightDiff;
						
							color = ColorUtil.proccessColor(color, heightDiff);
							chunkLevel.getImage(chunkPos).setPixelRgba(x, z, color);
						
							updateRegionData();
							
							this.saved = false;
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
	
	public boolean saveNeeded() {
		return !this.isEmpty() && !this.saved;
	}
	
	public void saveToNBT(CompoundTag data) {
		levels.forEach((layer, levels) -> {
			ListTag levelsTag = new ListTag();
			
			CompoundTag level;
			for(int i = 0; i < levels.length; i++) {
				int lvl = i;
				ChunkLevel chunkLevel = Arrays.stream(levels).filter((levelx) -> {
					return levelx != null && levelx.level == lvl;
				}).findFirst().orElse(EMPTY_LEVEL);
		         
				if (chunkLevel != EMPTY_LEVEL) {
		            level = new CompoundTag();
		            
		            level.putInt("Level", lvl);
		            chunkLevel.getContainer().write(level, "Palette", "BlockStates");
		            chunkLevel.store(level);

		            levelsTag.add(level);
				}
			}
			
			data.put(layer.name, levelsTag);
		});
		
		this.saved = true;
	}
	
	public void loadFromNBT() {
		CompoundTag chunkData = StorageUtil.getCache(chunkPos);
		if (chunkData.isEmpty()) return;
		
		levels.forEach((layer, levels) -> {
			ListTag listTag = chunkData.getList(layer.name, 10);
			for(int i = 0; i < listTag.size(); ++i) {
				CompoundTag level = listTag.getCompound(i);
				int lvl = level.getInt("Level");
				if (level.contains("Palette", 9) && level.contains("BlockStates", 12)) {
					ChunkLevel chunkLevel = new ChunkLevel(lvl);
					chunkLevel.getContainer().read(level.getList("Palette", 10), level.getLongArray("BlockStates"));
					chunkLevel.load(level);
					
					levels[lvl] = chunkLevel;
				}
			}			
		});
	}
}
