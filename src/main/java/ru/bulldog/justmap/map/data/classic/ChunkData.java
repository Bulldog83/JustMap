package ru.bulldog.justmap.map.data.classic;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.gen.ChunkRandom;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.network.ClientNetworkHandler;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.colors.ColorUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class ChunkData {
	
	public final static ChunkLevel EMPTY_LEVEL = new ChunkLevel(-1);
	
	private final static TaskManager chunkUpdater = TaskManager.getManager("chunk-updater", 2);
	private static final ClientNetworkHandler networkHandler = JustMapClient.getNetworkHandler();
	
	private final WorldData mapData;
	private final Map<Layer, ChunkLevel[]> levels = new ConcurrentHashMap<>();
	private final ChunkPos chunkPos;
	private final World world;
	private SoftReference<WorldChunk> worldChunk;
	private boolean outdated = false;
	private boolean slime = false;
	private boolean saved = true;
	private long refreshed = 0;
	
	public boolean saving = false;
	public long updated = 0;
	public long requested = 0;
	
	private final Object levelLock = new Object();

	public ChunkData(WorldData data, ChunkPos pos) {
		this.mapData = data;
		this.world = data.getWorld();
		this.chunkPos = pos;
		this.worldChunk = new SoftReference<>(world.getChunk(pos.x, pos.z));

		if (Dimension.isOverworld(world)) {
			ServerWorld serverWorld = DataUtil.getServerWorld();
			if (serverWorld != null) {
				this.slime = ChunkRandom.getSlimeRandom(chunkPos.x, chunkPos.z,
						serverWorld.getSeed(), 987234911L).nextInt(10) == 0;
			} else if (networkHandler.canRequestData()) {
				networkHandler.requestChunkHasSlime(chunkPos, result -> {
					this.slime = result;
				});
			}
		}
		if (Dimension.isNether(world)) {
			initLayer(Layer.NETHER);
		} else {
			initLayer(Layer.SURFACE);
			initLayer(Layer.CAVES);
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
	
	private void initLayer(Layer layer) {
		int levels = this.world.getDimension().getHeight() / layer.height;
		this.levels.put(layer, new ChunkLevel[levels]);
	}

	public ChunkLevel getChunkLevel(Layer layer, int level) {
		synchronized (levelLock) {
			if (!levels.containsKey(layer)) {
				initLayer(layer);
			}
			
			ChunkLevel chunkLevel;
			try {
				chunkLevel = this.levels.get(layer)[level];
				if (chunkLevel == null) {
					chunkLevel = new ChunkLevel(level);
					this.levels.get(layer)[level] = chunkLevel;
				}
			} catch (ArrayIndexOutOfBoundsException ex) {
				if (level < 0) {
					chunkLevel = EMPTY_LEVEL;
				} else {
					ChunkLevel[] levels = this.levels.get(layer);
					this.levels.replace(layer, Arrays.copyOf(levels, levels.length + 1));
					chunkLevel = this.getChunkLevel(layer, level);
				}
			}

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
	
	public WorldChunk getWorldChunk() {
		return this.worldChunk.get();
	}
	
	public BlockState getBlockState(Layer layer, int level, BlockPos pos) {
		return this.getChunkLevel(layer, level).getBlockState(pos.getX() & 15, pos.getZ() & 15);
	}
	
	public void setBlockState(Layer layer, int level, BlockPos pos, BlockState blockState) {
		this.getChunkLevel(layer, level).setBlockState(pos.getX() & 15, pos.getZ() & 15, blockState);
	}
	
	private boolean checkUpdating(Layer layer, int level) {
		return this.getChunkLevel(layer, level).updating;
	}

	public void updateWorldChunk(WorldChunk lifeChunk) {
		if (lifeChunk != null && !lifeChunk.isEmpty()) {
			this.worldChunk = new SoftReference<>(lifeChunk);
		}
	}
	
	public WorldChunk updateWorldChunk() {
		WorldChunk currentChunk = this.worldChunk.get();
		if(currentChunk == null || currentChunk.isEmpty()) {
			WorldChunk lifeChunk = this.world.getChunk(getX(), getZ());
			if (lifeChunk == null || lifeChunk.isEmpty()) {
				return this.mapData.getEmptyChunk();
			}
			this.updateWorldChunk(lifeChunk);
			return lifeChunk;
		}
		return currentChunk;
	}
	
	public boolean updateFullChunk(Layer layer, int level, boolean forceUpdate) {
		return this.updateChunkArea(layer, level, forceUpdate, 0, 0, 16, 16);
	}

	public boolean updateChunkArea(Layer layer, int level, boolean forceUpdate, int x, int z, int width, int height) {
		if (!JustMapClient.canMapping()) return false;
		if (checkUpdating(layer, level)) return false;
		if (!outdated && forceUpdate) {
			this.outdated = forceUpdate;
		}
		long currentTime = System.currentTimeMillis();
		if (!outdated && currentTime - updated < ClientSettings.chunkUpdateInterval) return false;
		
		WorldChunk worldChunk = this.updateWorldChunk();
		chunkUpdater.execute(() -> {
			if (worldChunk.isEmpty() || !this.isChunkLoaded()) return;
			this.updateArea(worldChunk, layer, level, x, z, width, height);
			if (saveNeeded()) {
				BlockPos.Mutable chunkBlockPos = this.chunkPos.getStartPos().mutableCopy();
				chunkBlockPos.setY(level * layer.height);
				RegionData region = this.mapData.getRegion(chunkBlockPos);
				if (region.getLayer().equals(layer) && region.getLevel() == level) {
					region.writeChunkData(this);
				}
			}
		});
		
		return true;
	}
	
	private void updateHeighmap(WorldChunk worldChunk, Layer layer, int level, boolean skipWater, int x, int z, int width, int height) {
		if (worldChunk.isEmpty()) return;
		for (int sx = x; sx < width; sx++) {
			for (int sz = z; sz < height; sz++) {
				int y = MapProcessor.getTopBlockY(worldChunk, layer, level, sx, sz, skipWater);
				ChunkLevel chunkLevel = this.getChunkLevel(layer, level);
				if (y != -1) {
					chunkLevel.updateHeightmap(sx, sz, y);
				} else if (chunkLevel.sampleHeightmap(sx, sz) != -1) {
					chunkLevel.clear(sx, sz);					
					this.saved = false;
				}
			}
		}
	}
	
	private void updateArea(WorldChunk worldChunk, Layer layer, int level, int x, int z, int width, int height) {
		ChunkLevel chunkLevel = this.getChunkLevel(layer, level);
		chunkLevel.updating = true;

		boolean waterTint = ClientSettings.alternateColorRender && ClientSettings.waterTint;
		boolean skipWater = !(ClientSettings.hideWater || waterTint);
		long currentTime = System.currentTimeMillis();
		if (currentTime - chunkLevel.updated > ClientSettings.chunkLevelUpdateInterval) {
			this.updateHeighmap(worldChunk, layer, level, skipWater, x, z, width, height);
		}
		
		for (int sx = x; sx < width; sx++) {
			for (int sz = 0; sz < height; sz++) {
				this.updateBlock(worldChunk, chunkLevel, layer, level, sx, sz, skipWater);
			}
		}
		
		this.updated = currentTime;
		this.refreshed = currentTime;
		this.outdated = false;

		chunkLevel.updating = false;
	}
	
	private void updateBlock(WorldChunk worldChunk, ChunkLevel chunkLevel, Layer layer, int level, int x, int z, boolean skipWater) {
		int index = x + (z << 4);
		int posX = x + (chunkPos.x << 4);
		int posZ = z + (chunkPos.z << 4);
		int posY = chunkLevel.sampleHeightmap(x, z);
		
		if (posY < 0) return;
		
		long currentTime = System.currentTimeMillis();
		
		BlockPos blockPos = new BlockPos(posX, posY, posZ);
		BlockState blockState = this.getBlockState(layer, level, blockPos);
		BlockState worldState = worldChunk.getBlockState(blockPos);
		if(outdated || !blockState.equals(worldState) || currentTime - refreshed > 60000) {
			int color = ColorUtil.blockColor(worldChunk, blockPos);
			if (color == -1) return;

			chunkLevel.setBlockState(x, z, worldState);

			int height = layer.height;
			int bottom = 0, baseHeight = 0;
			if (layer == Layer.NETHER) {
				bottom = level * height;
				baseHeight = 128;
			} else if (layer == Layer.SURFACE) {
				bottom = this.world.getSeaLevel();
				baseHeight = 256;
			} else {
				bottom = level * height;
				baseHeight = 32;
			}

			float topoLevel = ((float) (posY - bottom) / baseHeight);
			int heightDiff = MapProcessor.heightDifference(this, layer, level, x, posY, z, skipWater);
			chunkLevel.topomap[index] = (int) (topoLevel * 100);
			chunkLevel.levelmap[index] = heightDiff;
			chunkLevel.colormap[index] = color;

			this.saved = false;
		} else if (chunkLevel.colormap[index] != -1) {
			int heightDiff = MapProcessor.heightDifference(this, layer, level, x, posY, z, skipWater);
			if (chunkLevel.levelmap[index] != heightDiff) {
				chunkLevel.levelmap[index] = heightDiff;
				this.saved = false;
			}
		}
	}
	
	public int[] getColorData(Layer layer, int level) {
		ChunkLevel chunkLevel = this.getChunkLevel(layer, level);
		int[] colordata = new int[256];
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int index = x + (z << 4);
				colordata[index] = this.getProcessedBlockColor(chunkLevel, index);
			}
		}
		return colordata;
	}
	
	private int getProcessedBlockColor(ChunkLevel chunkLevel, int index) {
		int color = chunkLevel.colormap[index];
		if (color != -1) {
			int heightDiff = chunkLevel.levelmap[index];
			float topoLevel = chunkLevel.topomap[index] / 100F;
			color = ColorUtil.proccessColor(color, heightDiff, topoLevel);
			if (ClientSettings.showTopography) {
				return MathUtil.isOdd(chunkLevel.sampleHeightmap(index)) ?
						ColorUtil.colorBrigtness(color, -0.6F) : color;
			}
			return color;
		}
		return Colors.BLACK;
	}
	
	public boolean saveNeeded() {
		return !this.saved;
	}
	
	public void setSaved() {
		this.saved = true;
	}
	
	public boolean isChunkLoaded() {
		return this.world.isChunkLoaded(chunkPos.x, chunkPos.z);
	}
	
	public boolean hasSlime() {
		return this.slime;
	}
}
