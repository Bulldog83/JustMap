package ru.bulldog.justmap.minimap;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.config.Params;
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
import ru.bulldog.justmap.util.Drawer.TextAlignment;

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

import java.util.ArrayList;
import java.util.List;

public class Minimap {
	
	private static final MinecraftClient minecraftClient = MinecraftClient.getInstance();
	
	private final TextManager textManager;
	
	private MapText txtCoords = new MapText(TextAlignment.CENTER, "0, 0, 0");
	private MapText txtBiome = new MapText(TextAlignment.CENTER, "Void");
	private MapText txtTime = new MapText(TextAlignment.CENTER, "00:00");
	private MapText txtFPS = new MapText(TextAlignment.CENTER, "00 fps");
	
	private int mapSize = JustMap.CONFIG.getInt("map_size");
	
	private float mapScale = 1;
	
	private Biome currentBiome;
	
	private NativeImage image;
	
	private List<WaypointIcon> waypoints = new ArrayList<>();
	private List<PlayerIcon> players = new ArrayList<>();
	private List<EntityIcon> entities = new ArrayList<>();
	
	private PlayerEntity locPlayer = null;
	
	private static boolean isMapVisible = true;
	
	public Minimap() {
		image = new NativeImage(NativeImage.Format.RGBA, mapSize, mapSize, false);	
		textManager = new TextManager(this);
		isMapVisible = JustMap.CONFIG.getBoolean("map_visible");
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
		image = new NativeImage(NativeImage.Format.RGBA, newSize, newSize, false);
		JustMap.LOGGER.logInfo(String.format("Map resized to %dx%d", newSize, newSize));
	}
	
	public void onConfigChanges() {
		
		isMapVisible = JustMap.CONFIG.getBoolean("map_visible");
		
		int configSize = JustMap.CONFIG.getInt("map_size");
		float configScale = JustMap.CONFIG.getFloat("map_scale");
		
		if (configSize != mapSize || configScale != mapScale) {
			this.mapSize = configSize;
			this.mapScale = configScale;
			
			resizeMap((int) (mapSize * mapScale));
		}
	}
	
	private void updateInfo(PlayerEntity player) {
		textManager.clear();
		
		if (Params.showPosition) {
			BlockPos playerPos = minecraftClient.player.getBlockPos();
			txtCoords.setText(playerPos.getX() + ", " + playerPos.getY() + ", " + playerPos.getZ());
			textManager.add(txtCoords);
		}		
		if (Params.showBiome) {
			txtBiome.setText(I18n.translate(currentBiome.getTranslationKey()));
			textManager.add(txtBiome);
		}		
		if (Params.showFPS) {
			txtFPS.setText(minecraftClient.fpsDebugString.substring(0, minecraftClient.fpsDebugString.indexOf("fps") + 3));
			textManager.add(txtFPS);
		}		
		if (Params.showTime) {
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
		if (world.getDimension().isNether()) {
			return true;
		}
		
		boolean allowCaves = isAllowed(Params.showCaves, MapGameRules.ALLOW_CAVES_MAP);
		
		return allowCaves && !world.isSkyVisibleAllowingSea(playerPos) &&
			   world.getLightLevel(LightType.SKY, playerPos) == 0;
	}
	
	public static boolean allowEntityRadar() {
		return isAllowed(Params.showEntities, MapGameRules.ALLOW_ENTITY_RADAR);
	}
	
	public static boolean allowHostileRadar() {
		return isAllowed(Params.showHostile, MapGameRules.ALLOW_HOSTILE_RADAR);
	}
	
	public static boolean allowCreatureRadar() {
		return isAllowed(Params.showCreatures, MapGameRules.ALLOW_CREATURE_RADAR);
	}
	
	public static boolean allowPlayerRadar() {
		return isAllowed(Params.showPlayers, MapGameRules.ALLOW_PLAYER_RADAR);
	}
	
	public void prepareMap(PlayerEntity player) {
		World world = player.world;
		BlockPos pos = player.getBlockPos();
		
		currentBiome = world.getBiome(pos);
		
		int scaled = (int) (mapSize * mapScale);
		int startX = pos.getX() - scaled / 2;
		int startZ = pos.getZ() - scaled / 2;
		int endX = startX + scaled;
		int endZ = startZ + scaled;
		
		if (needRenderCaves(world, player.getBlockPos())) {
			MapCache.setCurrentLayer(MapProcessor.Layer.CAVES);
		} else {
			MapCache.setCurrentLayer(MapProcessor.Layer.SURFACE);
		}
		MapCache.get(world).update(this, startX, startZ);
		
		if (allowPlayerRadar() && Params.showPlayers) {
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
					playerIcon.setPosition(MapIcon.getScaled(x, startX, endX, mapSize), MapIcon.getScaled(z, startZ, endZ, mapSize));
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
					if (allowHostileRadar() && hostile) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);						
						entIcon.setPosition(MapIcon.getScaled((int) entity.getX(), startX, endX, mapSize), MapIcon.getScaled((int) entity.getZ(), startZ, endZ, mapSize));						
						this.entities.add(entIcon);
					} else if (allowCreatureRadar()) {
						EntityIcon entIcon = new EntityIcon(this, entity, hostile);						
						entIcon.setPosition(MapIcon.getScaled((int) entity.getX(), startX, endX, mapSize), MapIcon.getScaled((int) entity.getZ(), startZ, endZ, mapSize));						
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
			wps.stream().filter(wp -> MathUtil.getDistance(pos, wp.pos, true) <= wp.showRange).forEach(wp -> {
				WaypointIcon waypoint = new WaypointIcon(this, wp);
				waypoint.setPosition(
					MathUtil.clamp(MapIcon.getScaled(wp.pos.getX(), startX, endX, mapSize), 0, mapSize),
					MathUtil.clamp(MapIcon.getScaled(wp.pos.getZ(), startZ, endZ, mapSize), 0, mapSize)
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
	
	public int getSize() {
		return mapSize;
	}
	
	public float getScale() {
		return mapScale;
	}
	
	public List<PlayerIcon> getPlayerIcons() {
		return players;
	}
	
	public List<EntityIcon> getEntities() {
		return entities;
	}
	
	public TextManager getTextManager() {
		return textManager;
	}
	
	public boolean isMapVisible() {
		return isMapVisible;
	}
}
