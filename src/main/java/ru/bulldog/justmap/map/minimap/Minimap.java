package ru.bulldog.justmap.map.minimap;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.util.Window;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.mob.MobEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.world.World;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.AdvancedInfo;
import ru.bulldog.justmap.advancedinfo.BiomeInfo;
import ru.bulldog.justmap.advancedinfo.CoordsInfo;
import ru.bulldog.justmap.advancedinfo.InfoText;
import ru.bulldog.justmap.advancedinfo.TextManager;
import ru.bulldog.justmap.advancedinfo.TimeInfo;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientConfig;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.client.render.AbstractMiniMapRenderer;
import ru.bulldog.justmap.client.render.BufferedMiniMapRenderer;
import ru.bulldog.justmap.client.render.FastMiniMapRenderer;
import ru.bulldog.justmap.client.screen.WaypointEditorScreen;
import ru.bulldog.justmap.enums.MapShape;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.enums.TextPosition;
import ru.bulldog.justmap.map.EntityRadar;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.map.data.MapDataProvider;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.multiworld.WorldKey;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.RandomUtil;
import ru.bulldog.justmap.util.render.ExtendedFramebuffer;

public class Minimap implements IMap {
	private static final TextManager textManager;
	private static final InfoText txtCoords = new CoordsInfo(TextAlignment.CENTER, "0, 0, 0");
	private static final InfoText txtBiome = new BiomeInfo(TextAlignment.CENTER, "");
	private static final InfoText txtTime = new TimeInfo(TextAlignment.CENTER, "");

	private final MinecraftClient minecraft;
	private final FastMiniMapRenderer fastRenderer;
	private final BufferedMiniMapRenderer bufferedRenderer;
	private final List<WaypointIcon> waypoints = new ArrayList<>();
	private PlayerEntity locPlayer = null;
	private Layer mapLayer = Layer.SURFACE;
	private final EntityRadar entityRadar;
	private MapRegionProvider mapRegionProvider;
	private MapSkin mapSkin;
	private World world;
	private boolean isMapVisible = true;
	private boolean rotateMap = false;
	private boolean bigMap = false;
	private double winScale;
	private float mapScale;
	private int lastPosX;
	private int lastPosZ;
	private int skinX, skinY;
	private int mapX, mapY;
	private int offset;
	private int border;
	private int mapLevel;
	private int mapWidth;
	private int mapHeight;
	private int scaledWidth;
	private int scaledHeight;

	public Minimap() {
		this.minecraft = MinecraftClient.getInstance();
		this.entityRadar = new EntityRadar();
		this.fastRenderer = new FastMiniMapRenderer(this);
		this.bufferedRenderer = new BufferedMiniMapRenderer(this);
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

		Window window = minecraft.getWindow();
		if (window != null) {
			double scale = window.getScaleFactor();
			if (winScale != scale) {
				winScale = scale;
				this.updateMapParams();
			}
		}
	}

