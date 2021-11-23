package ru.bulldog.justmap.client.config;

import ru.bulldog.justmap.enums.ArrowType;
import ru.bulldog.justmap.enums.MapShape;
import ru.bulldog.justmap.enums.MultiworldDetection;
import ru.bulldog.justmap.enums.ScreenPosition;

public class ClientSettings {
	public static ScreenPosition mapPosition = ScreenPosition.TOP_RIGHT;
	public static ScreenPosition infoPosition = ScreenPosition.TOP_LEFT;
	public static ScreenPosition itemsPosition = ScreenPosition.MIDDLE_LEFT;
	public static ArrowType arrowIconType = ArrowType.DIRECTION_ARROW;
	public static MultiworldDetection multiworldDetection = MultiworldDetection.AUTO;
	public static MapShape mapShape = MapShape.SQUARE;
	
	public static int positionOffset = 4;
	public static int mapPositionX = positionOffset;
	public static int mapPositionY = positionOffset;
	public static int mapSize = 64;
	public static int bigMapSize = 256;
	public static float mapScale = 1.0F;
	public static boolean mapVisible = true;
	public static boolean moveEffects = true;
	public static boolean showEffectTimers = true;
	public static boolean rotateMap = false;
	public static boolean showInChat = false;
	public static boolean showBigMap = false;
	public static boolean detectMultiworlds = false;

	public static boolean drawCaves = true;
	public static boolean hidePlants = false;
	public static boolean hideWater = false;
	public static boolean showGrid = false;
	public static boolean showWorldmapGrid = false;
	public static boolean showTerrain = true;
	public static boolean showTopography = true;
	public static boolean showSlime = false;
	public static boolean showLoadedChunks = false;
	public static int terrainStrength = 4;

	public static boolean useSkins = true;
	public static boolean alternateColorRender = true;
	public static boolean waterTint = true;
	public static boolean simpleArrow = false;
	public static boolean textureFilter = false;
	public static float skinScale = 2.0F;
	public static int currentSkin = 0;
	public static int bigMapSkin = 0;
	public static int mapSaturation = 0;
	public static int mapBrightness = 0;
	public static int arrowIconSize = 12;
	public static int worldmapIconSize = 12;
	
	public static boolean advancedInfo = true;
	public static boolean mapInfo = true;
	public static boolean showPosition = true;
	public static boolean showFPS = true;
	public static boolean showBiome = true;
	public static boolean showTime = true;
	public static boolean showLight = true;
	public static boolean showItems = true;
	public static boolean showMainhand = true;
	public static boolean showOffhand = true;
	public static boolean showHead = true;
	public static boolean showChest = true;
	public static boolean showLegs = true;
	public static boolean showFeet = true;
	
	public static boolean showEntities = true;
	public static boolean showEntityHeads = true;
	public static boolean showHostile = true;
	public static boolean showCreatures = true;
	public static boolean showPlayers = true;
	public static boolean showPlayerHeads = true;
	public static boolean showPlayerNames = true;
	public static boolean showIconsOutline = false;
	public static boolean renderEntityModel = false;
	public static boolean entityIconsShading = true;
	public static int entityIconSize = 8;
	public static int entityModelSize = 5;
	public static int entityOutlineSize = 1;

	public static int chunkUpdateInterval = 1000;
	public static int chunkLevelUpdateInterval = 3000;
	public static int purgeDelay = 60;
	public static int purgeAmount = 1500;
	public static boolean forceUpdate = false;
	public static boolean fastRender = false;
	
	public static boolean showWaypoints = true;
	public static boolean jumpToWaypoints = true;
	public static boolean waypointsTracking = true;
	public static boolean waypointsWorldRender = true;
	public static boolean renderLightBeam = true;
	public static boolean renderMarkers = true;
	public static boolean renderAnimation = true;
	public static int minRenderDist = 1;
	public static int maxRenderDist = 1000;
}
