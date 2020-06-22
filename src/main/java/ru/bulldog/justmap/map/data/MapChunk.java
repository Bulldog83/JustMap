package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.StorageUtil;
import ru.bulldog.justmap.util.TaskManager;

import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MapChunk {
	
	public final ChunkLevel EMPTY_LEVEL = new ChunkLevel(-1);
	
	private final static TaskManager chunkUpdater = TaskManager.getManager("chunk-data");
	private volatile Map<Layer, ChunkLevel[]> levels;
	private static MinecraftClient client = MinecraftClient.getInstance();
	
	private World world;
	private WorldChunk worldChunk;
	private ChunkPos chunkPos;
	private Layer.Type layer;
	private int dimension;
	private int level = 0;
	private boolean outdated = false;
	private boolean updating = false;
	private boolean saved = true;
	private boolean purged = false;
	
	public boolean saving = false;
	public long updated = 0;
	public long requested = 0;
	
	private Object levelLock = new Object();
	
	public MapChunk(World world, ChunkPos pos, Layer.Type layer, int level) {
		this(world, pos, layer);
		this.level = level > 0 ? level : 0;
	}
	
	public MapChunk(World world, ChunkPos pos, Layer.Type layer) {
		this.world = world;
		this.worldChunk = client.world.getChunk(pos.x, pos.z);
		this.dimension = world.getDimension().getType().getRawId();
		this.chunkPos = pos;
		this.layer = layer;
		this.levels = new ConcurrentHashMap<>();
		
		this.init();
	}
	
	private void init() {
		if (dimension == -1) {
			initLayer(Layer.Type.NETHER);
		} else {
			initLayer(Layer.Type.SURFACE);
			initLayer(Layer.Type.CAVES);
		}
		
		this.restore();
	}
	
	public MapChunk resetChunk() {
		this.levels.clear();
		this.updated = 0;
		
		return this;
	}
	
	private void initLayer() {
		this.initLayer(layer);
	}
	
	private void initLayer(Layer.Type layer) {
		int levels = worldChunk.getHeight() / layer.value.height;		
		this.levels.put(layer.value, new ChunkLevel[levels]);
	}
	
	private ChunkLevel getChunkLevel() {
		return this.getChunkLevel(layer, level);
	}
	
	private ChunkLevel getChunkLevel(Layer.Type layer, int level) {
		if (!levels.containsKey(layer.value)) {
			initLayer();
		}
		
		ChunkLevel chunkLevel;
		try {
			if (this.levels.get(layer.value)[level] == null) {
				chunkLevel = new ChunkLevel(level);
				this.levels.get(layer.value)[level] = chunkLevel;
			} else {
				chunkLevel = this.levels.get(layer.value)[level];
			}			
			
		} catch (ArrayIndexOutOfBoundsException ex) {
			chunkLevel = EMPTY_LEVEL;
		}
		
		synchronized (levelLock) {
			return chunkLevel;
		}
	}
	
	public MapChunk setPos(ChunkPos chunkPos) {
		this.chunkPos = chunkPos;
		return this;
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
	
	public Layer.Type getLayer() {
		return layer;
	}
	
	public int currentLevel() {
		return level;
	}
	
	public int[] getHeighmap() {
		return getChunkLevel().heightmap;
	}
	
	public MapChunk setLevel(Layer.Type layer, int level) {
		if (this.layer == layer &&
			this.level == level) return this;
		
		this.level = level;
		this.layer = layer;
		
		return this;
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
	
	public MapChunk updateHeighmap() {
		if (!this.updateWorldChunk()) return this;
		
		boolean waterTint = ClientParams.alternateColorRender && ClientParams.waterTint;
		boolean skipWater = !(ClientParams.hideWater || waterTint);
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int y = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
				y = MapProcessor.getTopBlockY(this, x, y + 1, z, skipWater);
				
				int index = x + (z << 4);
				ChunkLevel chunkLevel = this.getChunkLevel();
				if (y != -1) {
					chunkLevel.updateHeightmap(x, z, y);
				} else if (getHeighmap()[index] != -1) {
					chunkLevel.clear(x, z);					
					this.saved = false;
				}
			}
		}
		
		return this;
	}
	
	public boolean update(boolean forceUpdate) {
		if (updating) return false;		
		if (!outdated && forceUpdate) {
			this.outdated = forceUpdate;
		}
		
		long currentTime = System.currentTimeMillis();
		if (!outdated && currentTime - updated < ClientParams.chunkUpdateInterval) return false;
		if (purged || !this.updateWorldChunk()) return false;
		
		CompletableFuture<Boolean> updated = chunkUpdater.run((future) -> {
			return () -> future.complete(this.updateChunkData());
		});
		
		return updated.join();
	}
	
	private boolean updateWorldChunk() {
		if(worldChunk.isEmpty()) {
			WorldChunk lifeChunk = world.getChunkManager().getWorldChunk(getX(), getZ(), false);
			if (lifeChunk == null || lifeChunk.isEmpty()) return false;
			this.worldChunk = lifeChunk;
		}
		return true;
	}
	
	private boolean updateChunkData() {
		this.updating = true;
		
		MapCache mapData = MapCache.get();
		MapChunk eastChunk = mapData.getCurrentChunk(chunkPos.x + 1, chunkPos.z);
		MapChunk southChunk = mapData.getCurrentChunk(chunkPos.x, chunkPos.z - 1);
		
		long currentTime = System.currentTimeMillis();
		
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
				
				if (posY == -1) continue;
				
				BlockPos blockPos = new BlockPos(posX, posY, posZ);
				BlockState blockState = this.getBlockState(blockPos);
				BlockState worldState = worldChunk.getBlockState(blockPos);
				if(outdated || !blockState.equals(worldState)) {
					int color = ColorUtil.blockColor(worldChunk, blockPos);
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						
						this.setBlockState(blockPos, worldState);
						
						chunkLevel.colormap[index] = color;
						chunkLevel.levelmap[index] = heightDiff;
						chunkLevel.colordata[index] = ColorUtil.proccessColor(color, heightDiff);
						
						this.saved = false;
					}
				} else {				
					int color = chunkLevel.colormap[index];
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						if (chunkLevel.levelmap[index] != heightDiff) {
							chunkLevel.levelmap[index] = heightDiff;
							chunkLevel.colordata[index] = ColorUtil.proccessColor(color, heightDiff);
							this.saved = false;
						}
					}
				}
			}
		}
		
		this.outdated = false;
		this.updated = currentTime;
		this.updating = false;
		
		return this.saveNeeded();
	}
	
	public int[] getColorData() {
		ChunkLevel chunkLevel = this.getChunkLevel();
		return chunkLevel.colordata.clone();
	}
	
	public int getBlockColor(int x, int z) {
		int index = x + (z << 4);
		ChunkLevel chunkLevel = getChunkLevel();
		return chunkLevel.colordata[index];
	}
	
	public boolean saveNeeded() {
		return !this.saved;
	}
	
	public void store(CompoundTag data) {
		this.levels.forEach((layer, levels) -> {
			ListTag levelsTag = new ListTag();
			
			CompoundTag level;
			for(int i = 0; i < levels.length; i++) {
				int lvl = i;
				ChunkLevel chunkLevel = Arrays.stream(levels).filter((levelx) -> {
					return levelx != null && levelx.level == lvl;
				}).findFirst().orElse(EMPTY_LEVEL);
		         
				if (chunkLevel.isEmpty()) continue;
	            
				level = new CompoundTag();
	            
	            level.putInt("Level", lvl);
	            chunkLevel.container().write(level, "Palette", "BlockStates");
	            chunkLevel.store(level);

	            levelsTag.add(level);
			}
			
			if (!levelsTag.isEmpty()) data.put(layer.name, levelsTag);
		});
		
		this.saved = true;
	}
	
	private void restore() {
		CompoundTag chunkData = StorageUtil.getCache(chunkPos);
		if (chunkData.isEmpty()) return;
		
		final int dataVer = chunkData.contains("version") ? chunkData.getInt("version") : -1;
		this.levels.forEach((layer, levels) -> {
			ListTag listTag = chunkData.getList(layer.name, 10);
			for(int i = 0; i < listTag.size(); ++i) {
				CompoundTag level = listTag.getCompound(i);
				int lvl = level.getInt("Level");
				if (level.contains("Palette", 9) && level.contains("BlockStates", 12)) {
					ChunkLevel chunkLevel = this.getChunkLevel(layer.type, lvl);
					if (chunkLevel.isEmpty()) continue;
					
					chunkLevel.container().read(level.getList("Palette", 10), level.getLongArray("BlockStates"));
					chunkLevel.load(level);
					
					if (dataVer == -1) {
						for (int j = 0; j < chunkLevel.colormap.length; j++) {
							int color = chunkLevel.colormap[j];
							if (color != -1) {
								chunkLevel.colormap[j] = ColorUtil.ABGRtoARGB(color);
							}
						}
						this.saved = false;
					}
				}
			}			
		});
	}
}
