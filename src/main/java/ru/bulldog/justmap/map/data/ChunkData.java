package ru.bulldog.justmap.map.data;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.tasks.TaskManager;

import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.chunk.ChunkManager;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;
import net.minecraft.world.gen.ChunkRandom;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class ChunkData {
	
	public final ChunkLevel EMPTY_LEVEL = new ChunkLevel(-1);
	
	private final static TaskManager chunkUpdater = TaskManager.getManager("chunk-updater");
	
	private final Map<Layer, ChunkLevel[]> levels = new ConcurrentHashMap<>();
	private final Identifier dimension;
	private final ChunkPos chunkPos;
	private World world;
	private WeakReference<WorldChunk> worldChunk;
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
	
	public static void updadeChunk(ChunkData mapChunk) {
		if (mapChunk.updating || mapChunk.purged) return;
		chunkUpdater.execute("Updating chunk " + mapChunk.chunkPos, mapChunk::updateChunkData);
	}
	
	public ChunkData(World world, WorldChunk lifeChunk, Layer.Type layer, int level) {
		this(world, lifeChunk.getPos(), layer, level);
		this.updateWorldChunk(lifeChunk);
	}
	
	public ChunkData(World world, ChunkPos pos, Layer.Type layer, int level) {
		this(world, pos, layer);
		this.level = level > 0 ? level : 0;
	}
	
	public ChunkData(World world, ChunkPos pos, Layer.Type layer) {
		MinecraftClient minecraft = JustMapClient.MINECRAFT;
		RegistryKey<DimensionType> dimType = minecraft.world.getDimensionRegistryKey();
		
		this.world = world;
		this.dimension = dimType.getValue();
		this.chunkPos = pos;
		this.layer = layer;
		this.worldChunk = new WeakReference<>(minecraft.world.getChunk(pos.x, pos.z));

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
	
	public ChunkData resetChunk() {
		synchronized (levelLock) {
			this.levels.clear();
		}
		this.outdated = true;
		this.updated = 0;
		
		return this;
	}
	
	private void initLayer(Layer.Type layer) {
		int levels = this.world.getDimensionHeight() / layer.value.height;		
		this.levels.put(layer.value, new ChunkLevel[levels]);
	}
	
	private ChunkLevel getChunkLevel() {
		return this.getChunkLevel(layer, level);
	}
	
	private ChunkLevel getChunkLevel(Layer.Type layer, int level) {
		if (!levels.containsKey(layer.value)) {
			initLayer(layer);
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
	
	public ChunkData setLevel(Layer.Type layer, int level) {
		if (this.layer == layer &&
			this.level == level) return this;
		
		this.level = level;
		this.layer = layer;
		
		return this;
	}
	
	public WorldChunk getWorldChunk() {
		return this.worldChunk.get();
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
	
	public void updateWorldChunk(WorldChunk lifeChunk) {
		if (lifeChunk != null && !lifeChunk.isEmpty()) {
			this.worldChunk = new WeakReference<>(lifeChunk);
		}
	}
	
	private boolean updateWorldChunk() {
		WorldChunk currentChunk = this.worldChunk.get();
		if(currentChunk == null || currentChunk.isEmpty()) {
			ChunkManager chunkManager = this.world.getChunkManager();
			if (chunkManager == null) return false;
			WorldChunk lifeChunk = chunkManager.getWorldChunk(getX(), getZ());
			if (lifeChunk == null || lifeChunk.isEmpty()) {
				return ChunkDataManager.callSavedChunk(world, this);
			}
			this.updateWorldChunk(lifeChunk);
		}
		return true;
	}
	
	public ChunkData updateHeighmap() {
		if (!this.updateWorldChunk()) return this;
		
		WorldChunk worldChunk = this.worldChunk.get();
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
		WorldChunk worldChunk = this.worldChunk.get();
		
		DimensionData data = DimensionManager.getData(world, dimension);
		ChunkData eastChunk = data.getCurrentChunk(chunkPos.x + 1, chunkPos.z);
		ChunkData southChunk = data.getCurrentChunk(chunkPos.x, chunkPos.z - 1);
		
		long currentTime = System.currentTimeMillis();
		
		ChunkLevel chunkLevel = this.getChunkLevel();
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
				int posY = this.getHeighmap()[index];
				
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
}
