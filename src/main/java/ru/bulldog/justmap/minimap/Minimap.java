package ru.bulldog.justmap.minimap;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.data.MapCache;
import ru.bulldog.justmap.minimap.data.MapProcessor;
import ru.bulldog.justmap.minimap.icon.EntityIcon;
import ru.bulldog.justmap.minimap.icon.MapIcon;
import ru.bulldog.justmap.minimap.icon.PlayerIcon;
import ru.bulldog.justmap.minimap.icon.WaypointIcon;
import ru.bulldog.justmap.minimap.waypoint.Waypoint;
import ru.bulldog.justmap.minimap.waypoint.WaypointEditor;
import ru.bulldog.justmap.minimap.waypoint.WaypointKeeper;
import ru.bulldog.justmap.util.MathUtil;
import ru.bulldog.justmap.util.RandomUtil;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.client.texture.NativeImage;
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

public class Minimap {
	
	private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();
	
	private final TextManager textManager;
	
	private MapText txtCoords = new MapText(TextAlignment.CENTER, "0, 0, 0");
	private MapText txtBiome = new MapText(TextAlignment.CENTER, "Void");
	private MapText txtTime = new MapText(TextAlignment.CENTER, "00:00");
	private MapText txtFPS = new MapText(TextAlignment.CENTER, "00 fps");
	
	private int mapSize;	
	private float mapScale;
	private int picSize;
	
	private Biome currentBiome;
	
	private NativeImage image;
	
	private List<WaypointIcon> waypoints = new ArrayList<>();
	private List<PlayerIcon> players = new ArrayList<>();
	private List<EntityIcon> entities = new ArrayList<>();
	
	private PlayerEntity locPlayer = null;
	
	private static boolean isMapVisible = true;
	
