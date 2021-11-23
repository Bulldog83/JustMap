package ru.bulldog.justmap.client.screen;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import org.lwjgl.glfw.GLFW;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.client.config.ConfigFactory;
import ru.bulldog.justmap.client.widget.DropDownListWidget;
import ru.bulldog.justmap.client.widget.ListElementWidget;
import ru.bulldog.justmap.map.ChunkGrid;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapDataProvider;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.data.WorldKey;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.LangUtil;
import ru.bulldog.justmap.util.PosUtil;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.MathUtil;

public class WorldmapScreen extends AbstractMapScreen implements IMap {

	private final static LiteralText TITLE = new LiteralText("Worldmap");
	
	private static WorldmapScreen worldmap;
	
	public static WorldmapScreen getScreen() {
		if (worldmap == null) {
			worldmap = new WorldmapScreen();
		}
		return worldmap;
	}
	
	private int scaledWidth;
	private int scaledHeight;
	private double centerX;
	private double centerY;
	private float imageScale = 1.0F;
	private boolean playerTracking = true;
	private int mapLevel = 0;
	private DropDownListWidget mapMenu;
	private MapRegionProvider mapRegionProvider;
	private WorldKey world;
	private BlockPos centerPos;
	private String cursorCoords;
	private Layer mapLayer;
	private ChunkGrid chunkGrid;
	
	private final List<WaypointIcon> waypoints = new ArrayList<>();
	private final List<PlayerIcon> players = new ArrayList<>();
	
	private WorldmapScreen() {
		super(TITLE);
	}

	@Override
	public void init() {		
		super.init();
		
		this.paddingTop = 8;
		this.paddingBottom = 8;
		this.centerX = width / 2.0;
		this.centerY = height / 2.0;
		
		this.mapRegionProvider = MapDataProvider.getManager().getMapRegionProvider();
		WorldKey worldKey = MapDataProvider.getManager().getWorldKey();
		if (centerPos == null || !worldKey.equals(world)) {
			this.centerPos = DataUtil.currentPos();
			this.world = worldKey;
		} else if (playerTracking) {
			this.centerPos = DataUtil.currentPos();
		}
		this.cursorCoords = PosUtil.posToString(centerPos);
		this.chunkGrid = new ChunkGrid(centerPos.getX(), centerPos.getZ(), x, y, width, height, imageScale);

		this.updateScale();

		if (Dimension.isNether(world.getDimension())) {
			this.mapLayer = Layer.NETHER;
			this.mapLevel = DataUtil.coordY() / mapLayer.height;
		} else {
			this.mapLayer = Layer.SURFACE;
			this.mapLevel = 0;
		}
		
		this.waypoints.clear();
		List<Waypoint> wps = WaypointKeeper.getInstance().getWaypoints(world, true);
		if (wps != null) {
			Stream<Waypoint> stream = wps.stream().filter(wp -> MathUtil.getDistance(centerPos, wp.pos) <= wp.showRange);
			for (Waypoint wp : stream.toArray(Waypoint[]::new)) {
				WaypointIcon waypoint = new WaypointIcon(this, wp);
				this.waypoints.add(waypoint);
			}
		}
		this.players.clear();
		if (RuleUtil.allowPlayerRadar()) {
			List<AbstractClientPlayerEntity> players = this.client.world.getPlayers();
			for (PlayerEntity player : players) {
				if (player == client.player) continue;
				this.players.add(new PlayerIcon(player));
			}
		}
		
		this.addMapMenu();
		this.addMapButtons();
	}
	
	private void addMapMenu() {
		LangUtil langUtil = new LangUtil("gui.worldmap");
		this.mapMenu = this.addDrawableChild(new DropDownListWidget(25, paddingTop + 2, 100, 22));
		this.mapMenu.addElement(new ListElementWidget(langUtil.getText("add_waypoint"), () -> {
			JustMapClient.getMiniMap().createWaypoint(world, centerPos);
			return true;
		}));
		this.mapMenu.addElement(new ListElementWidget(langUtil.getText("set_map_pos"), () -> {
			client.setScreen(new MapPositionScreen(this));
			return true;
		}));
		this.mapMenu.addElement(new ListElementWidget(langUtil.getText("open_map_config"), () -> {
			client.setScreen(ConfigFactory.getConfigScreen(this));
			return true;
		}));
	}
	
