package old_files.justmap.client.map.data;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.util.math.ChunkPos;

import old_files.justmap.JustMap;
import old_files.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Logger;
import old_files.justmap.util.RuleUtil;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.color.Colors;
import ru.bulldog.justmap.util.math.Plane;
import ru.bulldog.justmap.util.math.Point;
import ru.bulldog.justmap.client.render.MapTexture;
import ru.bulldog.justmap.client.render.RenderUtil;
import ru.bulldog.justmap.util.StorageUtil;
import old_files.justmap.util.tasks.TaskManager;

public class RegionData {
	
	private static TaskManager updater = TaskManager.getManager("region-updater");
	private static TaskManager worker = JustMap.WORKER;
	private static Logger logger = JustMap.LOGGER;
	
	private final WorldData mapData;
	private final RegionPos regPos;
	private final Map<Layer, MapTexture> images = new ConcurrentHashMap<>();
	private File cacheDir;
	private MapTexture image;
	private MapTexture texture;
	private MapTexture overlay;
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
	
	public RegionData(IMap map, WorldData data, RegionPos regPos) {
		this(data, regPos);
		
		this.layer = map.getLayer();
		this.level = map.getLevel();
		this.center = new ChunkPos(map.getCenter());
		this.worldmap = map.isWorldmap();
		int radius = DataUtil.getGameOptions().viewDistance - 1;
		this.updateArea = new Plane(center.x - radius, center.z - radius,
									center.x + radius, center.z + radius);
		this.loadImage(layer, level);
	}
	
	private RegionData(WorldData data, RegionPos regPos) {
		this.mapData = data;
		this.regPos = regPos;
		this.cacheDir = StorageUtil.cacheDir();
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
	
	public void setIsWorldmap(boolean isWorldmap) {
		this.worldmap = isWorldmap;
	}
	
	private void loadImage(Layer layer, int level) {
		File regionFile = this.imageFile(layer, level);
		if (images.containsKey(layer)) {
			this.image = this.images.get(layer);
		} else {
			this.image = new MapTexture(regionFile, 512, 512, Colors.BLACK);
			this.images.put(layer, image);
		}
		worker.execute(() -> {
			synchronized (imageLock) {
				if (!image.loadImage(regionFile)) {
					this.image.fill(Colors.BLACK);
				}
			}
			this.updateImage(true);
		});
	}
	
	public void updateImage(boolean needUpdate) {
		if (updating) return;
		this.updating = true;
		updater.execute(() -> {
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
		if (ClientSettings.hideWater != hideWater) {
			this.hideWater = ClientSettings.hideWater;
			this.needUpdate = true;
		}
		boolean waterTint = ClientSettings.alternateColorRender && ClientSettings.waterTint;
		if (this.waterTint != waterTint) {
			this.waterTint = waterTint;
			this.needUpdate = true;
		}
		if (ClientSettings.alternateColorRender != alternateRender) {
			this.alternateRender = ClientSettings.alternateColorRender;
			this.needUpdate = true;
		}
		if (slimeOverlay != RuleUtil.allowSlimeChunks()) {
			this.slimeOverlay = RuleUtil.allowSlimeChunks();
			this.renewOverlay = true;
		}
		if (ClientSettings.showLoadedChunks != loadedOverlay) {
			this.loadedOverlay = ClientSettings.showLoadedChunks;
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
				if (updateArea.contains(Point.fromPos(mapChunk.getPos()))) {
					boolean updated = mapChunk.saveNeeded();
					if (!worldmap) {
						if (!updated) {
							mapChunk.updateFullChunk(layer, level, needUpdate);
						}
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
		updater.execute(() -> {
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
		logger.debug("Swap region {} ({}, {}) to: {}, level: {}",
				regPos, this.layer, this.level, layer, level);
		this.layer = layer;
		this.level = level;
		synchronized (imageLock) {
			MapTexture toSave = new MapTexture(image);
			this.loadImage(layer, level);
			this.saveImage(toSave, true);
		}
	}
	
	private void saveImage() {
		this.saveImage(image, false);
		this.imageChanged = false;
	}
	
	private void saveImage(MapTexture image, boolean close) {
		worker.execute("Saving image for region: " + regPos, () -> {
			image.saveImage();
			if (close) {
				image.close();
			}
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
	
	public void draw(double x, double y, double width, double height, int imgX, int imgY, int imgW, int imgH) {
		if (width <= 0 || height <= 0) return;
		
		float u1 = imgX / 512F;
		float v1 = imgY / 512F;
		float u2 = (imgX + imgW) / 512F;
		float v2 = (imgY + imgH) / 512F;
		
		this.drawTexture(x, y, width, height, u1, v1, u2, v2);
	}
	
	private void drawTexture(double x, double y, double w, double h, float u1, float v1, float u2, float v2) {
		if (texture != null && texture.changed) {
			this.texture.upload();
		} else if (texture == null && image.changed) {
			this.image.upload();
		}
		int id = texture != null ? texture.getId() : image.getId();
		RenderUtil.bindTexture(id);
		RenderUtil.applyFilter(false);
		RenderUtil.startDraw();
		RenderUtil.addQuad(x, y, w, h, u1, v1, u2, v2);
		RenderUtil.endDraw();
	}
	
	public void close() {
		logger.debug("Closing region: {}", regPos);
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
