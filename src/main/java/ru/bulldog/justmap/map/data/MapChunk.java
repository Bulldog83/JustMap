package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.StateUtil;
import ru.bulldog.justmap.util.StorageUtil;
import ru.bulldog.justmap.util.TaskManager;

import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;

import java.util.Arrays;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class MapChunk {
	
	private final static TaskManager chunkUpdater = TaskManager.getManager("chunk-data");
	
	public final ChunkLevel EMPTY_LEVEL = new ChunkLevel(-1);
	
	private volatile ConcurrentMap<Layer, ChunkLevel[]> levels;
	
	private World world;
	private WorldChunk worldChunk;
	private ChunkPos chunkPos;
	private Layer.Type layer;
	private int dimension;
	private int level = 0;
	private boolean empty = true;
	private boolean saved = true;
	private boolean purged = false;
	
	public long updated = 0;
	public long requested = 0;
	
	public MapChunk(World world, ChunkPos pos, Layer.Type layer, int level) {
		this(world, pos, layer);
		this.level = level > 0 ? level : 0;
	}
	
	public MapChunk(World world, ChunkPos pos, Layer.Type layer) {
		this.world = world;
		this.worldChunk = world.getChunk(pos.x, pos.z);
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
		
		chunkUpdater.execute(this::restore);
	}
	
	public void resetChunk() {
		this.levels.clear();
		this.updated = 0;
	}
	
	private void initLayer() {
		initLayer(layer);
	}
	
	private void initLayer(Layer.Type layer) {
		int levels = worldChunk.getHeight() / layer.value.height;		
		this.levels.put(layer.value, new ChunkLevel[levels]);
	}
	
	private ChunkLevel getChunkLevel() {
		return getChunkLevel(layer, level);
	}
	
	private ChunkLevel getChunkLevel(Layer.Type layer, int level) {
		if (!levels.containsKey(layer.value)) {
			initLayer();
		}
		
		try {
			ChunkLevel chunkLevel;
			if (this.levels.get(layer.value)[level] == null) {
				chunkLevel = new ChunkLevel(level);
				this.levels.get(layer.value)[level] = chunkLevel;
			} else {
				chunkLevel = this.levels.get(layer.value)[level];
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
	
	public Layer.Type getLayer() {
		return layer;
	}
	
	public int getLevel() {
		return level;
	}
	
	public int[] getHeighmap() {
		return getChunkLevel().heightmap;
	}
	
	public void setLevel(Layer.Type layer, int level) {
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
				y = MapProcessor.getTopBlockY(this, x, y + 1, z, true);
				
				int index = x + (z << 4);
				ChunkLevel chunkLevel = getChunkLevel();
				if (y != -1) {
					chunkLevel.updateHeightmap(x, z, y);
				} else if (getHeighmap()[index] != -1) {
					chunkLevel.clear(x, z);					
					this.saved = false;
				}
			}
		}
	}
	
	public void update() {
		WorldChunk lifeChunk = world.getChunk(getX(), getZ());
		if (purged || lifeChunk.isEmpty()) return;
		
		if (worldChunk.isEmpty() && !lifeChunk.isEmpty()) {
			this.worldChunk = lifeChunk;
		}
		
		long currentTime = System.currentTimeMillis();
		if (currentTime - updated < ClientParams.chunkUpdateInterval) return;
		
		chunkUpdater.execute(this::updateData);
	}
	
	private void updateData() {
		MapChunk eastChunk = MapCache.get().getChunk(chunkPos.x + 1, chunkPos.z, true);
		MapChunk southChunk = MapCache.get().getChunk(chunkPos.x, chunkPos.z - 1, true);
		
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
				BlockState blockState = getBlockState(blockPos);
				BlockState worldState = worldChunk.getBlockState(blockPos);
				if(StateUtil.isAir(blockState) || !blockState.equals(worldState)) {
					int color = ColorUtil.blockColor(worldChunk, blockPos);
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						
						setBlockState(blockPos, worldState);
						
						chunkLevel.colormap[index] = color;
						chunkLevel.levelmap[index] = heightDiff;
						
						this.saved = false;
					}
				} else {				
					int color = chunkLevel.colormap[index];
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						if (chunkLevel.levelmap[index] != heightDiff) {
							chunkLevel.levelmap[index] = heightDiff;							
							this.saved = false;
						}
					}
				}
			}
		}
		
		this.empty = false;
		this.updated = currentTime;
	}
	
	public int getBlockColor(int x, int z) {
		ChunkLevel chunkLevel = getChunkLevel();
		
		int index = x + (z << 4);
		int color = chunkLevel.colormap[index];
		int heightDiff = chunkLevel.levelmap[index];
		
		if (color == -1) return ColorUtil.proccessColor(Colors.BLACK, 0);
		
		return ColorUtil.proccessColor(color, heightDiff);
	}
	
	public boolean saveNeeded() {
		return !this.isEmpty() && !this.saved;
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
	
	public void restore() {
		if (worldChunk.isEmpty()) return;
		
		CompoundTag chunkData = StorageUtil.getCache(chunkPos);
		if (chunkData.isEmpty()) return;
		
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
				}
			}			
		});
	}
}