	private void addMapButtons() {
		this.addDrawableChild(new ButtonWidget(width - 24, 10, 20, 20, new LiteralText("x"), (b) -> onClose()));
		this.addDrawableChild(new ButtonWidget(width / 2 - 10, height - paddingBottom - 44, 20, 20, new LiteralText("\u2191"), (b) -> moveMap(Direction.NORTH)));
		this.addDrawableChild(new ButtonWidget(width / 2 - 10, height - paddingBottom - 22, 20, 20, new LiteralText("\u2193"), (b) -> moveMap(Direction.SOUTH)));
		this.addDrawableChild(new ButtonWidget(width / 2 - 32, height - paddingBottom - 32, 20, 20, new LiteralText("\u2190"), (b) -> moveMap(Direction.WEST)));
		this.addDrawableChild(new ButtonWidget(width / 2 + 12, height - paddingBottom - 32, 20, 20, new LiteralText("\u2192"), (b) -> moveMap(Direction.EAST)));
		this.addDrawableChild(new ButtonWidget(width - 24, height / 2 - 21, 20, 20, new LiteralText("+"), (b) -> changeScale(-0.25F)));
		this.addDrawableChild(new ButtonWidget(width - 24, height / 2 + 1, 20, 20, new LiteralText("-"), (b) -> changeScale(+0.25F)));
		this.addDrawableChild(new ButtonWidget(width - 24, height - paddingBottom - 22, 20, 20, new LiteralText("\u271C"), (b) -> setCenterByPlayer()));
		this.addDrawableChild(new ButtonWidget(4, paddingTop + 2, 20, 20, new LiteralText("\u2630"), (b) -> mapMenu.toggleVisible()));
		this.addDrawableChild(new ButtonWidget(4, height - paddingBottom - 22, 20, 20, new LiteralText("\u2726"), (b) -> client.setScreen(new WaypointsListScreen(this))));
	}
	
	@Override
	public void renderBackground(MatrixStack matrixStack) {
		fill(matrixStack, x, 0, x + width, height, 0xFF444444);
		this.drawMap(matrixStack);
	}
	
	@Override
	public void renderForeground(MatrixStack matrices) {
		if (ClientSettings.showWorldmapGrid) {
			this.chunkGrid.draw();
		}
		int iconSize = (int) (ClientSettings.worldmapIconSize / imageScale);
		iconSize = iconSize % 2 != 0 ? iconSize + 1 : iconSize;
		iconSize = MathUtil.clamp(iconSize, 6, (int) (ClientSettings.worldmapIconSize * 1.2));
		for (WaypointIcon icon : waypoints) {
			icon.setPosition(
				MathUtil.screenPos(icon.waypoint.pos.getX(), centerPos.getX(), centerX, imageScale),
				MathUtil.screenPos(icon.waypoint.pos.getZ(), centerPos.getZ(), centerY, imageScale),
				icon.waypoint.pos.getY()
			);
			icon.draw(iconSize);
		}
		for (PlayerIcon icon : players) {
			icon.setPosition(
				MathUtil.screenPos(icon.getX(), centerPos.getX(), centerX, imageScale),
				MathUtil.screenPos(icon.getZ(), centerPos.getZ(), centerY, imageScale),
				(int) icon.getY()
			);
			icon.draw(matrices, iconSize);
		}
		
		ClientPlayerEntity player = client.player;
		
		double playerX = player.getX();
		double playerZ = player.getZ();
		double arrowX = MathUtil.screenPos(playerX, centerPos.getX(), centerX, imageScale);
		double arrowY = MathUtil.screenPos(playerZ, centerPos.getZ(), centerY, imageScale);
		
		MapPlayerManager.getPlayer(player).getIcon().draw(arrowX, arrowY, iconSize, true);
		
		this.drawBorders(paddingTop, paddingBottom);
		drawCenteredText(matrices, client.textRenderer, cursorCoords, width / 2, paddingTop + 4, Colors.WHITE);
	}
	
	private void drawMap(MatrixStack matrices) {
		int cornerX = centerPos.getX() - scaledWidth / 2;
		int cornerZ = centerPos.getZ() - scaledHeight / 2;
		
		int picX = 0, imgW = 0;
		while(picX < scaledWidth) {
			int cX = cornerX + picX;
			int picY = 0, imgH = 0;
			while (picY < scaledHeight) {				
				int cZ = cornerZ + picY;
				
				MapRegion region = mapRegionProvider.getMapRegion(this, cX, cZ);

				imgW = 512;
				imgH = 512;
				int imgX = cX - (region.getPos().x << 9);
				int imgY = cZ - (region.getPos().z << 9);
				
				if (picX + imgW >= scaledWidth) imgW = scaledWidth - picX;
				if (picY + imgH >= scaledHeight) imgH = scaledHeight - picY;
				if (imgX + imgW >= 512) imgW = 512 - imgX;
				if (imgY + imgH >= 512) imgH = 512 - imgY;
				
				double scX = picX / imageScale;
				double scY = picY / imageScale;
				double scW = imgW / imageScale;
				double scH = imgH / imageScale;
				
				RenderSystem.enableBlend();
				RenderSystem.defaultBlendFunc();
				region.drawLayer(matrices, mapLayer, mapLevel, scX, scY, scW, scH, imgX, imgY, imgW, imgH);

				picY += imgH > 0 ? imgH : 512;
			}
			
			picX += imgW > 0 ? imgW : 512;
		}
	}
	
