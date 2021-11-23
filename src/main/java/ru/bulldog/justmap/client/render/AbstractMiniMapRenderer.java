package ru.bulldog.justmap.client.render;

import com.mojang.blaze3d.systems.RenderSystem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.Window;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.InfoText;
import ru.bulldog.justmap.advancedinfo.MapText;
import ru.bulldog.justmap.advancedinfo.TextManager;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.enums.ArrowType;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.map.ChunkGrid;
import ru.bulldog.justmap.map.DirectionArrow;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.map.data.MapRegionProvider;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.Point;
import ru.bulldog.justmap.util.render.RenderUtil;

@Environment(EnvType.CLIENT)
public abstract class AbstractMiniMapRenderer {

	protected static final Identifier roundMask = new Identifier(JustMap.MODID, "textures/round_mask.png");
	protected static final MinecraftClient minecraft = MinecraftClient.getInstance();
	protected static TextManager textManager;
	protected static final InfoText dirN = new MapText(TextAlignment.CENTER, "N");
	protected static final InfoText dirS = new MapText(TextAlignment.CENTER, "S");
	protected static final InfoText dirE = new MapText(TextAlignment.CENTER, "E");
	protected static final InfoText dirW = new MapText(TextAlignment.CENTER, "W");

	protected int winWidth, winHeight;
	protected int mapWidth, mapHeight;
	protected int scaledW, scaledH;
	protected int centerX, centerY;
	protected int lastX, lastZ;
	protected int mapX, mapY;
	protected int imgX, imgY;
	protected int imgW, imgH;
	protected double currX, currZ;
	protected float mapScale;
	protected float rotation;
	protected float offX, offY;
	protected float delta;
	protected boolean mapRotation = false;
	protected boolean paramsUpdated = false;
	protected boolean playerMoved = false;
	protected final Minimap minimap;
	protected final BlockPos.Mutable playerPos;
	protected MapRegionProvider mapRegionProvider;
	protected ChunkGrid chunkGrid;
	protected MapSkin mapSkin;

	public AbstractMiniMapRenderer(Minimap map) {
		this.playerPos = new BlockPos.Mutable(0, 0, 0);
		this.minimap = map;
		if (textManager == null) {
			textManager = minimap.getTextManager();
			textManager.setSpacing(12);
			textManager.add(dirN);
			textManager.add(dirS);
			textManager.add(dirE);
			textManager.add(dirW);
		}
	}

	protected abstract void render(MatrixStack matrices, double scale);

