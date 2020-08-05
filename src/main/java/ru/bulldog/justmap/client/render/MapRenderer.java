package ru.bulldog.justmap.client.render;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.DirectionArrow;
import ru.bulldog.justmap.map.data.MapCache;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.icon.EntityIcon;
import ru.bulldog.justmap.map.icon.PlayerHeadIcon;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.minimap.MapPosition;
import ru.bulldog.justmap.map.minimap.MapSkin;
import ru.bulldog.justmap.map.minimap.MapText;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.minimap.TextManager;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.PosUtil;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.Line.Point;
import ru.bulldog.justmap.util.math.MathUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class MapRenderer {
	
	private static MapRenderer instance;
	
	protected MapPosition mapPosition;
	protected int border = 2;
	
	private int offset;
	private int posX, posY;
	private int mapX, mapY;
	private int mapW, mapH;
	private int imgX, imgY;
	private int imgW, imgH;
	private float rotation;
	private int lastX;
	private int lastZ;

	private final Minimap minimap;
	
	private TextManager textManager;
	
	private MapText dirN = new MapText(TextAlignment.CENTER, "N");
	private MapText dirS = new MapText(TextAlignment.CENTER, "S");
	private MapText dirE = new MapText(TextAlignment.CENTER, "E");
	private MapText dirW = new MapText(TextAlignment.CENTER, "W");
	
	private final MinecraftClient client = MinecraftClient.getInstance();
	
	private MapSkin mapSkin;
	
	public static MapRenderer getInstance() {
		if (instance == null) {
			instance = new MapRenderer();
		}
		
		return instance;
	}
	
	private MapRenderer() {
		this.minimap = JustMapClient.MAP;
		this.offset = ClientParams.positionOffset;
		this.mapPosition = ClientParams.mapPosition;
		this.textManager = this.minimap.getTextManager();
	}
	
	public int getX() {
		return this.posX;
	}
	
	public int getY() {
		return this.posY;
	}
	
	public int getBorder() {
		return this.border;
	}
	
	public void updateParams() {		
		this.mapW = minimap.getWidth();
		this.mapH = minimap.getHeight();
		
		int border = this.border;
		if (ClientParams.useSkins) {
			this.mapSkin = MapSkin.getSkin(ClientParams.currentSkin);			
			this.border = this.mapSkin.border;
			
			this.mapSkin.getRenderData().updateScale();
			
			double scale = this.mapSkin.getRenderData().scaleFactor;
			border = (int) (this.border * scale);
		}
		
		int winW = client.getWindow().getScaledWidth();
		int winH = client.getWindow().getScaledHeight();
		
		this.offset = ClientParams.positionOffset;
		this.mapPosition = ClientParams.mapPosition;
		
		this.posX = offset;
		this.posY = offset;
		this.mapX = posX + border;
		this.mapY = posY + border;
		
		this.rotation = client.player.headYaw;
		
		TextManager.TextPosition textPos = TextManager.TextPosition.UNDER;
		
		switch (mapPosition) {
			case TOP_LEFT:
				break;
			case TOP_CENTER:
				this.mapX = winW / 2 - mapW / 2;
				this.posX = mapX - border;
				break;
			case TOP_RIGHT:
				this.mapX = winW - offset - mapW - border;
				this.posX = mapX - border;
				break;
			case MIDDLE_RIGHT:
				this.mapX = winW - offset - mapW - border;
				this.mapY = winH / 2 - mapH / 2;
				this.posX = mapX - border;
				this.posY = mapY - border;
				break;
			case MIDDLE_LEFT:
				this.mapY = winH / 2 - mapH / 2;
				this.posY = mapY - border;
				break;
			case BOTTOM_LEFT:
				textPos = TextManager.TextPosition.ABOVE;
				this.mapY = winH - offset - mapH - border;
				this.posY = mapY - border;
				break;
			case BOTTOM_RIGHT:
				textPos = TextManager.TextPosition.ABOVE;
				this.mapX = winW - offset - mapW - border;
				this.posX = mapX - border;
				this.mapY = winH - offset - mapH - border;
				this.posY = mapY - border;
				break;
		}
		
		if (ClientParams.rotateMap) {
			this.imgW = (int) (mapW * 1.42);
			this.imgH = (int) (mapH * 1.42);
			this.imgX = mapX - (imgW - mapW) / 2;
			this.imgY = mapY - (imgH - mapH) / 2;
		} else {
			this.imgW = this.mapW;
			this.imgH = this.mapH;
			this.imgX = this.mapX;
			this.imgY = this.mapY;
		}
		
		this.textManager.setPosition(
			mapX, mapY + (textPos == TextManager.TextPosition.UNDER && minimap.isMapVisible() ?
				mapH + border + 3 :
				-(border + 3))
		);
		this.textManager.setDirection(textPos);
		this.textManager.setSpacing(12);
		
		int centerX = mapX + mapW / 2;
		int centerY = mapY + mapH / 2;
		int mapR = mapX + mapW;
		int mapB = mapY + mapH;
		
		Point center = new Point(centerX, centerY);
		Point pointN = new Point(centerX, mapY);
		Point pointS = new Point(centerX, mapB);
		Point pointE = new Point(mapR, centerY);
		Point pointW = new Point(mapX, centerY);
		
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
			
			calculatePos(center, pointN, mapR, mapB, angle);
			calculatePos(center, pointS, mapR, mapB, angle);
			calculatePos(center, pointE, mapR, mapB, angle);
			calculatePos(center, pointW, mapR, mapB, angle);
		}
		
		this.textManager.add(dirN, pointN.x, pointN.y - 5);
		this.textManager.add(dirS, pointS.x, pointS.y - 5);
		this.textManager.add(dirE, pointE.x, pointE.y - 5);
		this.textManager.add(dirW, pointW.x, pointW.y - 5);
	}
	
	private void calculatePos(Point center, Point dir, int mr, int mb, double angle) {		
		int posX = (int) (center.x + (dir.x - center.x) * Math.cos(angle) - (dir.y - center.y) * Math.sin(angle));
		int posY = (int) (center.y + (dir.y - center.y) * Math.cos(angle) + (dir.x - center.x) * Math.sin(angle));
		posX = MathUtil.clamp(posX, mapX, mr);
		posY = MathUtil.clamp(posY, mapY, mb);
		
		dir.x = posX; dir.y = posY;
	}
	
	public void draw() {
		if (!minimap.isMapVisible() || client.player == null) {
			return;
		}
		
		this.updateParams();
		
		int winH = client.getWindow().getFramebufferHeight();
		double scale = client.getWindow().getScaleFactor();
		
		int scaledX = (int) (mapX * scale);
		int scaledY = (int) (winH - (mapY + mapH) * scale);
		int scaledW = (int) (mapW * scale);
		int scaledH = (int) (mapH * scale);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableDepthTest();
		
		if (ClientParams.useSkins) {
			double skinScale = this.mapSkin.getRenderData().scaleFactor;
			int brd = (int) ((border * 2) * skinScale);
			this.mapSkin.draw(posX, posY, mapW + brd, mapH + brd);
		}
		
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
					player.draw(mapX, mapY, offX, offY, rotation);
				}
			}
			if (Minimap.allowCreatureRadar() || Minimap.allowHostileRadar()) {
				for (EntityIcon entity : minimap.getEntities()) {
					entity.draw(mapX, mapY, offX, offY, rotation);
				}
			}
		}
		
		DrawHelper.DRAWER.drawRightAlignedString(
				client.textRenderer, Float.toString(minimap.getScale()),
				mapX + mapW - 3, mapY + mapH - 10, Colors.WHITE);
		
		for (WaypointIcon waypoint : minimap.getWaypoints()) {
			if (!waypoint.isHidden()) {
				waypoint.draw(mapX, mapY, offX, offY, rotation);
			}
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		int centerX = mapX + mapW / 2;
		int centerY = mapY + mapH / 2;
		int iconSize = ClientParams.arrowIconSize;
		if (ClientParams.arrowIconType == DirectionArrow.Type.DIRECTION_ARROW) {
			float direction = ClientParams.rotateMap ? 180 : rotation;
			DirectionArrow.draw(centerX, centerY, iconSize, direction);
		} else {
			PlayerHeadIcon.getIcon(client.player).draw(centerX, centerY, iconSize, true);
		}
		
		this.textManager.draw();
		
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