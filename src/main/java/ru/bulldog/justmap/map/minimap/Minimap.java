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
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
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
	private int scaledSize;
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
		
		if (configSize != mapWidth || configScale != mapScale ||
			this.rotateMap != needRotate) {
			
			this.mapWidth = configSize;
			this.mapHeight = configSize;
			this.mapScale = configScale;
			this.rotateMap = needRotate;
			
			if (rotateMap) {
				this.scaledSize = (int) ((mapWidth * mapScale) * 1.42 + 8);
			} else {
				this.scaledSize = (int) ((mapWidth * mapScale) + 8);
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
	
	private static boolean isAllowed(boolean param, GameRules.RuleKey<GameRules.BooleanRule> rule) {
		if (param) {
			return minecraftClient.isInSingleplayer() || MapGameRules.isAllowed(rule);
		}
		
		return false;
	}
	
	private boolean needRenderCaves(World world, BlockPos playerPos) {
		boolean allowCaves = isAllowed(ClientParams.drawCaves, MapGameRules.ALLOW_CAVES_MAP);
		
		DimensionType dimType = world.getDimension().getType();
		if (dimType.hasSkyLight()) {
			return allowCaves && !world.isSkyVisibleAllowingSea(playerPos) &&
				   world.getLightLevel(LightType.SKY, playerPos) == 0;
		}
		if (dimType == DimensionType.THE_END) {
			return false;
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
		int scaled = this.getScaledSize();
		double startX = posX - scaled / 2;
		double startZ = posZ - scaled / 2;

		if (world.dimension.isNether()) {
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
			scaled = (int) (mapWidth * mapScale);
			startX = posX - scaled / 2;
			startZ = posZ - scaled / 2;
		}		
		
		double endX = startX + scaled;
		double endZ = startZ + scaled;
		double shiftX = (PosUtil.doubleCoordX() - lastPosX) / this.mapScale;
		double shiftZ = (PosUtil.doubleCoordZ() - lastPosZ) / this.mapScale;
		
		if (allowPlayerRadar()) {
			players.clear();			
			List<? extends PlayerEntity> players = world.getPlayers();
			for (PlayerEntity p : players) {
				if (p == player || p.isSpectator()) {
					continue;
				}
				
				BlockPos ppos = p.getBlockPos();
			 
				int x = ppos.getX();
				int z = ppos.getZ();				
				if (x >= startX && x <= endX && z >= startZ && z <= endZ) {
					PlayerIcon playerIcon = new PlayerIcon(this, p, false);
					playerIcon.setPosition(MathUtil.screenPos(x, startX, endX, mapWidth),
										   MathUtil.screenPos(z, startZ, endZ, mapHeight));
					this.players.add(playerIcon);
				}
			}
		}
		
		if (allowEntityRadar()) {
			entities.clear();
			
			int checkHeight = 24;
			BlockPos start = new BlockPos(startX, posY - checkHeight / 2, startZ);
			BlockPos end = new BlockPos(endX, posY + checkHeight / 2, endZ);
			List<Entity> entities = world.getEntities((Entity) null, new Box(start, end));
		
			int amount = 0;				
			for (Entity entity : entities) {
				if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
					LivingEntity livingEntity = (LivingEntity) entity;
					boolean hostile = livingEntity instanceof HostileEntity;
					double entX = MathUtil.screenPos(entity.getX(), startX, endX, mapWidth);
					double entZ = MathUtil.screenPos(entity.getZ(), startZ, endZ, mapHeight);
					if (hostile && allowHostileRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);	
						entIcon.setPosition(entX, entZ);
						this.entities.add(entIcon);
						amount++;
					} else if (!hostile && allowCreatureRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);	
						entIcon.setPosition(entX, entZ);
						this.entities.add(entIcon);
						amount++;
					}
				}
				if (amount >= 250) break;
			}
		}
		
		waypoints.clear();
		List<Waypoint> wps = WaypointKeeper.getInstance().getWaypoints(world.dimension.getType().getRawId(), true);
		if (wps != null) {
			Stream<Waypoint> stream = wps.stream().filter(wp -> MathUtil.getDistance(pos, wp.pos, false) <= wp.showRange);
			for (Waypoint wp : stream.toArray(Waypoint[]::new)) {
				WaypointIcon waypoint = new WaypointIcon(this, wp);
				waypoint.setPosition(
					MathUtil.screenPos(wp.pos.getX(), startX, endX, mapWidth) - shiftX,
					MathUtil.screenPos(wp.pos.getZ(), startZ, endZ, mapHeight) - shiftZ
				);
				this.waypoints.add(waypoint);
			}
		}
	}
	
	public List<WaypointIcon> getWaypoints() {
		return waypoints;
	}
	
	public void createWaypoint(int dimension, BlockPos pos) {
		Waypoint waypoint = new Waypoint();
		waypoint.dimension = dimension;
		waypoint.name = "Waypoint";
		waypoint.color = RandomUtil.getElement(Waypoint.WAYPOINT_COLORS);
		waypoint.pos = pos;
		
		minecraftClient.openScreen(new WaypointEditor(waypoint, minecraftClient.currentScreen, WaypointKeeper.getInstance()::addNew));
	}
	
	public void createWaypoint() {
		PlayerEntity player = minecraftClient.player;
		createWaypoint(player.world.dimension.getType().getRawId(), player.getBlockPos());
	}
	
	public int getScaledSize() {
		return this.scaledSize;
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
		return this.getScaledSize();
	}

	@Override
	public int getScaledHeight() {
		return this.getScaledSize();
	}
}
