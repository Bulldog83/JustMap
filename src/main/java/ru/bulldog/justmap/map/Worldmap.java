package ru.bulldog.justmap.map;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Direction;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.MapScreen;
import ru.bulldog.justmap.client.config.ConfigFactory;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapCache;
import ru.bulldog.justmap.map.data.MapChunk;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.math.MathUtil;

public class Worldmap extends MapScreen implements AbstractMap {

	private static final LiteralText TITLE = new LiteralText("Worldmap");
	
	private static Worldmap worldmap;
	
	public static Worldmap getScreen() {
		if (worldmap == null) {
			worldmap = new Worldmap();
		}
		
		return worldmap;
	}
	
	private double mapWidth;
	private double mapHeight;
	private double scaledWidth;
	private double scaledHeight;
	private double centerX;
	private double centerZ;
	private double startX;
	private double startZ;	
	private double endX;
	private double endZ;
	private double shiftW;
	private double shiftH;
	private int dimension;
	private float imageScale = 1.0F;
	private boolean playerTracking = true;
	
	private long updateInterval = 50;
	private long updated = 0;
	
	private BlockPos centerPos;
	private NativeImage mapImage;
	private NativeImageBackedTexture mapTexture;
	private Identifier textureId;
	private String cursorCoords;
	
	private List<WaypointIcon> waypoints = new ArrayList<>();
	
	private Worldmap() {
		super(TITLE);
	}

	@Override
	public void init() {		
		super.init();
		
		this.paddingTop = 8;
		this.paddingBottom = 8;
		
		this.mapWidth = Math.ceil(width / 128F) * 128;
		this.mapHeight = Math.ceil(height / 128F) * 128;
		
		PlayerEntity player = minecraft.player;
		
		int currentDim = player.dimension.getRawId();
		if (centerPos == null || currentDim != dimension) {
			this.dimension = currentDim;
			this.centerPos = minecraft.player.getBlockPos();
		} else if (playerTracking) {
			this.centerPos = minecraft.player.getBlockPos();
		}
		
		this.cursorCoords = String.format("%d, %d, %d", centerPos.getX(), centerPos.getY(), centerPos.getZ());
		
		addMapButtons();
		
		waypoints.clear();
		List<Waypoint> wps = WaypointKeeper.getInstance().getWaypoints(dimension, true);
		if (wps != null) {
			Stream<Waypoint> stream = wps.stream().filter(wp -> MathUtil.getDistance(player.getBlockPos(), wp.pos) <= wp.showRange);
			for (Waypoint wp : stream.toArray(Waypoint[]::new)) {
				WaypointIcon waypoint = new WaypointIcon(this, wp);
				this.waypoints.add(waypoint);
			}
		}
		
		if (mapImage == null) {
			prepareTexture();
		} else {
			updateMapTexture();
		}
	}
	
	private void addMapButtons() {
		children.add(new ButtonWidget(width - 24, 10, 20, 20, "x", (b) -> onClose()));		
		children.add(new ButtonWidget(width / 2 - 10, height - paddingBottom - 44, 20, 20, "\u2191", (b) -> moveMap(Direction.NORTH)));
		children.add(new ButtonWidget(width / 2 - 10, height - paddingBottom - 22, 20, 20, "\u2193", (b) -> moveMap(Direction.SOUTH)));
		children.add(new ButtonWidget(width / 2 - 32, height - paddingBottom - 32, 20, 20, "\u2190", (b) -> moveMap(Direction.WEST)));
		children.add(new ButtonWidget(width / 2 + 12, height - paddingBottom - 32, 20, 20, "\u2192", (b) -> moveMap(Direction.EAST)));		
		children.add(new ButtonWidget(width - 24, height / 2 - 21, 20, 20, "+", (b) -> changeScale(-0.25F)));
		children.add(new ButtonWidget(width - 24, height / 2 + 1, 20, 20, "-", (b) -> changeScale(+0.25F)));		
		children.add(new ButtonWidget(width - 24, height - paddingBottom - 22, 20, 20, "\u271C", (b) -> setCenterByPlayer()));
		children.add(new ButtonWidget(4, paddingTop + 2, 20, 20, "\u2630",(b) -> minecraft.openScreen(ConfigFactory.getConfigScreen(this))));
	}
	
	@Override
	public void renderBackground() {
		fill(x, 0, x + width, height, 0xFF444444);
		
		drawMap();
		
		int iconSize = MathUtil.clamp((int) (10 / imageScale), 6, 10);
		for (WaypointIcon icon : waypoints) {
			if (!icon.isHidden()) {
				icon.setPosition(
					MathUtil.screenPos(icon.waypoint.pos.getX(), startX, endX, mapWidth) - shiftW,
					MathUtil.screenPos(icon.waypoint.pos.getZ(), startZ, endZ, mapHeight) - shiftH
				);
				icon.draw(iconSize);
			}
		}
		
		PlayerEntity player = minecraft.player;
		
		int playerX = player.getBlockPos().getX();
		int playerZ = player.getBlockPos().getZ();
		double arrowX = MathUtil.screenPos(playerX, startX, endX, mapWidth) - shiftW;
		double arrowY = MathUtil.screenPos(playerZ, startZ, endZ, mapHeight) - shiftH;
		
		DirectionArrow.draw(arrowX, arrowY, iconSize, player.headYaw);
	}
	
