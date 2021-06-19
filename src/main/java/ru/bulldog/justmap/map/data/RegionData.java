package ru.bulldog.justmap.map.data;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.level.ChunkPos;
import org.jetbrains.annotations.Nullable;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Logger;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.Plane;
import ru.bulldog.justmap.util.math.Point;
import ru.bulldog.justmap.util.render.MapTexture;
import ru.bulldog.justmap.util.render.RenderUtil;
import ru.bulldog.justmap.util.storage.StorageUtil;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class RegionData {
	
	private static final TaskManager updater = TaskManager.getManager("region-updater");
	private static final TaskManager worker = JustMap.WORKER;
	private static final Logger logger = JustMap.LOGGER;
	
	private final WorldData mapData;
	private final RegionPos regPos;
	private final Map<Layer, MapTexture> images = new ConcurrentHashMap<>();
	private final File cacheDir;
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
	private boolean imageChanged = false;
	private boolean worldmap = false;
	
	public long updated = 0;
	
	private final Object imageLock = new Object();
	
	public RegionData(IMap map, WorldData data, RegionPos regPos) {
		this(data, regPos);
		
		this.layer = map.getLayer();
		this.level = map.getLevel();
		this.center = new ChunkPos(map.getCenter());
		this.worldmap = map.isWorldmap();
		int radius = DataUtil.getGameOptions().renderDistance - 1;
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
		File regionFile = imageFile(layer, level);
		if (images.containsKey(layer)) {
			image = images.get(layer);
		} else {
			image = new MapTexture(regionFile, 512, 512, Colors.BLACK);
			images.put(layer, image);
		}
		worker.execute(() -> {
			synchronized (imageLock) {
				if (!image.loadImage(regionFile)) {
					image.fill(Colors.BLACK);
				}
			}
			updateImage(true);
		});
	}
	
	public void updateImage(boolean needUpdate) {
		if (updating) return;
		updating = true;
		updater.execute(() -> {
			updateMapParams(needUpdate);
			update();
		});
	}
	
	public void setCenter(ChunkPos centerPos) {
		int radius = DataUtil.getGameOptions().renderDistance - 1;
		center = centerPos;
		updateArea = new Plane(center.x - radius, center.z - radius,
				center.x + radius, center.z + radius);
	}
	
	public ChunkPos getCenter() {
		return center;
	}
	
	private void updateMapParams(boolean needUpdate) {
		this.needUpdate = needUpdate;
		if (ClientSettings.hideWater != hideWater) {
			hideWater = ClientSettings.hideWater;
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
		this.overlayNeeded = slimeOverlay || loadedOverlay;
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
			int x = (mapChunk.getX() << 4) - (getX() << 9);
			int y = (mapChunk.getZ() << 4) - (getZ() << 9);
			synchronized (imageLock) {
				image.writeChunkData(x, y, mapChunk.getColorData(layer, level));
				mapChunk.setSaved();
				imageChanged = true;
			}
		});
	}
	
	private void updateTexture() {
		synchronized (imageLock) {
			texture.copyData(image);
			image.changed = false;
			texture.applyOverlay(overlay);
			overlay.changed = false;
		}
	}
	
	private void updateOverlay(int x, int y, ChunkData mapChunk) {
		if (renewOverlay) {
			overlay.fill(x, y, 16, 16, Colors.TRANSPARENT);
		}
		if (loadedOverlay && mapChunk.isChunkLoaded()) {
			overlay.fill(x, y, 16, 16, Colors.LOADED_OVERLAY);
		} else if (loadedOverlay && !renewOverlay) {
			overlay.fill(x, y, 16, 16, Colors.TRANSPARENT);
		}
		if (slimeOverlay && mapChunk.hasSlime()) {
			overlay.fill(x, y, 16, 16, Colors.SLIME_OVERLAY);
		}
	}
	
	@Nullable
	public Layer getLayer() {
		return layer;
	}
	
	public int getLevel() {
		return level;
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
	
	public void draw(PoseStack matrices, double x, double y, double width, double height, int imgX, int imgY, int imgW, int imgH) {
		if (width <= 0 || height <= 0) return;
		
		float u1 = imgX / 512F;
		float v1 = imgY / 512F;
		float u2 = (imgX + imgW) / 512F;
		float v2 = (imgY + imgH) / 512F;
		
		drawTexture(matrices, x, y, width, height, u1, v1, u2, v2);
	}
	
	private void drawTexture(PoseStack matrices, double x, double y, double w, double h, float u1, float v1, float u2, float v2) {
		if (texture != null && texture.changed) {
			texture.upload();
		} else if (texture == null && image.changed) {
			image.upload();
		}
		int id = texture != null ? texture.getId() : image.getId();
		RenderUtil.bindTexture(id);
		//RenderUtil.applyFilter(false);
		RenderUtil.startDraw();
		RenderUtil.addQuad(matrices, x, y, w, h, u1, v1, u2, v2);
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
				overlay.close();
				texture.close();
			}
		}
	}
}