	public void setCenterByPlayer() {
		this.playerTracking = true;
		this.centerPos = DataUtil.currentPos();
		this.chunkGrid.updateCenter(centerPos.getX(), centerPos.getZ());
		this.chunkGrid.updateGrid();
	}
	
	private void updateScale() {
		this.scaledWidth = (int) Math.ceil(width * imageScale);
		this.scaledHeight = (int) Math.ceil(height * imageScale);		
		if (scaledWidth > 2580) {
			this.imageScale = 2580F / width;
			this.updateScale();
			
			return;
		}
		this.chunkGrid.updateRange(x, y, width, height, imageScale);
		this.chunkGrid.updateGrid();
	}
	
	private void changeScale(float value) {
		this.imageScale = MathUtil.clamp(imageScale + value, 0.5F, 3F);
		this.updateScale();
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
		this.chunkGrid.updateCenter(centerPos.getX(), centerPos.getZ());
		this.chunkGrid.updateGrid();
		this.playerTracking = false;
	}
	
	@Override
	public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
		switch(keyCode) {
			case GLFW.GLFW_KEY_W:
			case GLFW.GLFW_KEY_UP:
				this.moveMap(Direction.NORTH);
		  		return true;
		  	case GLFW.GLFW_KEY_S:
		  	case GLFW.GLFW_KEY_DOWN:
		  		this.moveMap(Direction.SOUTH);
		  		return true;
		  	case GLFW.GLFW_KEY_A:
		  	case GLFW.GLFW_KEY_LEFT:
		  		this.moveMap(Direction.WEST);
		  		return true;
		  	case GLFW.GLFW_KEY_D:
		  	case GLFW.GLFW_KEY_RIGHT:
		  		this.moveMap(Direction.EAST);
		  		return true;
		  	case GLFW.GLFW_KEY_MINUS:
		  	case GLFW.GLFW_KEY_KP_SUBTRACT:
		  		this.changeScale(0.25F);
		  		return true;
		  	case GLFW.GLFW_KEY_EQUAL:
		  	case GLFW.GLFW_KEY_KP_ADD:
		  		this.changeScale(-0.25F);
		  		return true;
		  	case GLFW.GLFW_KEY_X:
		  		this.setCenterByPlayer();
		  		return true;
		  	case GLFW.GLFW_KEY_M:
		  		this.onClose();
		  		return true;
		  	default:
		  		return super.keyPressed(keyCode, scanCode, modifiers);
		}
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		if (super.mouseDragged(mouseX, mouseY, button, deltaX, deltaY)) return true;
		
		if (button == 0) {
			
			int x = centerPos.getX();
			int y = centerPos.getY();
			int z = centerPos.getZ();
			
			x -= Math.ceil(deltaX * imageScale);
			z -= Math.ceil(deltaY * imageScale);
			
			this.centerPos = new BlockPos(x, y, z);
			this.chunkGrid.updateCenter(x, z);
			this.chunkGrid.updateGrid();
			this.playerTracking = false;
		
			return true;
		}
		
		return false;
	}
	
	private int pixelToPos(double screenPos, double centerWorld, double centerScreen) {
		return (int) MathUtil.worldPos(screenPos, centerWorld, centerScreen, imageScale);
	}
	
	private BlockPos cursorBlockPos(double x, double y) {
		int posX = this.pixelToPos(x, centerPos.getX(), centerX);
		int posZ = this.pixelToPos(y, centerPos.getZ(), centerY);
		int posY = MapDataProvider.getManager().getMapHeight(mapLayer, mapLevel, posX, posZ);
		posY = posY == -1 ? centerPos.getY() : posY;

		return new BlockPos(posX, posY, posZ);
	}

	@Override
	public void mouseMoved(double d, double e) {		
		this.cursorCoords = PosUtil.posToString(cursorBlockPos(d, e));
	}
	
	private int clicks = 0;
	private long clicked = 0;
	
	@Override
	public boolean mouseReleased(double d, double e, int i) {
		if (super.mouseReleased(d, e, i)) return true; 
		
		if (i == 0) {
			long time = System.currentTimeMillis();
			if (time - clicked > 300) clicks = 0;
			
			if (++clicks == 2) {			
				JustMapClient.getMiniMap().createWaypoint(world, cursorBlockPos(d, e));
				
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
	public boolean isRotated() {
		return false;
	}
	
	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		boolean scrolled = super.mouseScrolled(d, e, f);
		this.changeScale(f > 0 ? -0.25F : 0.25F);
		return scrolled;
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
	public float getScale() {
		return this.imageScale;
	}

	@Override
	public Layer getLayer() {
		return this.mapLayer;
	}

	@Override
	public int getLevel() {
		return this.mapLevel;
	}

	@Override
	public BlockPos getCenter() {
		return this.centerPos;
	}
}
