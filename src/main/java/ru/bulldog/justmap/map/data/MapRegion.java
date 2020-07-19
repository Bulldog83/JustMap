package ru.bulldog.justmap.map.data;

import java.io.File;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.MapTexture;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.math.Plane;
import ru.bulldog.justmap.util.math.Point;
import ru.bulldog.justmap.util.storage.StorageUtil;
import ru.bulldog.justmap.util.tasks.TaskManager;

public class MapRegion {
	
	private static TaskManager worker = TaskManager.getManager("region-data");
	
	private final RegionPos regPos;
	private final MapTexture image;
	private World world;
	private MapTexture texture;
	private MapTexture overlay;
	private Layer.Type layer;
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
	
	public boolean surfaceOnly = false;	
	public long updated = 0;
	
	public MapRegion(World world, BlockPos blockPos, Layer.Type layer, int level) {
		this.world = world;
		this.regPos = new RegionPos(blockPos);
		this.center = new ChunkPos(blockPos);
		this.image = new MapTexture(512, 512, Colors.BLACK);
		this.layer = layer;
		this.level = level;
		
		int radius = JustMapClient.MINECRAFT.options.viewDistance;
		this.updateArea = new Plane(center.x - radius, center.z - radius,
									center.x + radius, center.z + radius);
		
		this.loadImage();
		this.updateImage(true);
	}
	
	public int getX() {
		return this.regPos.x;
	}
	
	public int getZ() {
		return this.regPos.z;
	}
	
	public void updateWorld(World world) {
		if (world == null) return;
		if (!world.equals(this.world)) {
			this.world = world;
			this.clear();
			this.updateImage(true);
		}
	}
	
	public void updateImage(boolean needUpdate) {
		if (updating) return;
		this.updating = true;
		worker.execute("Updating Region: " + regPos, () -> {
			this.updateMapParams(needUpdate);
			this.update();
		});
	}
	
	public void setCenter(ChunkPos centerPos) {
		int radius = JustMapClient.MINECRAFT.options.viewDistance;
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
		if (ClientParams.showSlime != Minimap.allowSlimeChunks()) {
			this.slimeOverlay = Minimap.allowSlimeChunks();
			this.renewOverlay = true;
		}
		if (ClientParams.showLoadedChunks != loadedOverlay) {
			this.loadedOverlay = ClientParams.showLoadedChunks;
			this.renewOverlay = true;
		}
		this.overlayNeeded = gridOverlay || slimeOverlay || loadedOverlay;
		if (overlayNeeded && overlay == null) {
			this.texture = new MapTexture(image);
			this.overlay = new MapTexture(512, 512, Colors.TRANSPARENT);
		} else if (!overlayNeeded && overlay != null) {
			this.overlay.close();
			this.overlay = null;
			this.texture.close();
			this.texture = null;
		}
	}
	
	private void update() {
		DimensionData mapData = DimensionManager.getData();
		ChunkDataManager chunkManager = mapData.getChunkManager();
		
		int regX = this.regPos.x << 9;
		int regZ = this.regPos.z << 9;		
		for (int x = 0; x < 512; x += 16) {
			int chunkX = (regX + x) >> 4;
			for (int y = 0; y < 512; y += 16) {
				int chunkZ = (regZ + y) >> 4;
				
				ChunkData mapChunk;
				boolean updated = false;
				if (surfaceOnly) {
					mapChunk = chunkManager.getChunk(Layer.Type.SURFACE, 0, chunkX, chunkZ);
					if (DimensionData.currentLayer() == Layer.Type.SURFACE &&
						updateArea.contains(Point.fromPos(mapChunk.getPos()))) {
						
						updated = mapChunk.update(needUpdate);
					}
				} else {
					mapChunk = mapData.getCurrentChunk(chunkX, chunkZ);
					if (updateArea.contains(Point.fromPos(mapChunk.getPos()))) {
						updated = mapChunk.update(needUpdate);
					}
				}
				if (updated) {
					this.image.writeChunkData(x, y, mapChunk.getColorData());
				}
				if (overlayNeeded) {
					this.updateOverlay(x, y, mapChunk);
				}
			}
		}
		if (image.changed) this.saveImage();
		if (overlayNeeded && (image.changed || overlay.changed)) {
			this.updateTexture();
		}
		this.updated = System.currentTimeMillis();
		this.renewOverlay = false;
		this.needUpdate = false;
		this.updating = false;
	}
	
	private void updateTexture() {
		this.texture.copyData(image);
		this.image.changed = false;
		this.texture.applyOverlay(overlay);		
		this.overlay.changed = false;
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
	
	public Layer.Type getLayer() {
		return this.layer != null ? this.layer : null;
	}
	
	public int getLevel() {
		return this.level;
	}
	
	public void swapLayer(Layer.Type layer, int level) {
		this.layer = layer;
		this.level = level;
		if (!this.loadImage()) {
			this.image.fill(Colors.BLACK);
		}
		if (texture != null) {
			this.updateTexture();
		}
		this.updateImage(true);
	}
	
	private void clear() {
		this.image.fill(Colors.BLACK);
		if (texture != null) {
			this.overlay.fill(Colors.TRANSPARENT);
			this.texture.fill(Colors.BLACK);
		}
	}
	
	private void saveImage() {
		File imgFile = this.imageFile();
		JustMap.WORKER.execute("Saving image for region: " + regPos, () -> this.image.saveImage(imgFile));
	}
	
	private boolean loadImage() {
		File imgFile = this.imageFile();
		return this.image.loadImage(imgFile);
	}
	
	private File imageFile() {
		File dir = StorageUtil.cacheDir();
		if (surfaceOnly || Layer.Type.SURFACE == layer) {
			dir = new File(dir, "surface");
		} else {
			dir = new File(dir, String.format("%s/%d", layer.value.name, level));
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
		this.image.close();
		if (overlay != null) {
			this.overlay.close();
			this.texture.close();
		}
	}
}