	@Override
	public void renderForeground() {
		drawBorders(paddingTop, paddingBottom);
		
		int x = width / 2;
		this.drawCenteredString(minecraft.textRenderer, cursorCoords, x, paddingTop + 4, Colors.WHITE);
	}
	
	private void prepareTexture() {
		this.scaledWidth = mapWidth * imageScale;
		this.scaledHeight = mapHeight * imageScale;
		
		if (mapImage == null || mapImage.getWidth() != scaledWidth || mapImage.getHeight() != scaledHeight) {
			this.mapImage = new NativeImage((int) Math.ceil(scaledWidth), (int) Math.ceil(scaledHeight), false);
			ImageUtil.fillImage(mapImage, Colors.BLACK);
			
			updateMapTexture();
			
			if (mapTexture != null) mapTexture.close();
			
			mapTexture = new NativeImageBackedTexture(mapImage);
			textureId = minecraft.getTextureManager().registerDynamicTexture(JustMap.MODID + "_worldmap_texture", mapTexture);
		}
	}
	
	private void updateMapTexture() {
		calculateShift();
		
		int centerX = centerPos.getX() >> 4;
		int centerZ = centerPos.getZ() >> 4;
		int chunksX = (int) Math.ceil(scaledWidth / 32D);
		int chunksZ = (int) Math.ceil(scaledHeight / 32D);
		int startX = centerX - chunksX;
		int startZ = centerZ - chunksZ;
		int stopX = centerX + chunksX;
		int stopZ = centerZ + chunksZ;
		
		MapCache mapData = MapCache.get();
		
		int picX = 0;
		for (int posX = startX; posX < stopX; posX++) {
			int picY = 0;
			for (int posZ = startZ; posZ < stopZ; posZ++) {
				ChunkPos pos = new ChunkPos(posX, posZ);
				
				NativeImage chunkImage;
				if (dimension != -1) {
					chunkImage = mapData.getRegion(pos).getChunkImage(pos, Layer.SURFACE, 0);
				} else {
					chunkImage = mapData.getRegion(pos).getChunkImage(pos);
				}
				
				ImageUtil.writeTile(mapImage, chunkImage, picX, picY);
				
				chunkImage.close();
				picY += 16;
			}			
			picX += 16;
		}
		
		if (mapTexture != null) {
			mapTexture.upload();
		}
	}
	
	private void drawMap() {
		prepareTexture();		
		minecraft.getTextureManager().bindTexture(textureId);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		
		double x1 = -shiftW;
		double y1 = -shiftH;
		double x2 = mapWidth - shiftW;
		double y2 = mapHeight - shiftH;
		
		builder.begin(7, VertexFormats.POSITION_TEXTURE);			
		builder.vertex(x1, y1, 0).texture(0F, 0F).next();
		builder.vertex(x1, y2, 0).texture(0F, 1F).next();
		builder.vertex(x2, y2, 0).texture(1F, 1F).next();
		builder.vertex(x2, y1, 0).texture(1F, 0F).next();
		
		tessellator.draw();
	}
	
	private void calculateShift() {
		this.centerX = (centerPos.getX() >> 4) << 4;
		this.centerZ = (centerPos.getZ() >> 4) << 4;
		this.startX = centerX - scaledWidth / 2;
		this.startZ = centerZ - scaledHeight / 2;	
		this.endX = startX + scaledWidth;
		this.endZ = startZ + scaledHeight;
		
		double screenCX = MathUtil.screenPos(centerPos.getX(), startX, endX, mapWidth);
		double screenCY = MathUtil.screenPos(centerPos.getZ(), startZ, endZ, mapHeight);
		
		this.shiftW = screenCX - width / 2F;
		this.shiftH = screenCY - height / 2F;
	}
	
	public void setCenterByPlayer() {
		this.playerTracking = true;
		this.centerPos = minecraft.player.getBlockPos();
  		
		updateMapTexture();
	}
	
	private void changeScale(float value) {
		this.imageScale = MathUtil.clamp(this.imageScale + value, 0.75F, 4F);		
		prepareTexture();
	}
	
	private void moveMap(Direction direction) {
		long time = System.currentTimeMillis();
		
		if (time - updated < updateInterval) return;
		
		switch (direction) {
			case NORTH:
				this.centerPos = centerPos.add(0, 0, -16);
				break;
			case SOUTH:
				this.centerPos = centerPos.add(0, 0, 16);
				break;
			case EAST:
				this.centerPos = centerPos.add(16, 0, 0);
				break;
			case WEST:
				this.centerPos = centerPos.add(-16, 0, 0);
				break;
			default: break;
		}
		
		updateMapTexture();
		
		this.playerTracking = false;
		this.updated = time;
	}
	
