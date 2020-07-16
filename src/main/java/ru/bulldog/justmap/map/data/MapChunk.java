package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.tasks.TaskManager;

import net.minecraft.world.ChunkSerializer;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.world.ServerChunkManager;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.ReadOnlyChunk;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkRandom;
import net.minecraft.world.storage.VersionedChunkStorage;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class MapChunk {
	
	public final ChunkLevel EMPTY_LEVEL = new ChunkLevel(-1);
	
	private final static TaskManager chunkUpdater = TaskManager.getManager("chunk-data");
	
	private final Map<Layer, ChunkLevel[]> levels = new ConcurrentHashMap<>();
	private final Identifier dimension;
	private final ChunkPos chunkPos;
	private World world;
	private WorldChunk worldChunk;
	private Layer.Type layer;
	private int level = 0;
	private boolean outdated = false;
	private boolean updating = false;
	private boolean purged = false;
	private boolean slime = false;
	private boolean saved = true;
	private long refreshed = 0;
	
	public boolean saving = false;
	public long updated = 0;
	public long requested = 0;
	
	private Object levelLock = new Object();
	
	public MapChunk(World world, WorldChunk lifeChunk, Layer.Type layer, int level) {
		this(world, lifeChunk.getPos(), layer, level);
		this.worldChunk = lifeChunk;
	}
	
	public MapChunk(World world, ChunkPos pos, Layer.Type layer, int level) {
		this(world, pos, layer);
		this.level = level > 0 ? level : 0;
	}
	
	public MapChunk(World world, ChunkPos pos, Layer.Type layer) {
		MinecraftClient client = MinecraftClient.getInstance();
		RegistryKey<DimensionType> dimType = client.world.getDimensionRegistryKey();
		
		this.world = world;
		this.worldChunk = client.world.getChunk(pos.x, pos.z);
		this.dimension = dimType.getValue();
		this.chunkPos = pos;
		this.layer = layer;
		
		if (Dimension.isOverworld(dimType) && (world instanceof ServerWorld)) {
			this.slime = ChunkRandom.getSlimeRandom(chunkPos.x, chunkPos.z,
					((ServerWorld) world).getSeed(), 987234911L).nextInt(10) == 0;
		}
		
		if (dimension.equals(DimensionType.THE_NETHER_REGISTRY_KEY.getValue())) {
			initLayer(Layer.Type.NETHER);
		} else {
			initLayer(Layer.Type.SURFACE);
			initLayer(Layer.Type.CAVES);
		}
	}
	
	public MapChunk resetChunk() {
		synchronized (levelLock) {
			this.levels.clear();
		}
		this.outdated = true;
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
		return this.worldChunk;
	}
	
	public BlockState getBlockState(BlockPos pos) {
		return getChunkLevel().getBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15);
	}
	
	public BlockState setBlockState(BlockPos pos, BlockState blockState) {
		return getChunkLevel().setBlockState(pos.getX() & 15, pos.getY() & 15, pos.getZ() & 15, blockState);
	}
	
	public boolean update(boolean forceUpdate) {
		if (updating || purged) return false;
		
		if (!outdated && forceUpdate) {
			this.outdated = forceUpdate;
		}
		
		long currentTime = System.currentTimeMillis();
		if (!outdated && currentTime - updated < ClientParams.chunkUpdateInterval) return false;
		
		CompletableFuture<Boolean> updated = chunkUpdater.run("Updating Chunk: " + chunkPos, future -> {
			return () -> future.complete(this.updateChunkData());
		});
		
		return updated.join();
	}
	
	private boolean callSavedChunk() {
		if (!(world instanceof ServerWorld)) return false;
		
		ServerWorld world = (ServerWorld) this.world;
		ServerChunkManager manager = world.getChunkManager();
		VersionedChunkStorage storage = manager.threadedAnvilChunkStorage;
		try {		
			CompoundTag chunkTag = storage.getNbt(chunkPos);
			if (chunkTag == null) return false;
			
			Chunk chunk = ChunkSerializer.deserialize(
					world, world.getStructureManager(), world.getPointOfInterestStorage(), chunkPos, chunkTag);
			if (chunk instanceof ReadOnlyChunk) {
				WorldChunk worldChunk = ((ReadOnlyChunk) chunk).getWrappedChunk();
				worldChunk.setLoadedToWorld(true);
				this.updateWorldChunk(worldChunk);
				
				return true;
			}
			return false;
		} catch (IOException ex) {
			JustMap.LOGGER.catching(ex);
			return false;
		}
	}
	
	public void updateWorldChunk(WorldChunk lifeChunk) {
		if (!this.worldChunk.isEmpty()) return;
		if (lifeChunk != null && !lifeChunk.isEmpty()) {
			this.worldChunk = lifeChunk;
		}
	}
	
	private boolean updateWorldChunk() {
		if(worldChunk.isEmpty()) {
			ChunkManager chunkManager = this.world.getChunkManager();
			if (chunkManager == null) return false;
			WorldChunk lifeChunk = chunkManager.getWorldChunk(getX(), getZ());
			if (lifeChunk == null || lifeChunk.isEmpty()) {
				return this.callSavedChunk();
			}
			this.worldChunk = lifeChunk;
		}
		return true;
	}
	
	public MapChunk updateHeighmap() {
		if (!this.updateWorldChunk()) return this;
		
		boolean waterTint = ClientParams.alternateColorRender && ClientParams.waterTint;
		boolean skipWater = !(ClientParams.hideWater || waterTint);
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int yws = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
				int ymb = worldChunk.sampleHeightmap(Heightmap.Type.MOTION_BLOCKING, x, z);
				int y = Math.max(yws, ymb);
				
				if (y <= 0) continue; 
				
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
	
	private boolean updateChunkData() {
		this.updating = true;
		
		if (!this.updateWorldChunk()) {
			this.updating = false;
			return false;
		}
		
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
				
				if (posY < 0) continue;
				
				BlockPos blockPos = new BlockPos(posX, posY, posZ);
				BlockState blockState = this.getBlockState(blockPos);
				BlockState worldState = worldChunk.getBlockState(blockPos);
				if(outdated || !blockState.equals(worldState) || currentTime - refreshed > 60000) {
					int color = ColorUtil.blockColor(worldChunk, blockPos);
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						
						this.setBlockState(blockPos, worldState);
						
						int height = layer.value.height;
						int bottom = 0, baseHeight = 0;
						if (layer == Layer.Type.NETHER) {
							bottom = level * height;
							baseHeight = 128;
						} else if (layer == Layer.Type.SURFACE) {
							bottom = this.world.getSeaLevel();
							baseHeight = 256;
						} else {
							bottom = level * height;
							baseHeight = 32;
						}
						
						float topoLevel = ((float) (posY - bottom) / baseHeight);						
						
						chunkLevel.topomap[index] = (int) (topoLevel * 100);
						chunkLevel.colormap[index] = color;
						chunkLevel.levelmap[index] = heightDiff;
						chunkLevel.colordata[index] = ColorUtil.proccessColor(color, heightDiff, topoLevel);
						
						this.saved = false;
					}
				} else {				
					int color = chunkLevel.colormap[index];
					if (color != -1) {
						int heightDiff = MapProcessor.heightDifference(this, eastChunk, southChunk, x, posY, z);
						if (chunkLevel.levelmap[index] != heightDiff) {
							float topoLevel = chunkLevel.topomap[index] / 100F;
							chunkLevel.levelmap[index] = heightDiff;
							chunkLevel.colordata[index] = ColorUtil.proccessColor(color, heightDiff, topoLevel);
							this.saved = false;
						}
					}
				}
			}
		}
		
		this.updated = currentTime;
		this.refreshed = currentTime;
		this.outdated = false;
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
	
	public boolean isChunkLoaded() {
		return this.world.getChunkManager().isChunkLoaded(getX(), getZ());
	}
	
	public boolean hasSlime() {
		return this.slime;
	}
	
	public void updateWorld(World world) {
		if (!this.world.equals(world)) {
			this.resetChunk();
			this.world = world;
		}
	}
	
	public World getWorld() {
		return this.world;
	}
}
