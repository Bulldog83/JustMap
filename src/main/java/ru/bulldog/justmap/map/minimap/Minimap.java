package ru.bulldog.justmap.map.minimap;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.Layer.Type;
import ru.bulldog.justmap.map.data.MapCache;
import ru.bulldog.justmap.map.icon.EntityIcon;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointEditor;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import ru.bulldog.justmap.util.PosUtil;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.RandomUtil;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.GameRules;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.dimension.DimensionType;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

public class Minimap implements IMap{
	
	private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();
	
	private final TextManager textManager;
	
	private MapText txtCoords = new MapText(TextAlignment.CENTER, "0, 0, 0");
	private MapText txtBiome = new MapText(TextAlignment.CENTER, "Void");
	private MapText txtTime = new MapText(TextAlignment.CENTER, "00:00");
	private MapText txtFPS = new MapText(TextAlignment.CENTER, "00 fps");
	
	private int mapWidth;
	private int mapHeight;
	private int scaledWidth;
	private int scaledHeight;
	private float mapScale;
	private int lastPosX = 0;
	private int lastPosZ = 0;
	
	private Biome currentBiome;	
	
	private List<WaypointIcon> waypoints = new ArrayList<>();
	private List<PlayerIcon> players = new ArrayList<>();
	private List<EntityIcon> entities = new ArrayList<>();	
	private PlayerEntity locPlayer = null;
	
	private boolean isMapVisible = true;
	private boolean rotateMap = false;
	private boolean bigMap = false;

	public boolean posChanged = false;
	
	public Minimap() {
		this.textManager = new TextManager(this);
		this.updateMapParams();
	}
	
	public void update() {
		if (!this.isMapVisible()) { return; }
	
		PlayerEntity player = minecraftClient.player;
		if (player != null) {
			if (locPlayer == null) {
				locPlayer = player;
			}

			prepareMap(player);
			updateInfo(player);
		} else {
			locPlayer = null;
		}
	}
	
	public void updateMapParams() {
		int configSize = JustMapClient.CONFIG.getInt("map_size");
		float configScale = JustMapClient.CONFIG.getFloat("map_scale");		
		boolean needRotate = JustMapClient.CONFIG.getBoolean("rotate_map");
		boolean bigMap = JustMapClient.CONFIG.getBoolean("show_big_map");
		
		if (configSize != mapWidth || configScale != mapScale ||
			this.rotateMap != needRotate || this.bigMap != bigMap) {
			
			if (bigMap) {
				this.mapWidth = JustMapClient.CONFIG.getInt("big_map_size");
				this.mapHeight = (mapWidth * 10) / 16;
			} else {
				this.mapWidth = configSize;
				this.mapHeight = configSize;				
			}
			this.mapScale = configScale;
			this.rotateMap = needRotate;
			this.bigMap = bigMap;
			
			if (rotateMap) {
				this.scaledWidth = (int) ((mapWidth * mapScale) * 1.42 + 8);
				this.scaledHeight = (int) ((mapHeight * mapScale) * 1.42 + 8);
			} else {
				this.scaledWidth = (int) ((mapWidth * mapScale) + 8);
				this.scaledHeight = (int) ((mapHeight * mapScale) + 8);
			}
		}
		
		this.isMapVisible = JustMapClient.CONFIG.getBoolean("map_visible");
	}
	
	private void updateInfo(PlayerEntity player) {
		textManager.clear();
		
		if (ClientParams.showPosition) {
			Entity camera = minecraftClient.cameraEntity;
			txtCoords.setText(PosUtil.posToString(camera.getX(), camera.getY(), camera.getZ()));
			textManager.add(txtCoords);
		}		
		if (ClientParams.showBiome) {
			txtBiome.setText(I18n.translate(currentBiome.getTranslationKey()));
			textManager.add(txtBiome);
		}		
		if (ClientParams.showFPS) {
			txtFPS.setText(minecraftClient.fpsDebugString.substring(0, minecraftClient.fpsDebugString.indexOf("fps") + 3));
			textManager.add(txtFPS);
		}		
		if (ClientParams.showTime) {
			txtTime.setText(this.timeString(minecraftClient.world.getTimeOfDay()));
			textManager.add(txtTime);
		}
	}
	
	private String timeString(long time) {
		time = time > 24000 ? time % 24000 : time;
	
		int h = (int) time / 1000 + 6;
		int m = (int) (((time % 1000) / 1000.0F) * 60);
		
		h = h >= 24 ? h - 24 : h;
	
		return String.format("%02d:%02d", h, m);
	}
	
	private static boolean isAllowed(boolean param, GameRules.Key<GameRules.BooleanRule> rule) {
		if (param) {
			return minecraftClient.isInSingleplayer() || MapGameRules.isAllowed(rule);
		}
		
		return false;
	}
	
	private boolean needRenderCaves(World world, BlockPos playerPos) {
		boolean allowCaves = isAllowed(ClientParams.drawCaves, MapGameRules.ALLOW_CAVES_MAP);
		
		DimensionType dimType = world.getDimension();
		RegistryKey<DimensionType> dimKey = world.getDimensionRegistryKey();
		if (Dimension.isEnd(dimKey)) {
			return false;
		}
		if (!dimType.hasCeiling() && dimType.hasSkyLight()) {
			return allowCaves && (!world.isSkyVisibleAllowingSea(playerPos) &&
				   world.getLightLevel(LightType.SKY, playerPos) == 0 ||
				   dimKey == DimensionType.OVERWORLD_CAVES_REGISTRY_KEY);
		}
		
		return allowCaves;
	}
	
