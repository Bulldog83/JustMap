package ru.bulldog.justmap.client.render;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.InfoText;
import ru.bulldog.justmap.advancedinfo.MapText;
import ru.bulldog.justmap.advancedinfo.TextManager;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.enums.TextPosition;
import ru.bulldog.justmap.enums.ArrowType;
import ru.bulldog.justmap.map.DirectionArrow;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.map.data.WorldData;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.data.RegionData;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.minimap.skin.MapSkin;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.Point;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.Window;
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
	private boolean isRound = false;

	private final Minimap minimap;
	private WorldData worldData;
	
	private MapSkin mapSkin;
	private TextManager textManager;	
	private InfoText dirN = new MapText(TextAlignment.CENTER, "N");
	private InfoText dirS = new MapText(TextAlignment.CENTER, "S");
	private InfoText dirE = new MapText(TextAlignment.CENTER, "E");
	private InfoText dirW = new MapText(TextAlignment.CENTER, "W");
	
	private final MinecraftClient minecraft = DataUtil.getMinecraft();
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
		this.minimap.updateMapParams();
		this.worldData = this.minimap.getWorldData();
		
		this.isRound = !minimap.isBigMap() && Minimap.isRound();
		int border = 0;
		if (ClientParams.useSkins) {
			if (minimap.isBigMap()) {
				this.mapSkin = MapSkin.getBigMapSkin();
			} else {
				this.mapSkin = MapSkin.getCurrentSkin();
			}
			if (isRound) {
				double scale = (double) mapWidth / mapSkin.getWidth();
				this.mapSkin.getRenderData().updateScale(scale);
				border = (int) (mapSkin.border * scale);
			} else {
				this.mapSkin.getRenderData().updateScale();
				double scale = mapSkin.getRenderData().scaleFactor;
				border = (int) (mapSkin.border * scale);
			}
		}
		
		Window window = minecraft.getWindow();
		int winW = window.getScaledWidth();
		int winH = window.getScaledHeight();
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
			
			TextPosition textPos = TextPosition.UNDER;

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
					textPos = TextPosition.ABOVE;
					this.mapY = winH - offset - mapHeight - border;
					this.posY = mapY - border;
					break;
				case BOTTOM_RIGHT:
					textPos = TextPosition.ABOVE;
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
				mapX, mapY + (textPos == TextPosition.UNDER ?
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
			int len = (int) (Minimap.isRound() ? radius.lenght() : corner.lenght());
			
			pointN.y = centerY - len;
			pointS.y = centerY + len;
			pointE.x = centerX + len;
			pointW.x = centerX - len;
			
			this.calculatePos(center, pointN, mapR, mapB, angle);
			this.calculatePos(center, pointS, mapR, mapB, angle);
			this.calculatePos(center, pointE, mapR, mapB, angle);
			this.calculatePos(center, pointW, mapR, mapB, angle);
		}
		
		this.dirN.setPos((int) pointN.x, (int) pointN.y - 5);
		this.dirS.setPos((int) pointS.x, (int) pointS.y - 5);
		this.dirE.setPos((int) pointE.x, (int) pointE.y - 5);
		this.dirW.setPos((int) pointW.x, (int) pointW.y - 5);
	}
	
	private void calculatePos(Point center, Point dir, int mr, int mb, double angle) {		
		Point pos = MathUtil.circlePos(dir, center, angle);
		int posX = (int) MathUtil.clamp(pos.x, mapX, mr);
		int posY = (int) MathUtil.clamp(pos.y, mapY, mb);
		
		dir.x = posX; dir.y = posY;
	}
	
	public void draw() {
		if (!minimap.isMapVisible() || !JustMapClient.canMapping()) return;
		
		this.updateParams();
		
		if (worldData == null) return;
		
		int winH = minecraft.getWindow().getFramebufferHeight();
		double scale = minecraft.getWindow().getScaleFactor();
		
		int scaledX = (int) (mapX * scale);
		int scaledY = (int) (winH - (mapY + mapHeight) * scale);
		int scaledW = (int) (mapWidth * scale);
		int scaledH = (int) (mapHeight * scale);
		
		
		if (this.minimap.posChanged) {
			this.lastX = minimap.getLasX();
			this.lastZ = minimap.getLastZ();
			this.minimap.posChanged = false;
		}
		
		RenderSystem.disableDepthTest();
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(scaledX, scaledY, scaledW, scaledH);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		float mult = 1 / minimap.getScale();		
		float offX = (float) (DataUtil.doubleX() - this.lastX) * mult;
		float offY = (float) (DataUtil.doubleZ() - this.lastZ) * mult;
		
		if (isRound) {
			RenderSystem.enableBlend();
			RenderSystem.colorMask(false, false, false, true);
			RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
			RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);
			RenderSystem.colorMask(true, true, true, true);
			RenderUtil.bindTexture(this.roundMask);
			RenderUtil.startDraw();
			RenderUtil.addQuad(mapX, mapY, mapWidth, mapHeight);
			RenderUtil.endDraw();
			RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
		}
		
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
		
		MatrixStack matrices = new MatrixStack();
		for (MapIcon<?> icon : minimap.getDrawedIcons()) {
			icon.draw(matrices, mapX, mapY, offX, offY, rotation);
		}
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		if (ClientParams.useSkins) {
			int brd = border * 2;
			this.mapSkin.draw(matrices, posX, posY, mapWidth + brd, mapHeight + brd);
		}
		
		RenderUtil.drawRightAlignedString(
				Float.toString(minimap.getScale()),
				mapX + mapWidth - 3, mapY + mapHeight - 10, Colors.WHITE);
		
		int centerX = mapX + mapWidth / 2;
		int centerY = mapY + mapHeight / 2;
		int iconSize = ClientParams.arrowIconSize;
		if (ClientParams.arrowIconType == ArrowType.DIRECTION_ARROW) {
			float direction = ClientParams.rotateMap ? 180 : rotation;
			DirectionArrow.draw(centerX, centerY, iconSize, direction);
		} else {
			MapPlayerManager.getPlayer(minecraft.player).getIcon().draw(centerX, centerY, iconSize, true);
		}
		
		this.textManager.draw();
		
		RenderSystem.enableDepthTest();
	}
	
	private void drawMap() {
		int scaledW = this.minimap.getScaledWidth();
		int scaledH = this.minimap.getScaledHeight();
		int cornerX = DataUtil.coordX() - scaledW / 2;
		int cornerZ = DataUtil.coordZ() - scaledH / 2;		
		int right = this.imgX + scaledW;
		
		int bottom;
		if (ClientParams.rotateMap) {
			bottom = (int) (this.imgY + scaledH * 1.42);
		} else {
			bottom = this.imgY + scaledH;
		}
		
		float scale = this.minimap.getScale();
		
		BlockPos center = DataUtil.currentPos();
		BlockPos.Mutable currentPos = new BlockPos.Mutable();
		int cY = center.getY();
		
		int picX = 0, picW = 0;
		while(picX < scaledW) {
			int cX = cornerX + picX;
			int picY = 0, picH = 0;
			while (picY < scaledH ) {				
				int cZ = cornerZ + picY;
				
				RegionData region = this.worldData.getRegion(minimap, currentPos.set(cX, cY, cZ));
				region.swapLayer(minimap.getLayer(), minimap.getLevel());
				
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