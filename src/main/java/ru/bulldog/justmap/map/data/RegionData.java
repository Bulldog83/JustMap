package ru.bulldog.justmap.map.data;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.MapTexture;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.math.Plane;
import ru.bulldog.justmap.util.math.Point;
import ru.bulldog.justmap.util.storage.StorageUtil;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class RegionData {
	
	private static TaskManager worker = TaskManager.getManager("region-updater");
	
	private final DimensionData mapData;
	private final RegionPos regPos;
	private final Map<Layer, MapTexture> images = new ConcurrentHashMap<>();
	private File cacheDir;
	private MapTexture image;
	private MapTexture texture;
	private MapTexture overlay;
	private World world;
	private Layer layer;
	private ChunkPos center;
	private Plane updateArea;
	private int level;
	private boolean needUpdate = false;
	private boolean renewOverlay = false;
	private boolean overlayNeeded = false;
	private boolean updating = false;
	private boolean hideWater = false;
	private boolean waterTint = true;
	private boolean alternateRender = true;
	private boolean slimeOverlay = false;
	private boolean loadedOverlay = false;
	private boolean gridOverlay = false;
	private boolean imageChanged = false;	
	private boolean worldmap = false;
	
	public long updated = 0;
	
	private Object imageLock = new Object();
	
	public RegionData(DimensionData data, World world, BlockPos blockPos, boolean worldmap) {
		if (worldmap && !Dimension.isNether(world.getDimensionRegistryKey())) {
			this.layer = Layer.SURFACE;
			this.level = 0;
		} else {
			this.layer = DataUtil.getLayer(world, blockPos);
			this.level = DataUtil.getLevel(layer, blockPos.getY());
		}
		
		this.mapData = data;
		this.world = world;
		this.regPos = new RegionPos(blockPos);
		this.center = new ChunkPos(DataUtil.currentPos());
		this.cacheDir = StorageUtil.cacheDir(world);
		this.image = this.getImage(layer, level);
		this.worldmap = worldmap;
		
		int radius = DataUtil.getGameOptions().viewDistance - 1;
		this.updateArea = new Plane(center.x - radius, center.z - radius,
									center.x + radius, center.z + radius);
		
		this.updateImage(true);
	}
	
	public RegionPos getPos() {
		return this.regPos;
	}
	
	public int getX() {
		return this.regPos.x;
	}
	
	public int getZ() {
		return this.regPos.z;
	}
	
	private MapTexture getImage(Layer layer, int level) {
		File regionFile = this.imageFile(layer, level);
		if (images.containsKey(layer)) {
			MapTexture image = this.images.get(layer);
			if (!image.loadImage(regionFile)) {
				this.image.fill(Colors.BLACK);
			}
			return image;
		}
		
		MapTexture image = new MapTexture(regionFile, 512, 512, Colors.BLACK);
		image.loadImage(regionFile);
		this.images.put(layer, image);
		
		return image;
	}
	
	public void updateWorld(World world, boolean worldmap) {
		this.worldmap = worldmap;
		if (world == null) return;
		if (!world.equals(this.world)) {
			this.cacheDir = StorageUtil.cacheDir(world);
			this.world = world;
			this.clear();
			this.updateImage(true);
		}
	}
	
	public void updateImage(boolean needUpdate) {
		if (updating) return;
		this.updating = true;
		worker.execute(() -> {
			this.updateMapParams(needUpdate);
			this.update();
		});
	}
	
	public void setCenter(ChunkPos centerPos) {
		int radius = DataUtil.getGameOptions().viewDistance - 1;
		this.center = centerPos;
		this.updateArea = new Plane(center.x - radius, center.z - radius,
									center.x + radius, center.z + radius);
	}
	
	public ChunkPos getCenter() {
		return this.center;
	}
	
	private void updateMapParams(boolean needUpdate) {
		this.needUpdate = needUpdate;
		if (ClientParams.hideWater != hideWater) {
			this.hideWater = ClientParams.hideWater;
			this.needUpdate = true;
		}
		boolean waterTint = ClientParams.alternateColorRender && ClientParams.waterTint;
		if (this.waterTint != waterTint) {
			this.waterTint = waterTint;
			this.needUpdate = true;
		}
		if (ClientParams.alternateColorRender != alternateRender) {
			this.alternateRender = ClientParams.alternateColorRender;
			this.needUpdate = true;
		}
		if (ClientParams.showGrid != gridOverlay) {
			this.gridOverlay = ClientParams.showGrid;
			this.renewOverlay = true;
		}
		if (slimeOverlay != RuleUtil.allowSlimeChunks()) {
			this.slimeOverlay = RuleUtil.allowSlimeChunks();
			this.renewOverlay = true;
		}
		if (ClientParams.showLoadedChunks != loadedOverlay) {
			this.loadedOverlay = ClientParams.showLoadedChunks;
			this.renewOverlay = true;
		}
		this.overlayNeeded = gridOverlay || slimeOverlay || loadedOverlay;
		synchronized (imageLock) {
			if (overlayNeeded && texture == null) {
				this.texture = new MapTexture(null, image);
				this.overlay = new MapTexture(null, 512, 512, Colors.TRANSPARENT);
			} else if (!overlayNeeded && texture != null) {
				this.overlay.close();
				this.overlay = null;
				this.texture.close();
				this.texture = null;
			}
		}
	}
	
	private void update() {
		int regX = this.regPos.x << 9;
		int regZ = this.regPos.z << 9;		
		for (int x = 0; x < 512; x += 16) {
			int chunkX = (regX + x) >> 4;
			for (int y = 0; y < 512; y += 16) {
				int chunkZ = (regZ + y) >> 4;				
				ChunkData mapChunk = this.mapData.getChunk(chunkX, chunkZ);
				if (!worldmap && updateArea.contains(Point.fromPos(mapChunk.getPos()))) {
					boolean updated = mapChunk.saveNeeded();
					if (!updated) {
						mapChunk.update(layer, level, needUpdate);
					}
					synchronized (imageLock) {
						if (updated) {
							this.image.writeChunkData(x, y, mapChunk.getColorData(layer, level));
							mapChunk.setSaved();
						}
					}
				}
				if (overlayNeeded) {
					this.updateOverlay(x, y, mapChunk);
				}
			}
		}
		if (imageChanged || image.changed) this.saveImage();
		if (overlayNeeded && (image.changed || overlay.changed)) {
			this.updateTexture();
		}
		this.updated = System.currentTimeMillis();
		this.renewOverlay = false;
		this.needUpdate = false;
		this.updating = false;
	}
	
	public void writeChunkData(ChunkData mapChunk) {
		worker.execute(() -> {
			int x = (mapChunk.getX() << 4) - (this.getX() << 9);
			int y = (mapChunk.getZ() << 4) - (this.getZ() << 9);
			synchronized (imageLock) {
				this.image.writeChunkData(x, y, mapChunk.getColorData(layer, level));
				mapChunk.setSaved();
				this.imageChanged = true;
			}
		});
	}
	
	private void updateTexture() {
		synchronized (imageLock) {
			this.texture.copyData(image);
			this.image.changed = false;
			this.texture.applyOverlay(overlay);		
			this.overlay.changed = false;
		}
	}
	
	private void updateOverlay(int x, int y, ChunkData mapChunk) {
		if (renewOverlay) {
			this.overlay.fill(x, y, 16, 16, Colors.TRANSPARENT);
		}
		if (loadedOverlay && mapChunk.isChunkLoaded()) {
			this.overlay.fill(x, y, 16, 16, Colors.LOADED_OVERLAY);
		} else if (loadedOverlay && !renewOverlay) {
			this.overlay.fill(x, y, 16, 16, Colors.TRANSPARENT);
		}
		if (slimeOverlay && mapChunk.hasSlime()) {
			this.overlay.fill(x, y, 16, 16, Colors.SLIME_OVERLAY);
		}
		if (gridOverlay) {
			this.overlay.setColor(x, y, Colors.GRID);
			for (int i = 1; i < 16; i++) {
				this.overlay.setColor(x + i, y, Colors.GRID);
				this.overlay.setColor(x, y + i, Colors.GRID);
			}
		}
	}
	
	public Layer getLayer() {
		return this.layer != null ? this.layer : null;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public void swapLayer(Layer layer, int level) {
		if (this.layer.equals(layer) &&
			this.level == level) {
			
			return;
		}
		JustMap.LOGGER.debug(String.format("Swap region %s (%s, %d) to: %s, level: %d",
				regPos, this.layer, this.level, layer, level));
		synchronized (imageLock) {
			this.image.saveImage();
			this.layer = layer;
			this.level = level;
			this.image = this.getImage(layer, level);
			this.updateImage(true);
			if (texture != null) {
				this.updateTexture();
			}
		}
	}
	
	private void clear() {
		synchronized (imageLock) {
			this.image.fill(Colors.BLACK);
			if (texture != null) {
				this.overlay.fill(Colors.TRANSPARENT);
				this.texture.fill(Colors.BLACK);
			}
		}
	}
	
	private void saveImage() {
		JustMap.WORKER.execute("Saving image for region: " + regPos, () -> {
			this.image.saveImage();
			this.imageChanged = false;
		});
	}
	
	private File imageFile(Layer layer, int level) {
		File dir = this.cacheDir;
		if (Layer.SURFACE.equals(layer)) {
			dir = new File(dir, "surface");
		} else if (Layer.NETHER.equals(layer)) {
			dir = new File(dir, Integer.toString(level));
		} else {
			dir = new File(dir, String.format("%s/%d", layer.name, level));
		}		
		if (!dir.exists()) {
			dir.mkdirs();
		}

		return new File(dir, String.format("r%d.%d.png", regPos.x, regPos.z));
	}
	
	public void draw(double x, double y, int imgX, int imgY, int width, int height, float scale) {
		if (width <= 0 || height <= 0) return;
		
		float u1 = imgX / 512F;
		float v1 = imgY / 512F;
		float u2 = (imgX + width) / 512F;
		float v2 = (imgY + height) / 512F;
		
		double scW = (double) width / scale;
		double scH = (double) height / scale;
		
		this.drawTexture(x, y, scW, scH, u1, v1, u2, v2);
	}
	
	private void drawTexture(double x, double y, double w, double h, float u1, float v1, float u2, float v2) {
		if (texture != null && texture.changed) {
			this.texture.upload();
		} else if (texture == null && image.changed) {
			this.image.upload();
		}
		int id = texture != null ? texture.getId() : image.getId();
		RenderUtil.bindTexture(id);
		if (ClientParams.textureFilter) {
			RenderUtil.applyFilter();
		}
		
		RenderUtil.startDraw();
		RenderUtil.addQuad(x, y, w, h, u1, v1, u2, v2);
		RenderUtil.endDraw();
	}
	
	public void close() {
		JustMap.LOGGER.debug("Closing region: " + regPos);
		synchronized (imageLock) {
			this.images.forEach((layer, image) -> {
				image.saveImage();
				image.close();
			});
			if (texture != null) {
				this.overlay.close();
				this.texture.close();
			}
		}
	}
}