	public static boolean allowEntityRadar() {
		return isAllowed(ClientParams.showEntities, MapGameRules.ALLOW_ENTITY_RADAR);
	}
	
	public static boolean allowHostileRadar() {
		return isAllowed(ClientParams.showHostile, MapGameRules.ALLOW_HOSTILE_RADAR);
	}
	
	public static boolean allowCreatureRadar() {
		return isAllowed(ClientParams.showCreatures, MapGameRules.ALLOW_CREATURE_RADAR);
	}
	
	public static boolean allowPlayerRadar() {
		return isAllowed(ClientParams.showPlayers, MapGameRules.ALLOW_PLAYER_RADAR);
	}
	
	public void prepareMap(PlayerEntity player) {
		World world = player.world;
		BlockPos pos = PosUtil.currentPos();
		
		currentBiome = world.getBiome(pos);
		
		int posX = pos.getX();
		int posZ = pos.getZ();
		int posY = pos.getY();
		int scaledW = this.scaledWidth;
		int scaledH = this.scaledHeight;
		double startX = posX - scaledW / 2;
		double startZ = posZ - scaledH / 2;

		if (Dimension.isNether(world.getDimensionRegistryKey())) {
			MapCache.setCurrentLayer(Type.NETHER, posY);
		} else if (needRenderCaves(world, pos)) {
			MapCache.setCurrentLayer(Type.CAVES, posY);
		} else {
			MapCache.setCurrentLayer(Type.SURFACE, posY);
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
		
		if (allowEntityRadar()) {
			this.players.clear();
			this.entities.clear();
			
			int checkHeight = 24;
			BlockPos start = new BlockPos(startX, posY - checkHeight / 2, startZ);
			BlockPos end = new BlockPos(endX, posY + checkHeight / 2, endZ);
			List<Entity> entities = world.getEntities(null, new Box(start, end));
		
			int amount = 0;				
			for (Entity entity : entities) {
				float tick = minecraftClient.getTickDelta();
				double entX = entity.prevX + (entity.getX() - entity.prevX) * tick;
				double entZ = entity.prevZ + (entity.getZ() - entity.prevZ) * tick;
				double iconX = MathUtil.screenPos(entX, startX, endX, mapWidth);
				double iconY = MathUtil.screenPos(entZ, startZ, endZ, mapHeight);
				if (entity instanceof PlayerEntity && allowPlayerRadar()) {
					PlayerEntity pEntity  = (PlayerEntity) entity;
					if (pEntity == player) continue;
					PlayerIcon playerIcon = new PlayerIcon(this, pEntity, false);
					playerIcon.setPosition(iconX, iconY);
					this.players.add(playerIcon);
				} else if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
					LivingEntity livingEntity = (LivingEntity) entity;
					boolean hostile = livingEntity instanceof HostileEntity;
					if (hostile && allowHostileRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);	
						entIcon.setPosition(iconX, iconY);
						this.entities.add(entIcon);
						amount++;
					} else if (!hostile && allowCreatureRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);	
						entIcon.setPosition(iconX, iconY);
						this.entities.add(entIcon);
						amount++;
					}
				}
				if (amount >= 250) break;
			}
		}
		
		waypoints.clear();
		List<Waypoint> wps = WaypointKeeper.getInstance().getWaypoints(world.getDimensionRegistryKey().getValue(), true);
		if (wps != null) {
			Stream<Waypoint> stream = wps.stream().filter(wp -> MathUtil.getDistance(pos, wp.pos, false) <= wp.showRange);
			for (Waypoint wp : stream.toArray(Waypoint[]::new)) {
				WaypointIcon waypoint = new WaypointIcon(this, wp);
				waypoint.setPosition(
					MathUtil.screenPos(wp.pos.getX(), startX, endX, mapWidth),
					MathUtil.screenPos(wp.pos.getZ(), startZ, endZ, mapHeight)
				);
				this.waypoints.add(waypoint);
			}
		}
	}
	
	public List<WaypointIcon> getWaypoints() {
		return waypoints;
	}
	
	public void createWaypoint(Identifier dimension, BlockPos pos) {
		Waypoint waypoint = new Waypoint();
		waypoint.dimension = dimension;
		waypoint.name = "Waypoint";
		waypoint.color = RandomUtil.getElement(Waypoint.WAYPOINT_COLORS);
		waypoint.pos = pos;
		
		minecraftClient.openScreen(new WaypointEditor(waypoint, minecraftClient.currentScreen, WaypointKeeper.getInstance()::addNew));
	}
	
	public void createWaypoint() {
		World world = minecraftClient.world;
		createWaypoint(world.getDimensionRegistryKey().getValue(), PosUtil.currentPos());
	}
	
	public float getScale() {
		return this.mapScale;
	}
	
	public List<PlayerIcon> getPlayerIcons() {
		return this.players;
	}
	
	public List<EntityIcon> getEntities() {
		return this.entities;
	}
	
	public TextManager getTextManager() {
		return this.textManager;
	}
	
	public boolean isMapVisible() {
		if (minecraftClient.currentScreen != null) {
			return this.isMapVisible && !minecraftClient.isPaused() &&
				   ClientParams.showInChat && minecraftClient.currentScreen instanceof ChatScreen;
		}
		
		return this.isMapVisible;
	}
	
	public int getLasX() {
		return this.lastPosX;
	}
	
	public int getLastZ() {
		return this.lastPosZ;
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
}
