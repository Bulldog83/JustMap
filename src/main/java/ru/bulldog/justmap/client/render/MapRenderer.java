package ru.bulldog.justmap.client.render;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.InfoText;
import ru.bulldog.justmap.advancedinfo.MapText;
import ru.bulldog.justmap.advancedinfo.TextManager;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.DirectionArrow;
import ru.bulldog.justmap.map.data.MapCache;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.icon.EntityIcon;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.minimap.MapPlayerManager;
import ru.bulldog.justmap.map.minimap.MapSkin;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.RenderUtil.TextAlignment;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.ScreenPosition;
import ru.bulldog.justmap.util.PosUtil;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.Line.Point;
import ru.bulldog.justmap.util.math.MathUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class MapRenderer {
	
	private static MapRenderer instance;
	
	protected ScreenPosition mapPosition;
	protected int border = 0;
	
	private int offset;
	private int posX, posY;
	private int mapX, mapY;
	private int winWidth, winHeight;
	private int mapWidth, mapHeight;
	private int imgX, imgY;
	private int imgW, imgH;
	private float rotation;
	private int lastX;
	private int lastZ;

	private final Minimap minimap;
	
	private MapSkin mapSkin;
	private TextManager textManager;	
	private InfoText dirN = new MapText(TextAlignment.CENTER, "N");
	private InfoText dirS = new MapText(TextAlignment.CENTER, "S");
	private InfoText dirE = new MapText(TextAlignment.CENTER, "E");
	private InfoText dirW = new MapText(TextAlignment.CENTER, "W");
	
	private final MinecraftClient minecraft = MinecraftClient.getInstance();
	private final Identifier roundMask = new Identifier(JustMap.MODID, "textures/round_mask.png");
	
	public static MapRenderer getInstance() {
		if (instance == null) {
			instance = new MapRenderer();
		}
		
		return instance;
	}
	
	private MapRenderer() {
		this.minimap = JustMapClient.MAP;
		this.textManager = this.minimap.getTextManager();
		this.textManager.setSpacing(12);		
		this.textManager.add(dirN);
		this.textManager.add(dirS);
		this.textManager.add(dirE);
		this.textManager.add(dirW);
	}
	
	public int getX() {
		return this.posX;
	}
	
	public int getY() {
		return this.posY;
	}
	
	public void updateParams() {		
		int border = 0;
		if (ClientParams.useSkins) {
			this.mapSkin = MapSkin.getSkin(ClientParams.currentSkin);
			
			mapSkin.getRenderData().updateScale();
			
			double scale = mapSkin.getRenderData().scaleFactor;
			border = (int) (this.mapSkin.border * scale);
		}
		
		int winW = minecraft.getWindow().getScaledWidth();
		int winH = minecraft.getWindow().getScaledHeight();
		int mapW = this.minimap.getWidth();
		int mapH = this.minimap.getHeight();
		int off = ClientParams.positionOffset;
		ScreenPosition mapPos = ClientParams.mapPosition;
		if (mapWidth != mapW || mapHeight != mapH || mapPosition != mapPos ||
			this.border != border || offset != off ||
			winWidth != winW || winHeight != winH) {
			
			this.winWidth = winW;
			this.winHeight = winH;
			this.mapWidth = mapW;
			this.mapHeight = mapH;
			this.mapPosition = mapPos;
			this.offset = off;
			this.border = border;
			
			this.posX = offset;
			this.posY = offset;
			this.mapX = posX + border;
			this.mapY = posY + border;			
			
			TextManager.TextPosition textPos = TextManager.TextPosition.UNDER;

			switch (mapPosition) {
				case TOP_LEFT:
					break;
				case TOP_CENTER:
					this.mapX = winW / 2 - mapWidth / 2;
					this.posX = mapX - border;
					break;
				case TOP_RIGHT:
					this.mapX = winW - offset - mapWidth - border;
					this.posX = mapX - border;
					break;
				case MIDDLE_RIGHT:
					this.mapX = winW - offset - mapWidth - border;
					this.mapY = winH / 2 - mapHeight / 2;
					this.posX = mapX - border;
					this.posY = mapY - border;
					break;
				case MIDDLE_LEFT:
					this.mapY = winH / 2 - mapHeight / 2;
					this.posY = mapY - border;
					break;
				case BOTTOM_LEFT:
					textPos = TextManager.TextPosition.ABOVE;
					this.mapY = winH - offset - mapHeight - border;
					this.posY = mapY - border;
					break;
				case BOTTOM_RIGHT:
					textPos = TextManager.TextPosition.ABOVE;
					this.mapX = winW - offset - mapWidth - border;
					this.posX = mapX - border;
					this.mapY = winH - offset - mapHeight - border;
					this.posY = mapY - border;
					break;
			}
			
			if (ClientParams.rotateMap) {
				this.imgW = (int) (mapWidth * 1.42);
				this.imgH = (int) (mapHeight * 1.42);
				this.imgX = mapX - (imgW - mapWidth) / 2;
				this.imgY = mapY - (imgH - mapHeight) / 2;
			} else {
				this.imgW = this.mapWidth;
				this.imgH = this.mapHeight;
				this.imgX = this.mapX;
				this.imgY = this.mapY;
			}
			
			this.textManager.updatePosition(textPos,
				mapX, mapY + (textPos == TextManager.TextPosition.UNDER ?
					mapHeight + border + 3 :
					-(border + 3))
			);
		}
		
		int centerX = mapX + mapWidth / 2;
		int centerY = mapY + mapHeight / 2;
		int mapR = mapX + mapWidth;
		int mapB = mapY + mapHeight;
		
		Point center = new Point(centerX, centerY);
		Point pointN = new Point(centerX, mapY);
		Point pointS = new Point(centerX, mapB);
		Point pointE = new Point(mapR, centerY);
		Point pointW = new Point(mapX, centerY);
		
		this.rotation = minecraft.player.headYaw;
		
		if (ClientParams.rotateMap) {
			float rotate = MathUtil.correctAngle(rotation) + 180;
			double angle = Math.toRadians(-rotate);
			
			Line radius = new Line(center, pointN);
			Line corner = new Line(center, new Point(mapX, mapY));
			
			radius.add(corner.lenght() - radius.lenght());
			int len = radius.lenght();
			
			pointN.y = centerY - len;
			pointS.y = centerY + len;
			pointE.x = centerX + len;
			pointW.x = centerX - len;
			
			this.calculatePos(center, pointN, mapR, mapB, angle);
			this.calculatePos(center, pointS, mapR, mapB, angle);
			this.calculatePos(center, pointE, mapR, mapB, angle);
			this.calculatePos(center, pointW, mapR, mapB, angle);
		}
		
		this.dirN.setPos(pointN.x, pointN.y - 5);
		this.dirS.setPos(pointS.x, pointS.y - 5);
		this.dirE.setPos(pointE.x, pointE.y - 5);
		this.dirW.setPos(pointW.x, pointW.y - 5);
	}
	
	private void calculatePos(Point center, Point dir, int mr, int mb, double angle) {		
		int posX = (int) (center.x + (dir.x - center.x) * Math.cos(angle) - (dir.y - center.y) * Math.sin(angle));
		int posY = (int) (center.y + (dir.y - center.y) * Math.cos(angle) + (dir.x - center.x) * Math.sin(angle));
		posX = MathUtil.clamp(posX, mapX, mr);
		posY = MathUtil.clamp(posY, mapY, mb);
		
		dir.x = posX; dir.y = posY;
	}
	
	public void draw(MatrixStack matrix) {
		if (!minimap.isMapVisible() || minecraft.player == null) {
			return;
		}
		
		this.updateParams();
		
		int winH = minecraft.getWindow().getFramebufferHeight();
		double scale = minecraft.getWindow().getScaleFactor();
		
		int scaledX = (int) (mapX * scale);
		int scaledY = (int) (winH - (mapY + mapHeight) * scale);
		int scaledW = (int) (mapWidth * scale);
		int scaledH = (int) (mapHeight * scale);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableDepthTest();
		
		if (this.minimap.posChanged) {
			this.lastX = minimap.getLasX();
			this.lastZ = minimap.getLastZ();
			this.minimap.posChanged = false;
		}
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(scaledX, scaledY, scaledW, scaledH);
		
		float mult = 1 / minimap.getScale();		
		float offX = (float) (PosUtil.doubleCoordX() - this.lastX) * mult;
		float offY = (float) (PosUtil.doubleCoordZ() - this.lastZ) * mult;
		
		RenderSystem.enableBlend();
		RenderSystem.colorMask(false, false, false, true);
		RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
		RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);
		RenderSystem.colorMask(true, true, true, true);
		RenderUtil.bindTexture(this.roundMask);
		RenderUtil.startDraw();
		RenderUtil.addQuad(mapX, mapY, mapWidth, mapHeight);
		RenderUtil.endDraw();
		//RenderSystem.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
		RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
		
		RenderSystem.pushMatrix();
		if (ClientParams.rotateMap) {
			float moveX = imgX + imgW / 2;
			float moveY = imgY + imgH / 2;
			RenderSystem.translatef(moveX, moveY, 0.0F);
			RenderSystem.rotatef(-rotation + 180, 0, 0, 1.0F);
			RenderSystem.translatef(-moveX, -moveY, 0.0F);
		}
		RenderSystem.translatef(-offX, -offY, 0.0F);		
		this.drawMap();
		RenderSystem.popMatrix();
		
		if (Minimap.allowEntityRadar()) {
			if (Minimap.allowPlayerRadar()) {
				for (PlayerIcon player : minimap.getPlayerIcons()) {
					player.draw(matrix, mapX, mapY, offX, offY, rotation);
				}
			}
			if (Minimap.allowCreatureRadar() || Minimap.allowHostileRadar()) {
				for (EntityIcon entity : minimap.getEntities()) {
					entity.draw(matrix, mapX, mapY, offX, offY, rotation);
				}
			}
		}
		
		RenderUtil.drawRightAlignedString(
				matrix, Float.toString(minimap.getScale()),
				mapX + mapWidth - 3, mapY + mapHeight - 10, Colors.WHITE);
		
		for (WaypointIcon waypoint : minimap.getWaypoints()) {
			if (!waypoint.isHidden()) {
				waypoint.draw(matrix, mapX, mapY, offX, offY, rotation);
			}
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		if (ClientParams.useSkins) {
			int brd = border * 2;
			this.mapSkin.draw(matrix, posX, posY, mapWidth + brd, mapHeight + brd);
		}
		
		int centerX = mapX + mapWidth / 2;
		int centerY = mapY + mapHeight / 2;
		int iconSize = ClientParams.arrowIconSize;
		if (ClientParams.arrowIconType == DirectionArrow.Type.DIRECTION_ARROW) {
			float direction = ClientParams.rotateMap ? 180 : rotation;
			DirectionArrow.draw(centerX, centerY, iconSize, direction);
		} else {
			MapPlayerManager.getPlayer(minecraft.player).getIcon().draw(centerX, centerY, iconSize, true);
		}
		
		this.textManager.draw(matrix);
		
		RenderSystem.enableDepthTest();
	}
	
	private void drawMap() {
		MapCache mapData = MapCache.get();
		
		int scaledW = minimap.getScaledWidth();
		int scaledH = minimap.getScaledHeight();
		int cornerX = PosUtil.coordX() - scaledW / 2;
		int cornerZ = PosUtil.coordZ() - scaledH / 2;		
		int right = this.imgX + scaledW;
		
		int bottom;
		if (ClientParams.rotateMap) {
			bottom = (int) (this.imgY + scaledH * 1.42);
		} else {
			bottom = this.imgY + scaledH;
		}
		
		float scale = minimap.getScale();
		
		int picX = 0, picW = 0;
		while(picX < scaledW) {
			int cX = cornerX + picX;
			int picY = 0, picH = 0;
			while (picY < scaledH ) {				
				int cZ = cornerZ + picY;
				
				MapRegion region = mapData.getRegion(new BlockPos(cX, 0, cZ));
				
				picW = 512;
				picH = 512;
				int imgX = cX - (region.getX() << 9);
				int imgY = cZ - (region.getZ() << 9);
				
				if (picX + picW >= right) picW = right - picX;
				if (picY + picH >= bottom) picH = bottom - picY;
				if (imgX + picW >= 512) picW = 512 - imgX;
				if (imgY + picH >= 512) picH = 512 - imgY;
				
				double scX = (picX - 4) / scale;
				double scY = (picY - 4) / scale;
				
				region.draw(this.imgX + scX, this.imgY + scY, imgX, imgY, picW, picH, scale);
				
				picY += picH > 0 ? picH : 512;
			}
			
			picX += picW > 0 ? picW : 512;
		}
	}
}