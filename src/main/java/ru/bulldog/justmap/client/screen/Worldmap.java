package ru.bulldog.justmap.client.screen;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.client.config.ConfigFactory;
import ru.bulldog.justmap.client.widget.DropDownListWidget;
import ru.bulldog.justmap.client.widget.ListElementWidget;
import ru.bulldog.justmap.map.ChunkGrid;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.map.data.*;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.*;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.MathUtil;

import java.util.List;
import java.util.stream.Stream;

@SuppressWarnings("ConstantConditions")
public class Worldmap extends MapScreen implements IMap {

	private final static TextComponent TITLE = new TextComponent("Worldmap");
	
	private static Worldmap worldmap;
	
	public static Worldmap getScreen() {
		if (worldmap == null) {
			worldmap = new Worldmap();
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
	private WorldData worldData;
	private WorldKey world;
	private BlockPos centerPos;
	private String cursorCoords;
	private Layer mapLayer;
	private ChunkGrid chunkGrid;
	
	private final List<WaypointIcon> waypoints = Lists.newArrayList();
	private final List<PlayerIcon> players = Lists.newArrayList();
	
	private Worldmap() {
		super(TITLE);
	}

	@Override
	public void init() {		
		super.init();
		
		this.paddingTop = 8;
		this.paddingBottom = 8;
		this.centerX = width / 2.0;
		this.centerY = height / 2.0;
		
		this.worldData = WorldManager.getData();
		WorldKey worldKey = WorldManager.getWorldKey();
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
			List<AbstractClientPlayer> players = minecraft.level.players();
			for (Player player : players) {
				if (player == minecraft.player) continue;
				this.players.add(new PlayerIcon(player));
			}
		}
		
		this.addMapMenu();
		this.addMapButtons();
	}
	
	private void addMapMenu() {
		LangUtil langUtil = new LangUtil("gui.worldmap");
		this.mapMenu = addWidget(new DropDownListWidget(25, paddingTop + 2, 100, 22));
		this.mapMenu.addElement(new ListElementWidget(langUtil.getText("add_waypoint"), () -> {
			JustMapClient.getMap().createWaypoint(world, centerPos);
			return true;
		}));
		this.mapMenu.addElement(new ListElementWidget(langUtil.getText("set_map_pos"), () -> {
			minecraft.setScreen(new MapPositionScreen(this));
			return true;
		}));
		this.mapMenu.addElement(new ListElementWidget(langUtil.getText("open_map_config"), () -> {
			minecraft.setScreen(ConfigFactory.getConfigScreen(this));
			return true;
		}));
	}
	
	private void addMapButtons() {
		this.addWidget(new Button(width - 24, 10, 20, 20, new TextComponent("x"), (b) -> onClose()));
		this.addWidget(new Button(width / 2 - 10, height - paddingBottom - 44, 20, 20, new TextComponent("\u2191"), (b) -> moveMap(Direction.NORTH)));
		this.addWidget(new Button(width / 2 - 10, height - paddingBottom - 22, 20, 20, new TextComponent("\u2193"), (b) -> moveMap(Direction.SOUTH)));
		this.addWidget(new Button(width / 2 - 32, height - paddingBottom - 32, 20, 20, new TextComponent("\u2190"), (b) -> moveMap(Direction.WEST)));
		this.addWidget(new Button(width / 2 + 12, height - paddingBottom - 32, 20, 20, new TextComponent("\u2192"), (b) -> moveMap(Direction.EAST)));
		this.addWidget(new Button(width - 24, height / 2 - 21, 20, 20, new TextComponent("+"), (b) -> changeScale(-0.25F)));
		this.addWidget(new Button(width - 24, height / 2 + 1, 20, 20, new TextComponent("-"), (b) -> changeScale(+0.25F)));
		this.addWidget(new Button(width - 24, height - paddingBottom - 22, 20, 20, new TextComponent("\u271C"), (b) -> setCenterByPlayer()));
		this.addWidget(new Button(4, paddingTop + 2, 20, 20, new TextComponent("\u2630"), (b) -> mapMenu.toggleVisible()));
		this.addWidget(new Button(4, height - paddingBottom - 22, 20, 20, new TextComponent("\u2726"), (b) -> minecraft.setScreen(new WaypointsList(this))));
	}
	
	@Override
	public void renderBackground(PoseStack matrixStack) {
		fill(matrixStack, x, 0, x + width, height, 0xFF444444);
		this.drawMap();
	}
	
	@Override
	public void renderForeground(PoseStack matrices) {
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
		
		LocalPlayer player = minecraft.player;
		
		double playerX = player.getX();
		double playerZ = player.getZ();
		double arrowX = MathUtil.screenPos(playerX, centerPos.getX(), centerX, imageScale);
		double arrowY = MathUtil.screenPos(playerZ, centerPos.getZ(), centerY, imageScale);
		
		MapPlayerManager.getPlayer(player).getIcon().draw(arrowX, arrowY, iconSize, true);
		
		this.drawBorders(paddingTop, paddingBottom);
		drawCenteredString(matrices, minecraft.font, cursorCoords, width / 2, paddingTop + 4, Colors.WHITE);
	}
	
	private void drawMap() {		
		int cornerX = centerPos.getX() - scaledWidth / 2;
		int cornerZ = centerPos.getZ() - scaledHeight / 2;
		
		BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
		
		int picX = 0, imgW = 0;
		while(picX < scaledWidth) {
			int cX = cornerX + picX;
			int picY = 0, imgH;
			while (picY < scaledHeight) {				
				int cZ = cornerZ + picY;
				
				RegionData region = worldData.getRegion(this, currentPos.set(cX, 0, cZ));
				region.swapLayer(mapLayer, mapLevel);
				
				imgW = 512;
				imgH = 512;
				int imgX = cX - (region.getX() << 9);
				int imgY = cZ - (region.getZ() << 9);
				
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
				region.draw(scX, scY, scW, scH, imgX, imgY, imgW, imgH);
				
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
				this.centerPos = centerPos.offset(0, 0, -16);
				break;
			case SOUTH:
				this.centerPos = centerPos.offset(0, 0, 16);
				break;
			case EAST:
				this.centerPos = centerPos.offset(16, 0, 0);
				break;
			case WEST:
				this.centerPos = centerPos.offset(-16, 0, 0);
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
		
		int chunkX = posX >> 4;
		int chunkZ = posZ >> 4;
		
		ChunkData mapChunk = this.worldData.getChunk(chunkX, chunkZ);
		
		int cx = posX - (chunkX << 4);
		int cz = posZ - (chunkZ << 4);
		
		int posY = mapChunk.getChunkLevel(mapLayer, mapLevel).sampleHeightmap(cx, cz);
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
				JustMapClient.getMap().createWaypoint(world, cursorBlockPos(d, e));
				
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