	public void updateParams() {
		this.mapRegionProvider = minimap.getMapRegionProvider();
		this.mapSkin = minimap.getSkin();

		int winW = minecraft.getWindow().getWidth();
		int winH = minecraft.getWindow().getHeight();
		if (winWidth != winW || winHeight != winH) {
			minimap.updateMapParams();
			this.winWidth = winW;
			this.winHeight = winH;
		}

		this.delta = minecraft.getTickDelta();
		this.currX = DataUtil.doubleX(delta);
		this.currZ = DataUtil.doubleZ(delta);

		int mapW = minimap.getWidth();
		int mapH = minimap.getHeight();
		int mapX = minimap.getMapX();
		int mapY = minimap.getMapY();
		int lastX = MathUtil.floor(currX);
		int lastZ = MathUtil.floor(currZ);
		float scale = minimap.getScale();
		boolean rotateMap = minimap.isRotated();
		if (mapWidth != mapW || mapHeight != mapH ||
			this.mapX != mapX || this.mapY != mapY ||
			mapScale != scale || mapRotation != rotateMap) {

			this.mapWidth = mapW;
			this.mapHeight = mapH;
			this.mapRotation = rotateMap;
			this.mapScale = scale;
			this.mapX = mapX;
			this.mapY = mapY;
			this.centerX = mapX + mapWidth / 2;
			this.centerY = mapY + mapHeight / 2;
			this.scaledW = minimap.getScaledWidth();
			this.scaledH = minimap.getScaledHeight();

			if (mapRotation) {
				this.imgW = (int) (scaledW / mapScale);
				this.imgH = (int) (scaledH / mapScale);
				this.imgX = mapX - (imgW - mapW) / 2;
				this.imgY = mapY - (imgH - mapH) / 2;
			} else {
				this.imgW = (int) (mapW * 1.25);
				this.imgH = (int) (mapH * 1.25);
				int deltaX = imgW - mapW;
				int deltaY = imgH - mapH;
				this.imgX = mapX - deltaX / 2;
				this.imgY = mapY - deltaY / 2;
				this.scaledW += deltaX * mapScale;
				this.scaledH += deltaY * mapScale;
			}

			this.paramsUpdated = true;
		}

		if (this.lastX != lastX || this.lastZ != lastZ) {
			this.lastX = lastX;
			this.lastZ = lastZ;
			this.playerPos.set(lastX, DataUtil.coordY(), lastZ);
			this.playerMoved = true;
		}

		int mapR = mapX + mapWidth;
		int mapB = mapY + mapHeight;

		Point center = new Point(centerX, centerY);
		Point pointN = new Point(centerX, mapY);
		Point pointS = new Point(centerX, mapB);
		Point pointE = new Point(mapR, centerY);
		Point pointW = new Point(mapX, centerY);

		this.rotation = 180.0F;
		if (mapRotation) {
			this.rotation = minecraft.player.headYaw;
			double rotate = MathUtil.correctAngle(rotation) + 180;
			double angle = Math.toRadians(-rotate);

			Line radius = new Line(center, pointN);
			Line corner = new Line(center, new Point(mapX, mapY));
			int len = (int) (Minimap.isRound() ? radius.length() : corner.length());

			pointN.y = centerY - len;
			pointS.y = centerY + len;
			pointE.x = centerX + len;
			pointW.x = centerX - len;

			this.calculatePos(center, pointN, mapR, mapB, angle);
			this.calculatePos(center, pointS, mapR, mapB, angle);
			this.calculatePos(center, pointE, mapR, mapB, angle);
			this.calculatePos(center, pointW, mapR, mapB, angle);
		}

		dirN.setPos((int) pointN.x, (int) pointN.y - 5);
		dirS.setPos((int) pointS.x, (int) pointS.y - 5);
		dirE.setPos((int) pointE.x, (int) pointE.y - 5);
		dirW.setPos((int) pointW.x, (int) pointW.y - 5);
	}

	protected void calculatePos(Point center, Point dir, int mr, int mb, double angle) {
		Point pos = MathUtil.circlePos(dir, center, angle);
		int posX = (int) MathUtil.clamp(pos.x, mapX, mr);
		int posY = (int) MathUtil.clamp(pos.y, mapY, mb);

		dir.x = posX; dir.y = posY;
	}

	public void renderMap(MatrixStack matrices) {
		if (!minimap.isMapVisible() || !JustMapClient.canMapping()) return;

		this.updateParams();

		if (mapRegionProvider == null) return;

		Window window = minecraft.getWindow();
		double scale = window.getScaleFactor();

		this.offX = this.calcOffset(currX, lastX, mapScale);
		this.offY = this.calcOffset(currZ, lastZ, mapScale);

		RenderSystem.disableDepthTest();
		this.render(matrices, scale);

		if (mapSkin != null) {
			int skinX = minimap.getSkinX();
			int skinY = minimap.getSkinY();
			int brd = minimap.getBorder() * 2;
			this.mapSkin.draw(matrices, skinX, skinY, mapWidth + brd, mapHeight + brd);
		}

		RenderUtil.drawRightAlignedString(
				matrices, Double.toString(1 / mapScale),
				mapX + mapWidth - 3, mapY + mapHeight - 10, Colors.WHITE);

		int iconSize = ClientSettings.arrowIconSize;
		if (ClientSettings.arrowIconType == ArrowType.DIRECTION_ARROW) {
			float direction = mapRotation ? 180 : minecraft.player.headYaw;
			DirectionArrow.draw(centerX, centerY, iconSize, direction);
		} else {
			MapPlayerManager.getPlayer(minecraft.player).getIcon().draw(centerX, centerY, iconSize, true);
		}
		textManager.draw(matrices);

		RenderSystem.enableDepthTest();
	}

	protected float calcOffset(double x, double lastX, double scale) {
		return (float) (Math.floor(((x - lastX) / scale) * 1000.0) * 0.001);
	}
}
