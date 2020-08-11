package ru.bulldog.justmap.map.minimap;

import ru.bulldog.justmap.advancedinfo.AdvancedInfo;
import ru.bulldog.justmap.advancedinfo.BiomeInfo;
import ru.bulldog.justmap.advancedinfo.CoordsInfo;
import ru.bulldog.justmap.advancedinfo.InfoText;
import ru.bulldog.justmap.advancedinfo.TextManager;
import ru.bulldog.justmap.advancedinfo.TimeInfo;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.MapRenderer;
import ru.bulldog.justmap.client.screen.WaypointEditor;
import ru.bulldog.justmap.enums.MapShape;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.enums.TextPosition;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.WorldData;
import ru.bulldog.justmap.map.data.WorldKey;
import ru.bulldog.justmap.map.data.WorldManager;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.icon.EntityIcon;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.RandomUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Minimap implements IMap {
	private static final MinecraftClient minecraft = DataUtil.getMinecraft();

	private final TextManager textManager;
	private final MapRenderer mapRenderer;
	private InfoText txtCoords = new CoordsInfo(TextAlignment.CENTER, "0, 0, 0");
	private InfoText txtBiome = new BiomeInfo(TextAlignment.CENTER, "");
	private InfoText txtTime = new TimeInfo(TextAlignment.CENTER, "");
	private List<MapIcon<?>> drawedIcons = new ArrayList<>();
	private PlayerEntity locPlayer = null;
	private Layer mapLayer = Layer.SURFACE;
	private WorldData worldData;
	private MapSkin mapSkin;
	private World world;
	private ScreenPosition mapPosition;
	private boolean isMapVisible = true;
	private boolean rotateMap = false;
	private boolean bigMap = false;
	private float mapScale;
	private int skinX, skinY;
	private int mapX, mapY;
	private int offset;
	private int border;
	private int mapLevel;
	private int mapWidth;
	private int mapHeight;
	private int scaledWidth;
	private int scaledHeight;
	private int lastPosX;
	private int lastPosZ;

	public boolean posChanged = false;

	public Minimap() {
		this.textManager = AdvancedInfo.getMapTextManager();
		this.mapRenderer = new MapRenderer(this);
		this.textManager.add(txtCoords);
		this.textManager.add(txtBiome);
		this.textManager.add(txtTime);
	}

	public void update() {
		if (!this.isMapVisible()) {
			return;
		}

		PlayerEntity player = minecraft.player;
		if (player != null) {
			if (locPlayer == null) {
				locPlayer = player;
			}

			this.prepareMap(player);
			this.updateInfo(player);
		} else {
			locPlayer = null;
		}
	}

	public void updateMapParams() {
		ClientConfig config = JustMapClient.CONFIG;
		this.isMapVisible = config.getBoolean("map_visible");
		
		if (!isMapVisible) return;
		
		int configSize = config.getInt("map_size");
		float configScale = config.getFloat("map_scale");
		boolean needRotate = config.getBoolean("rotate_map");
		boolean bigMap = config.getBoolean("show_big_map");

		Window window = minecraft.getWindow();
		if (window != null) {
			int winWidth = window.getScaledWidth();
			int guiScale = minecraft.options.guiScale;
			double winScale = window.getScaleFactor();
			if (guiScale == 0 && winScale > 2) {
				configSize *= configSize / (winWidth / winScale);
			}
		}

		if (configSize != mapWidth || configScale != mapScale ||
			rotateMap != needRotate || this.bigMap != bigMap) {
			if (bigMap) {
				this.mapWidth = config.getInt("big_map_size");
				this.mapHeight = (mapWidth * 10) / 16;
			} else {
				this.mapWidth = configSize;
				this.mapHeight = configSize;
			}
			this.mapScale = configScale;
			this.rotateMap = needRotate;
			this.bigMap = bigMap;

			if (rotateMap) {
				double mult = (bigMap) ? 1.8 : 1.42;
				this.scaledWidth = (int) ((mapWidth * mapScale) * mult + 8);
				this.scaledHeight = (int) ((mapHeight * mapScale) * mult + 8);
			} else {
				this.scaledWidth = (int) ((mapWidth * mapScale) + 8);
				this.scaledHeight = (int) ((mapHeight * mapScale) + 8);
			}

			this.textManager.setLineWidth(this.mapWidth);
		}
		
		this.border = 0;
		if (ClientParams.useSkins) {
			if (isBigMap()) {
				this.mapSkin = MapSkin.getBigMapSkin();
			} else {
				this.mapSkin = MapSkin.getCurrentSkin();
			}
			if (isRound()) {
				double scale = (double) mapWidth / mapSkin.getWidth();
				this.mapSkin.getRenderData().updateScale(scale);
				this.border = (int) (mapSkin.border * scale);
			} else {
				this.mapSkin.getRenderData().updateScale();
				double scale = mapSkin.getRenderData().scaleFactor;
				this.border = (int) (mapSkin.border * scale);
			}
		} else {
			this.mapSkin = null;
		}
		this.updateMapPosition();
	}
	
	private void updateMapPosition() {
		Window window = minecraft.getWindow();
		int winW = window.getScaledWidth();
		int winH = window.getScaledHeight();
		this.offset = ClientParams.positionOffset;
		this.mapPosition = ClientParams.mapPosition;		
		
		TextPosition textPos = TextPosition.UNDER;

		int fullWidth = mapWidth + border * 2;
		int fullHeight = mapHeight + border * 2;
		switch (mapPosition) {
			case USER_DEFINED:
				this.skinX = ClientParams.mapPositionX;
				this.skinY = ClientParams.mapPositionY;
				break;
			case TOP_CENTER:
				this.skinX = winW / 2 - fullWidth / 2;
				this.skinY = offset;
				break;
			case TOP_RIGHT:
				this.skinX = winW - offset - fullWidth;
				this.skinY = offset;
				break;
			case MIDDLE_RIGHT:
				this.skinX = winW - offset - fullWidth;
				this.skinY = winH / 2 - fullHeight / 2;
				break;
			case MIDDLE_LEFT:
				this.skinX = offset;
				this.skinY = winH / 2 - fullHeight / 2;
				break;
			case BOTTOM_LEFT:
				textPos = TextPosition.ABOVE;
				this.skinX = offset;
				this.skinY = winH - offset - fullHeight;
				break;
			case BOTTOM_RIGHT:
				textPos = TextPosition.ABOVE;
				this.skinX = winW - offset - fullWidth;
				this.skinY = winH - offset - fullHeight;
				break;
			default:
				this.skinX = offset;
				this.skinY = offset;
		}
		
		int limitW = winW - fullWidth - offset;
		if (skinX < offset) {
			this.skinX = offset;
		} else if (skinX > limitW) {
			skinX = limitW;
		}
		int limitH = winH - fullHeight - offset;
		if (skinY < offset) {
			this.skinY = offset;
		} else if (skinY > limitH) {
			skinY = limitH;
		}
		
		this.mapX = skinX + border;
		this.mapY = skinY + border;	
		
		this.textManager.updatePosition(textPos,
				mapX, (textPos == TextPosition.UNDER ?
						skinY + fullHeight + 3 :
							skinY - 12)
		);
	}

	private void updateInfo(PlayerEntity player) {
		if (!ClientParams.mapInfo) {
			this.txtCoords.setVisible(false);
			this.txtBiome.setVisible(false);
			this.txtTime.setVisible(false);

			return;
		}
		this.txtCoords.setVisible(ClientParams.showPosition);
		if (ClientParams.showPosition) {
			this.txtCoords.update();
		}
		boolean showBiome = !ClientParams.advancedInfo && ClientParams.showBiome;
		boolean showTime = !ClientParams.advancedInfo && ClientParams.showTime;
		this.txtBiome.setVisible(showBiome);
		this.txtTime.setVisible(showTime);
		if (showBiome)
			this.txtBiome.update();
		if (showTime)
			this.txtTime.update();
	}

	public void prepareMap(PlayerEntity player) {
		this.world = player.world;
		this.worldData = WorldManager.getData();
		BlockPos pos = DataUtil.currentPos();

		int posX = pos.getX();
		int posZ = pos.getZ();
		int posY = pos.getY();
		int scaledW = scaledWidth;
		int scaledH = scaledHeight;
		double startX = posX - scaledW / 2;
		double startZ = posZ - scaledH / 2;

		if (Dimension.isNether(world)) {
			this.mapLayer = Layer.NETHER;
			this.mapLevel = posY / mapLayer.height;
		} else if (RuleUtil.needRenderCaves(world, pos)) {
			this.mapLayer = Layer.CAVES;
			this.mapLevel = posY / mapLayer.height;
		} else {
			this.mapLayer = Layer.SURFACE;
			this.mapLevel = 0;
		}

		if (lastPosX != posX || lastPosZ != posZ) {
			this.lastPosX = posX;
			this.lastPosZ = posZ;
			this.posChanged = true;
		}

		if (ClientParams.rotateMap) {
			scaledW = (int) (mapWidth * mapScale);
			scaledH = (int) (mapHeight * mapScale);
			startX = posX - scaledW / 2;
			startZ = posZ - scaledH / 2;
		}

		double endX = startX + scaledW;
		double endZ = startZ + scaledH;

		this.drawedIcons.clear();
		if (RuleUtil.allowEntityRadar()) {
			int checkHeight = 24;
			BlockPos start = new BlockPos(startX, posY - checkHeight / 2, startZ);
			BlockPos end = new BlockPos(endX, posY + checkHeight / 2, endZ);
			List<Entity> entities = world.getOtherEntities(player, new Box(start, end));
		
			int amount = 0;				
			for (Entity entity : entities) {
				float tick = minecraft.getTickDelta();
				double entX = entity.prevX + (entity.getX() - entity.prevX) * tick;
				double entZ = entity.prevZ + (entity.getZ() - entity.prevZ) * tick;
				double iconX = MathUtil.screenPos(entX, startX, endX, mapWidth);
				double iconY = MathUtil.screenPos(entZ, startZ, endZ, mapHeight);
				if (entity instanceof PlayerEntity && RuleUtil.allowPlayerRadar()) {
					PlayerEntity pEntity = (PlayerEntity) entity;
					if (pEntity == player) continue;
					PlayerIcon playerIcon = new PlayerIcon(this, pEntity);
					playerIcon.setPosition(iconX, iconY);
					this.drawedIcons.add(playerIcon);
				} else if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
					LivingEntity livingEntity = (LivingEntity) entity;
					boolean hostile = livingEntity instanceof HostileEntity;
					if (hostile && RuleUtil.allowHostileRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);
						entIcon.setPosition(iconX, iconY);
						this.drawedIcons.add(entIcon);
						amount++;
					} else if (!hostile && RuleUtil.allowCreatureRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);
						entIcon.setPosition(iconX, iconY);
						this.drawedIcons.add(entIcon);
						amount++;
					}
				}
				if (amount >= 250) break;
			}
		}
		if (ClientParams.showWaypoints) {
			List<Waypoint> wps = WaypointKeeper.getInstance().getWaypoints(WorldManager.getWorldKey(), true);
			if (wps != null) {
				Stream<Waypoint> stream = wps.stream()
						.filter(wp -> MathUtil.getDistance(pos, wp.pos, false) <= wp.showRange);
				for (Waypoint wp : stream.toArray(Waypoint[]::new)) {
					WaypointIcon waypoint = new WaypointIcon(this, wp);
					waypoint.setPosition(MathUtil.screenPos(wp.pos.getX(), startX, endX, mapWidth),
										 MathUtil.screenPos(wp.pos.getZ(), startZ, endZ, mapHeight));
					this.drawedIcons.add(waypoint);
				}
			}
		}
	}

	public void createWaypoint(WorldKey world, BlockPos pos) {
		Waypoint waypoint = new Waypoint();
		waypoint.world = world;
		waypoint.name = "Waypoint";
		waypoint.color = RandomUtil.getElement(Waypoint.WAYPOINT_COLORS);
		waypoint.pos = pos;

		minecraft.openScreen(
				new WaypointEditor(waypoint, minecraft.currentScreen, WaypointKeeper.getInstance()::addNew));
	}

	public void createWaypoint() {
		this.createWaypoint(WorldManager.getWorldKey(), DataUtil.currentPos());
	}
	
	public MapRenderer getRenderer() {
		return this.mapRenderer;
	}

	public World getWorld() {
		return this.world;
	}

	public WorldData getWorldData() {
		return this.worldData;
	}
	
	public MapSkin getSkin() {
		return this.mapSkin;
	}

	public float getScale() {
		return this.mapScale;
	}

	public List<MapIcon<?>> getDrawedIcons() {
		return this.drawedIcons;
	}

	public TextManager getTextManager() {
		return this.textManager;
	}

	public boolean isBigMap() {
		return this.bigMap;
	}

	public static boolean isBig() {
		return ClientParams.showBigMap;
	}

	public static boolean isRound() {
		return !isBig() && (ClientParams.mapShape == MapShape.CIRCLE);
	}

	public boolean isMapVisible() {
		if (minecraft.currentScreen != null) {
			return this.isMapVisible && !minecraft.isPaused() && ClientParams.showInChat
					&& minecraft.currentScreen instanceof ChatScreen;
		}

		return this.isMapVisible;
	}
	
	public int getOffset() {
		return this.offset;
	}
	
	public int getMapX() {
		return this.mapX;
	}
	
	public int getMapY() {
		return this.mapY;
	}
	
	public int getSkinX() {
		return this.skinX;
	}
	
	public int getSkinY() {
		return this.skinY;
	}

	public int getLasX() {
		return this.lastPosX;
	}

	public int getLastZ() {
		return this.lastPosZ;
	}
	
	public int getBorder() {
		return this.border;
	}
	
	@Override
	public boolean isRotated() {
		return ClientParams.rotateMap;
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
	public int getWidth() {
		return this.mapWidth;
	}

	@Override
	public int getHeight() {
		return this.mapHeight;
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
	public BlockPos getCenter() {
		return DataUtil.currentPos();
	}
}
