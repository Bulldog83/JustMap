package ru.bulldog.justmap.client.config;

import ru.bulldog.justmap.map.minimap.MapPosition;

public class ClientParams {
	public static MapPosition mapPosition = MapPosition.TOP_RIGHT;
	
	public static int positionOffset = 4;
	public static int mapSize = 128;
	public static int chunkLevelSize = 3;
	public static float mapScale = 1.0F;
	public static boolean mapVisible = true;
	public static boolean showCaves = true;
	public static boolean moveEffects = true;
	public static boolean showEffectTimers = true;
	public static boolean rotateMap = false;

	public static boolean useSkins = true;
	public static int currentSkin = 0;
	public static boolean alternateColorRender = true;
	public static int mapSaturation = 0;
	public static int mapBrightness = 0;
	public static boolean simpleArrow = false;
	
	public static boolean showPosition = true;
	public static boolean showFPS = false;
	public static boolean showBiome = true;
	public static boolean showTime = true;
	
	public static boolean showEntities = true;
	public static boolean showEntityHeads = true;
	public static boolean showHostile = true;
	public static boolean showCreatures = true;
	public static boolean showPlayers = true;
	public static boolean showPlayerHeads = true;
	public static boolean showPlayerNames = true;
	public static boolean showIconsOutline = false;
	public static boolean renderEntityModel = false;
	public static int entityIconSize = 8;
	public static int entityModelSize = 5;
	
	public static int chunkUpdateInterval = 1000;
	public static int chunkLevelUpdateInterval = 3000;
	public static int updatePerCycle = 10;
	public static int purgeDelay = 60;
	public static int purgeAmount = 1500;
	
	public static boolean waypointsTracking = true;
	public static boolean waypointsWorldRender = true;
	public static boolean renderLightBeam = true;
	public static boolean renderMarkers = true;
	public static boolean renderAnimation = true;
	public static int minRenderDist = 1;
	public static int maxRenderDist = 1000;
	
	public static boolean showTerrain = true;
	public static int terrainStrength = 4;
	public static boolean drawChunkGrid = false;
	public static boolean showInChat = false;
}
