package ru.bulldog.justmap.minimap.data;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.minimap.data.MapProcessor.Layer;
import ru.bulldog.justmap.util.Colors;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.Heightmap;
import net.minecraft.world.World;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.chunk.WorldChunk;
import net.minecraft.world.dimension.DimensionType;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class MapChunk {

	private MapProcessor.Layer layer;
	private WorldChunk worldChunk;
	
	private NativeImage image = new NativeImage(16, 16, false);
	
	private boolean updating;
	private boolean empty;
	
	public int[] heightmap;
	public long updated;
	
	private ChunkPos chunkPos;
	
	public MapChunk(WorldChunk chunk, MapProcessor.Layer layer) {
		this.chunkPos = chunk.getPos();
		this.layer = layer;
		this.worldChunk = chunk;
		this.heightmap = new int[16 * 16];
		
		Arrays.fill(heightmap, -1);
	}
	
	void setChunk(WorldChunk chunk) {
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
		return image;
	}
	
	public CompoundTag toNBT() {
		CompoundTag chunkTag = new CompoundTag();
		
		chunkTag.putString("layer", layer.name());
		chunkTag.putLong("updated", updated);
		chunkTag.putIntArray("heightmap", heightmap);
		
		CompoundTag chunkPos = new CompoundTag();		
		chunkPos.putInt("x", this.chunkPos.x);
		chunkPos.putInt("z", this.chunkPos.z);
		
		chunkTag.put("position", chunkPos);
		
		try {
			chunkTag.putByteArray("image", image.getBytes());
		} catch (IOException ex) {
			JustMap.LOGGER.logError("Can't store chunk image!");
			JustMap.LOGGER.catching(ex);
		}
		
		return chunkTag;
	}
	
	public static MapChunk fromNBT(CompoundTag data) {
		World world = MinecraftClient.getInstance().world;
		
		int chunkX = ((CompoundTag) data.get("position")).getInt("x");
		int chunkZ = ((CompoundTag) data.get("position")).getInt("z");
		
		Layer layer = Enum.valueOf(Layer.class, data.getString("layer"));
		MapChunk mapChunk = new MapChunk(world.getChunk(chunkX, chunkZ), layer);
		
		mapChunk.updated = data.getLong("updated");
		mapChunk.heightmap = data.getIntArray("heighmap");		
		if (!mapChunk.loadImage(data.getByteArray("image"))) {
			mapChunk.empty = true;
		}
		
		return mapChunk;
	}
	
	private boolean loadImage(byte[] data) {
		ByteBuffer buffer = ByteBuffer.wrap(data);
		try {
			NativeImage image = NativeImage.read(buffer);
			if (image.getWidth() != 16 || image.getHeight() != 16) return false;
			
			this.image = image;
			
			return true;
		} catch (IOException ex) {
			JustMap.LOGGER.logError("Can't load image!");
			JustMap.LOGGER.catching(ex);
			
			return false;
		}
	}
	
	public MapProcessor.Layer getLayer() {
		return layer;
	}
	
	public WorldChunk getWorldChunk() {
		return worldChunk;
	}
	
	public void update() {
		if (updating) { return; }
		
		long currentTime = System.currentTimeMillis();
		
		updating = true;		
		for (int x = 0; x < 16; x++) {
			for (int z = 0; z < 16; z++) {
				int color = Colors.BLACK;
				switch (layer) {
					case SURFACE:
						heightmap[x + z * 16] = worldChunk.sampleHeightmap(Heightmap.Type.WORLD_SURFACE, x, z);
						color = MapProcessor.surfaceColor(this, x, z);
					break;
					case CAVES:
						int ceiling = worldChunk.getWorld().getDimension().getType() == DimensionType.THE_NETHER ? 3 : 2;
						color = MapProcessor.cavesColor(this, x, z, ceiling);
					break;
				}
				
				image.setPixelRgba(x, z, color);
			}
		}
		
		empty = false;
		updated = currentTime;
		updating = false;
	}
}
