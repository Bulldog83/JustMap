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
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.MapScreen;
import ru.bulldog.justmap.map.data.MapCache;
import ru.bulldog.justmap.map.icon.MapIcon;
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
	
	private int paddingTop;
	private int paddingBottom;
	private int scaledWidth;
	private int scaledHeight;
	private float imageScale = 1.0F;
	
	private BlockPos centerPos;
	private NativeImage mapImage;
	private NativeImageBackedTexture mapTexture;
	private Identifier textureId;
	
	private List<WaypointIcon> waypoints = new ArrayList<>();
	
	private Worldmap() {
		super(TITLE);
	}

	@Override
	public void init() {		
		this.paddingTop = 8;
		this.paddingBottom = 8;
		
		if (centerPos == null) {
			centerPos = minecraft.player.getBlockPos();
		}
		
		int dimId = minecraft.player.dimension.getRawId();
		this.info = DIMENSION_INFO.getOrDefault(DimensionType.byRawId(dimId).toString(), null);
		
		children.add(new ButtonWidget(width - 24, 10, 20, 20, "x", (b) -> onClose()));		
		children.add(new ButtonWidget(width / 2 - 10, height - paddingBottom - 44, 20, 20, "\u2191", (b) -> moveMap(Direction.NORTH)));
		children.add(new ButtonWidget(width / 2 - 10, height - paddingBottom - 22, 20, 20, "\u2193", (b) -> moveMap(Direction.SOUTH)));
		children.add(new ButtonWidget(width / 2 - 32, height - paddingBottom - 32, 20, 20, "\u2190", (b) -> moveMap(Direction.WEST)));
		children.add(new ButtonWidget(width / 2 + 12, height - paddingBottom - 32, 20, 20, "\u2192", (b) -> moveMap(Direction.EAST)));		
		children.add(new ButtonWidget(width - 24, height / 2 - 21, 20, 20, "+", (b) -> changeScale(-0.25F)));
		children.add(new ButtonWidget(width - 24, height / 2 + 1, 20, 20, "-", (b) -> changeScale(+0.25F)));		
		children.add(new ButtonWidget(width - 24, height - paddingBottom - 22, 20, 20, "\u271C", (b) -> setCenterByPlayer()));
		
		PlayerEntity player = minecraft.player;
		
		waypoints.clear();
		List<Waypoint> wps = WaypointKeeper.getInstance().getWaypoints(player.world.dimension.getType().getRawId(), true);
		if (wps != null) {
			Stream<Waypoint> stream = wps.stream().filter(wp -> MathUtil.getDistance(player.getBlockPos(), wp.pos, false) <= wp.showRange);
			for (Waypoint wp : stream.toArray(Waypoint[]::new)) {
				WaypointIcon waypoint = new WaypointIcon(this, wp);
				this.waypoints.add(waypoint);
			}
		}
	}
	
	@Override
	public void renderBackground() {
		fill(x, 0, x + width, height, 0xFF444444);
		drawMap();
		
		int centerX = (centerPos.getX() >> 4) << 4;
		int centerZ = (centerPos.getZ() >> 4) << 4;
		
		double startX = centerX - scaledWidth / 2;
		double startZ = centerZ - scaledHeight / 2;	
		double endX = startX + scaledWidth;
		double endZ = startZ + scaledHeight;
		
		int iconSize = MathUtil.clamp((int) (8 / imageScale), 4, 8);
		for (WaypointIcon icon : waypoints) {
			if (!icon.isHidden()) {
				icon.setPosition(
					iconPos(icon.waypoint.pos.getX(), startX, endX, width),
					iconPos(icon.waypoint.pos.getZ(), startZ, endZ, height)
				);
				icon.draw(0, paddingTop, iconSize);
			}
		}
		
		PlayerEntity player = minecraft.player;
		
		int playerX = player.getBlockPos().getX();
		int playerZ = player.getBlockPos().getZ();
		double arrowX = iconPos(playerX, startX, endX, width);
		double arrowY = iconPos(playerZ, startZ, endZ, height);
		
		DirectionArrow.draw(arrowX, arrowY, iconSize, player.headYaw);
	}
	
	private double iconPos(int i, double start, double end, int range) {
		int chunkI = (i >> 4) << 4;
		int posI = i - chunkI;
		
		return MapIcon.scaledPos(chunkI, start, end, range) + posI;
	}
	
	@Override
	public void renderForeground() {
		drawBorders(paddingTop, paddingBottom);
	}
	
	private void prepareTexture() {
		this.scaledWidth = (int) (width * imageScale);
		this.scaledHeight = (int) (height * imageScale);
		
		if (mapImage == null || mapImage.getWidth() != scaledWidth || mapImage.getHeight() != scaledHeight) {
			this.mapImage = new NativeImage(scaledWidth, scaledHeight, false);
			ImageUtil.fillImage(mapImage, Colors.BLACK);
			
			updateMapTexture();
			
			if (mapTexture != null) mapTexture.close();
			
			mapTexture = new NativeImageBackedTexture(mapImage);
			textureId = minecraft.getTextureManager().registerDynamicTexture(JustMap.MODID + "_worldmap_texture", mapTexture);
		}
	}
	
	private void updateMapTexture() {
		int centerX = centerPos.getX() >> 4;
		int centerZ = centerPos.getZ() >> 4;		
		int chunksX = (scaledWidth >> 4) / 2 + 1;
		int chunksZ = (scaledHeight >> 4) / 2 + 1;
		int startX = centerX - chunksX;
		int startZ = centerZ - chunksZ;
		int stopX = centerX + chunksX;
		int stopZ = centerZ + chunksZ;
		
		MapCache mapData = MapCache.get();
		
		int picX = 0, picY = 0;
		for (int posX = startX; posX < stopX; posX++) {
			picY = 0;
			for (int posZ = startZ; posZ < stopZ; posZ++) {
				ChunkPos pos = new ChunkPos(posX, posZ);
				NativeImage chunkImage = mapData.getRegion(pos).getChunkImage(pos);
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
	
	private void bindMapTexture() {
		prepareTexture();
		minecraft.getTextureManager().bindTexture(textureId);
	}
	
	private void drawMap() {
		bindMapTexture();
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		
		builder.begin(7, VertexFormats.POSITION_TEXTURE);			
		builder.vertex(0, 0, 0).texture(0, 0).next();
		builder.vertex(0, height, 0).texture(0, 1).next();
		builder.vertex(width, height, 0).texture(1, 1).next();
		builder.vertex(width, 0, 0).texture(1, 0).next();
		
		tessellator.draw();
	}
	
	public void setCenterByPlayer() {
		this.centerPos = minecraft.player.getBlockPos();
  		updateMapTexture();
	}
	
	private void changeScale(float value) {
		this.imageScale = MathUtil.clamp(this.imageScale + value, 0.5F, 3.0F);		
		prepareTexture();
	}
	
	private void moveMap(Direction direction) {
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
		int x = centerPos.getX();
		int y = centerPos.getY();
		int z = centerPos.getZ();
		
		x -= Math.ceil(f);
		z -= Math.ceil(g);
		
		this.centerPos = new BlockPos(x, y, z);
		updateMapTexture();
		
		return super.mouseDragged(d, e, i, f, g);
	}
	
	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		changeScale(f > 0 ? -0.25F : 0.25F);		
		return super.mouseScrolled(d, e, f);
	}

	@Override
	public int getWidth() {
		return this.width;
	}

	@Override
	public int getHeight() {
		return this.height;
	}

	@Override
	public int getScaledWidth() {
		return this.scaledWidth;
	}

	@Override
	public int getScaledHeight() {
		return this.scaledHeight;
	}

	@Override
	public float getScale() {
		return this.getScale();
	}
}