	public Minimap() {
		this.mapSize = JustMapClient.CONFIG.getInt("map_size");
		this.mapScale = JustMapClient.CONFIG.getFloat("map_scale");
		
		this.picSize = ClientParams.rotateMap ? (int) (mapSize * 1.3) : mapSize;
		
		int scaledSize = getScaledSize();
		
		image = new NativeImage(scaledSize, scaledSize, false);
		textManager = new TextManager(this);
		isMapVisible = JustMapClient.CONFIG.getBoolean("map_visible");
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

			MapRenderer.getInstance().markDirty();
		} else {
			locPlayer = null;
		}
	}
	
	private void resizeMap(int newSize) {
		image = new NativeImage(newSize, newSize, false);
		JustMap.LOGGER.logInfo(String.format("Map resized to %dx%d", newSize, newSize));
	}
	
	public int getScaledSize() {
		this.picSize = ClientParams.rotateMap ? (int) (mapSize * 1.3) : mapSize;
		return (int) (picSize * mapScale);
	}
	
	public void onConfigChanges() {
		
		isMapVisible = JustMapClient.CONFIG.getBoolean("map_visible");
		
		int configSize = JustMapClient.CONFIG.getInt("map_size");
		float configScale = JustMapClient.CONFIG.getFloat("map_scale");
		
		if (configSize != mapSize || configScale != mapScale) {
			this.mapSize = configSize;
			this.mapScale = configScale;
			
			resizeMap(getScaledSize());
		}
	}
	
	private void updateInfo(PlayerEntity player) {
		textManager.clear();
		
		if (ClientParams.showPosition) {
			BlockPos playerPos = minecraftClient.player.getBlockPos();
			txtCoords.setText(playerPos.getX() + ", " + playerPos.getY() + ", " + playerPos.getZ());
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
			txtTime.setText(getTimeString(minecraftClient.world.getTimeOfDay()));
			textManager.add(txtTime);
		}
	}
	
	private String getTimeString(long time) {
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
		boolean allowCaves = isAllowed(ClientParams.showCaves, MapGameRules.ALLOW_CAVES_MAP);
		
		DimensionType dimType = world.getDimension().getType();
		if (dimType.hasSkyLight()) {
			return allowCaves && !world.isSkyVisibleAllowingSea(playerPos) &&
				   world.getLightLevel(LightType.SKY, playerPos) == 0;
		}
		if (dimType == DimensionType.THE_NETHER) {
			return true;
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
		BlockPos pos = player.getBlockPos();
		
		currentBiome = world.getBiome(pos);
		
		int scaled = getScaledSize();
		int startX = pos.getX() - scaled / 2;
		int startZ = pos.getZ() - scaled / 2;
		int endX = startX + scaled;
		int endZ = startZ + scaled;

		if (needRenderCaves(world, player.getBlockPos())) {
			MapCache.setCurrentLayer(MapProcessor.Layer.CAVES);
			MapCache.setLayerLevel(pos.getY() >> ClientParams.chunkLevelSize);
		} else {
			MapCache.setCurrentLayer(MapProcessor.Layer.SURFACE);
			MapCache.setLayerLevel(0);
		}
		
		MapCache.get().update(this, scaled, startX, startZ);
		
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
					playerIcon.setPosition(MapIcon.scaledPos(x, startX, endX, mapSize), MapIcon.scaledPos(z, startZ, endZ, mapSize));
					this.players.add(playerIcon);
				}
			}
		}
		
		if (allowEntityRadar()) {
			entities.clear();
			
			int checkHeight = 24;
			BlockPos start = new BlockPos(startX, player.getY() - checkHeight / 2, startZ);
			BlockPos end = new BlockPos(endX, player.getY() + checkHeight / 2, endZ);
			List<Entity> entities = world.getEntities((Entity) null, new Box(start, end));
		
			int t = 0;
				
			for (Entity entity : entities) {
				if (entity instanceof LivingEntity && !(entity instanceof PlayerEntity)) {
					t++;
					LivingEntity livingEntity = (LivingEntity) entity;
					boolean hostile = livingEntity instanceof HostileEntity;
					if (hostile && allowHostileRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);						
						entIcon.setPosition(MapIcon.scaledPos((int) entity.getX(), startX, endX, mapSize), MapIcon.scaledPos((int) entity.getZ(), startZ, endZ, mapSize));						
						this.entities.add(entIcon);
					} else if (!hostile && allowCreatureRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);						
						entIcon.setPosition(MapIcon.scaledPos((int) entity.getX(), startX, endX, mapSize), MapIcon.scaledPos((int) entity.getZ(), startZ, endZ, mapSize));						
						this.entities.add(entIcon);
					}
				}
				if (t >= 250) {
					break;
				}
			}
		}
		
		waypoints.clear();
		List<Waypoint> wps = WaypointKeeper.getInstance().getWaypoints(world.dimension.getType().getRawId(), true);
		if (wps != null) {
			wps.stream().filter(wp -> MathUtil.getDistance(pos, wp.pos, false) <= wp.showRange).forEach(wp -> {
				WaypointIcon waypoint = new WaypointIcon(this, wp);
				waypoint.setPosition(
					MathUtil.clamp(MapIcon.scaledPos(wp.pos.getX(), startX, endX, mapSize), 0, scaled),
					MathUtil.clamp(MapIcon.scaledPos(wp.pos.getZ(), startZ, endZ, mapSize), 0, scaled)
				);
				waypoints.add(waypoint);
			});
		}
	}
	
	public List<WaypointIcon> getWaypoints() {
		return waypoints;
	}
	
	public void createWaypoint() {
		PlayerEntity player = minecraftClient.player;
		
		Waypoint waypoint = new Waypoint();
		waypoint.dimension = player.world.dimension.getType().getRawId();
		waypoint.name = "Waypoint";
		waypoint.color = RandomUtil.getElement(Waypoint.WAYPOINT_COLORS);
		waypoint.pos = player.getBlockPos();
		
		minecraftClient.openScreen(new WaypointEditor(waypoint, minecraftClient.currentScreen, WaypointKeeper.getInstance()::addNew));		
	}
	
	public NativeImage getImage() {
		return image;
	}
	
	public int getPictureSize() {
		return this.picSize;
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
		return isMapVisible;
	}
}