	public void updateMapParams() {
		ClientConfig config = JustMapClient.getConfig();
		this.isMapVisible = config.getBoolean("map_visible");

		if (!isMapVisible) return;

		int configSize = config.getInt("map_size");
		float configScale = config.getMapScale();
		boolean needRotate = config.getBoolean("rotate_map");
		boolean bigMap = config.getBoolean("show_big_map");

		if (configSize != mapWidth || configScale != mapScale ||
			rotateMap != needRotate || this.bigMap != bigMap) {
			if (bigMap) {
				this.mapWidth = config.getInt("big_map_size");
				this.mapHeight = (int) (mapWidth * 0.625);
			} else {
				this.mapWidth = configSize;
				this.mapHeight = configSize;
			}
			this.mapScale = configScale;
			this.rotateMap = needRotate;
			this.bigMap = bigMap;

			if (rotateMap) {
				double mult = (bigMap) ? MathUtil.BIG_SQRT2 : MathUtil.SQRT2;
				this.scaledWidth = (int) (mapWidth * mapScale * mult);
				this.scaledHeight = (int) (mapHeight * mapScale * mult);
			} else {
				this.scaledWidth = (int) (mapWidth * mapScale);
				this.scaledHeight = (int) (mapHeight * mapScale);
			}

			textManager.setLineWidth(mapWidth);
		}

		this.border = 0;
		if (ClientSettings.useSkins) {
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

		if (!ClientSettings.fastRender) {
			try {
				if (ExtendedFramebuffer.canUseFramebuffer()) {
					if (!bufferedRenderer.isFBOTried()) {
						this.bufferedRenderer.loadFrameBuffers();
					}
				} else if (bufferedRenderer.isFBOLoaded()) {
					this.bufferedRenderer.deleteFramebuffers();
				}
			} catch (RuntimeException ex) {
				JustMap.LOGGER.error("Failed to load framebuffers!", ex);
			}
		} else if (bufferedRenderer.isFBOLoaded()) {
			this.bufferedRenderer.deleteFramebuffers();
		}
	}

	private void updateMapPosition() {
		Window window = minecraft.getWindow();
		int winW = window.getScaledWidth();
		int winH = window.getScaledHeight();
		this.offset = ClientSettings.positionOffset;
		ScreenPosition mapPosition = ClientSettings.mapPosition;

		TextPosition textPos = TextPosition.UNDER;

		int fullWidth = mapWidth + border * 2;
		int fullHeight = mapHeight + border * 2;
		switch (mapPosition) {
			case USER_DEFINED:
				this.skinX = ClientSettings.mapPositionX;
				this.skinY = ClientSettings.mapPositionY;
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

		textManager.updatePosition(textPos,
				mapX, (textPos == TextPosition.UNDER ?
						skinY + fullHeight + 3 :
							skinY - 12)
		);
	}

	private void updateInfo(PlayerEntity player) {
		if (!ClientSettings.mapInfo) {
			txtCoords.setVisible(false);
			txtBiome.setVisible(false);
			txtTime.setVisible(false);

			return;
		}
		txtCoords.setVisible(ClientSettings.showPosition);
		if (ClientSettings.showPosition) {
			txtCoords.update();
		}
		boolean showBiome = !ClientSettings.advancedInfo && ClientSettings.showBiome;
		boolean showTime = !ClientSettings.advancedInfo && ClientSettings.showTime;
		txtBiome.setVisible(showBiome);
		txtTime.setVisible(showTime);
		if (showBiome)
			txtBiome.update();
		if (showTime)
			txtTime.update();
	}

	public void prepareMap(PlayerEntity player) {
		this.world = player.world;
		this.mapRegionProvider = MapDataProvider.getManager().getMapRegionProvider();
		BlockPos pos = DataUtil.currentPos();

		int posX = pos.getX();
		int posZ = pos.getZ();
		int posY = pos.getY();

		if (lastPosX != posX || lastPosZ != posZ) {
			this.lastPosX = posX;
			this.lastPosZ = posZ;
		}

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

		int scaledW = scaledWidth;
		int scaledH = scaledHeight;
		double startX = posX - scaledW / 2.0;
		double startZ = posZ - scaledH / 2.0;
		if (ClientSettings.rotateMap) {
			scaledW = (int) (mapWidth * mapScale);
			scaledH = (int) (mapHeight * mapScale);
			startX = posX - scaledW / 2.0;
			startZ = posZ - scaledH / 2.0;
		}
		double endX = startX + scaledW;
		double endZ = startZ + scaledH;

		int radius = (int) (posX - startX);
		this.entityRadar.clear(pos, radius);
		if (RuleUtil.allowEntityRadar()) {
			int checkHeight = 24;
			BlockPos start = new BlockPos(startX, posY - checkHeight / 2, startZ);
			BlockPos end = new BlockPos(endX, posY + checkHeight / 2, endZ);
			List<Entity> entities = world.getOtherEntities(player, new Box(start, end));

			int amount = 0;
			for (Entity entity : entities) {
				if (entity instanceof PlayerEntity && RuleUtil.allowPlayerRadar()) {
					PlayerEntity pEntity = (PlayerEntity) entity;
					if (pEntity.isMainPlayer()) continue;
					this.entityRadar.addPlayer(pEntity);
				} else if (entity instanceof MobEntity) {
					MobEntity mobEntity = (MobEntity) entity;
					boolean hostile = mobEntity instanceof HostileEntity;
					if (hostile && RuleUtil.allowHostileRadar()) {
						this.entityRadar.addCreature(mobEntity);
						amount++;
					} else if (!hostile && RuleUtil.allowCreatureRadar()) {
						this.entityRadar.addCreature(mobEntity);
						amount++;
					}
				}
				if (amount >= 250) break;
			}
		} else {
			this.entityRadar.clearAll();
		}
	}

	public void createWaypoint(WorldKey world, BlockPos pos) {
		Waypoint waypoint = new Waypoint();
		waypoint.world = world;
		waypoint.name = "Waypoint";
		waypoint.color = RandomUtil.getElement(Waypoint.WAYPOINT_COLORS);
		waypoint.pos = pos;

		minecraft.setScreen(
				new WaypointEditorScreen(waypoint, minecraft.currentScreen, WaypointKeeper.getInstance()::addNew));
	}

	public void createWaypoint() {
		this.createWaypoint(MapDataProvider.getMultiworldManager().getCurrentWorldKey(), DataUtil.currentPos());
	}

	public AbstractMiniMapRenderer getRenderer() {
		if (bufferedRenderer.isFBOLoaded()) {
			return this.bufferedRenderer;
		}
		return this.fastRenderer;
	}

	public World getWorld() {
		return this.world;
	}

	public MapRegionProvider getMapRegionProvider() {
		return this.mapRegionProvider;
	}

	public MapSkin getSkin() {
		return this.mapSkin;
	}

	@Override
	public float getScale() {
		return this.mapScale;
	}

	public List<MapIcon<?>> getDrawableIcons(double worldX, double worldZ, double screenX, double screenY, float delta) {
		return this.entityRadar.getDrawableIcons(worldX, worldZ, screenX, screenY, mapScale, delta);
	}

	public List<WaypointIcon> getWaypoints(BlockPos currentPos, int screenX, int screenY) {
		this.waypoints.clear();
		if (ClientSettings.showWaypoints) {
			List<Waypoint> wps = WaypointKeeper.getInstance().getWaypoints(MapDataProvider.getMultiworldManager().getCurrentWorldKey(), true);
			if (wps != null) {
				Stream<Waypoint> stream = wps.stream()
						.filter(wp -> MathUtil.getDistance(currentPos, wp.pos, false) <= wp.showRange);
				for (Waypoint wp : stream.toArray(Waypoint[]::new)) {
					WaypointIcon waypoint = new WaypointIcon(this, wp);
					waypoint.setPosition(MathUtil.screenPos(wp.pos.getX(), currentPos.getX(), screenX, mapScale),
										 MathUtil.screenPos(wp.pos.getZ(), currentPos.getZ(), screenY, mapScale),
										 wp.pos.getY());
					this.waypoints.add(waypoint);
				}
			}
		}
		return this.waypoints;
	}

	public TextManager getTextManager() {
		return textManager;
	}

	public boolean isBigMap() {
		return this.bigMap;
	}

	public static boolean isBig() {
		return ClientSettings.showBigMap;
	}

	public static boolean isRound() {
		return !isBig() && (ClientSettings.mapShape == MapShape.CIRCLE);
	}

	public boolean isMapVisible() {
		if (minecraft == null) return false;
		if (minecraft.currentScreen != null) {
			return this.isMapVisible && !minecraft.isPaused() && ClientSettings.showInChat
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

	public int getLastX() {
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
		return ClientSettings.rotateMap;
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

	public int getScaledWidth() {
		return this.scaledWidth;
	}

	public int getScaledHeight() {
		return this.scaledHeight;
	}

	@Override
	public BlockPos getCenter() {
		return DataUtil.currentPos();
	}

	static {
		textManager = AdvancedInfo.getMapTextManager();
		textManager.add(txtCoords);
		textManager.add(txtBiome);
		textManager.add(txtTime);
	}
}