	@Override
	public boolean keyPressed(int i, int j, int k) {
		switch(i) {
			case GLFW.GLFW_KEY_W:
			case GLFW.GLFW_KEY_UP:
				moveMap(Direction.NORTH);
		  		return true;
		  	case GLFW.GLFW_KEY_S:
		  	case GLFW.GLFW_KEY_DOWN:
		  		moveMap(Direction.SOUTH);
		  		return true;
		  	case GLFW.GLFW_KEY_A:
		  	case GLFW.GLFW_KEY_LEFT:
		  		moveMap(Direction.WEST);
		  		return true;
		  	case GLFW.GLFW_KEY_D:
		  	case GLFW.GLFW_KEY_RIGHT:
		  		moveMap(Direction.EAST);
		  		return true;
		  	case GLFW.GLFW_KEY_MINUS:
		  	case GLFW.GLFW_KEY_KP_SUBTRACT:
		  		changeScale(0.25F);
		  		return true;
		  	case GLFW.GLFW_KEY_EQUAL:
		  	case GLFW.GLFW_KEY_KP_ADD:
		  		changeScale(-0.25F);
		  		return true;
		  	case GLFW.GLFW_KEY_X:
		  		setCenterByPlayer();
		  		return true;
		  	case GLFW.GLFW_KEY_M:
		  		this.onClose();
		  		return true;
		  	default:
		  		return super.keyPressed(i, j, k);
		}
	}
	
	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		if (super.mouseDragged(d, e, i, f, g)) return true;
		
		if (i == 0) {
			long time = System.currentTimeMillis();
			if (time - updated < updateInterval) return true;
			
			int x = centerPos.getX();
			int y = centerPos.getY();
			int z = centerPos.getZ();
			
			x -= MathUtil.clamp(Math.round((8 * f) * imageScale), -32, 32);
			z -= MathUtil.clamp(Math.round((8 * g) * imageScale), -32, 32);
			
			this.centerPos = new BlockPos(x, y, z);
			
			updateMapTexture();
			
			this.playerTracking = false;
			this.updated = time;
		
			return true;
		}
		
		return false;
	}
	
	private int pixelToPos(double x, int cx, double d) {
		double x1 = cx - d / 2;
		double x2 = x1 + d;
		
		return MathUtil.worldPos(x, x1, x2, d);
	}
	
	private BlockPos cursorBlockPos(double x, double y) {
		
		int posX = pixelToPos(x + shiftW, (centerPos.getX() >> 4) << 4, mapWidth);
		int posZ = pixelToPos(y + shiftH, (centerPos.getZ() >> 4) << 4, mapHeight);
		
		int chunkX = posX >> 4;
		int chunkZ = posZ >> 4;
		
		MapChunk mapChunk;
		if (dimension != -1) {
			mapChunk = MapCache.get().getChunk(Layer.SURFACE, 0, chunkX, chunkZ);
		} else {
			mapChunk = MapCache.get().getChunk(chunkX, chunkZ);
		}
		
		mapChunk.updateHeighmap();
		
		int cx = posX - (chunkX << 4);
		int cz = posZ - (chunkZ << 4);
		
		int posY = mapChunk.getHeighmap()[cx + (cz << 4)];
		posY = posY == -1 ? centerPos.getY() : posY;
		
		return new BlockPos(posX, posY, posZ);
	}
	
	@Override
	public void mouseMoved(double d, double e) {		
		BlockPos worldPos = cursorBlockPos(d, e);		
		this.cursorCoords = String.format("%d, %d, %d", worldPos.getX(), worldPos.getY(), worldPos.getZ());
	}
	
	private int clicks = 0;
	private long clicked = 0;
	
	@Override
	public boolean mouseReleased(double d, double e, int i) {
		if (super.mouseReleased(d, e, i)) return true; 
		
		if (i == 0) {
			long time = System.currentTimeMillis();
			if (time - clicked > 500) clicks = 0;
			
			if (++clicks == 2) {			
				JustMapClient.MAP.createWaypoint(dimension, cursorBlockPos(d, e));
				
				clicked = 0;
				clicks = 0;
			} else {
				clicked = time;
			}
			
			return true;
		}
		
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		changeScale(f > 0 ? -0.25F : 0.25F);
		return true;
	}

	@Override
	public int getWidth() {
		return (int) this.mapWidth;
	}

	@Override
	public int getHeight() {
		return (int) this.mapHeight;
	}

	@Override
	public int getScaledWidth() {
		return (int) Math.ceil(this.scaledWidth);
	}

	@Override
	public int getScaledHeight() {
		return (int) Math.ceil(this.scaledHeight);
	}

	@Override
	public float getScale() {
		return this.getScale();
	}
}
