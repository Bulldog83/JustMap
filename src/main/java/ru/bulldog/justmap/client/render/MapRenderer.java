package ru.bulldog.justmap.client.render;

import org.lwjgl.opengl.GL11;
import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.advancedinfo.InfoText;
import ru.bulldog.justmap.advancedinfo.MapText;
import ru.bulldog.justmap.advancedinfo.TextManager;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.enums.ArrowType;
import ru.bulldog.justmap.map.DirectionArrow;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.map.data.WorldData;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
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
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;

@Environment(EnvType.CLIENT)
public class MapRenderer {
	
	private final static MinecraftClient minecraft = DataUtil.getMinecraft();
	private final static Identifier roundMask = new Identifier(JustMap.MODID, "textures/round_mask.png");
	
	private int mapX, mapY;
	private int winWidth, winHeight;
	private int mapWidth, mapHeight;
	private int imgX, imgY;
	private int imgW, imgH;
	private float rotation;
	private float mapScale;
	private boolean mapRotation = false;

	private final Minimap minimap;
	private WorldData worldData;
	
	private MapSkin mapSkin;
	private TextManager textManager;	
	private InfoText dirN = new MapText(TextAlignment.CENTER, "N");
	private InfoText dirS = new MapText(TextAlignment.CENTER, "S");
	private InfoText dirE = new MapText(TextAlignment.CENTER, "E");
	private InfoText dirW = new MapText(TextAlignment.CENTER, "W");
	
	public MapRenderer(Minimap map) {
		this.minimap = map;
		this.textManager = minimap.getTextManager();
		this.textManager.setSpacing(12);
		this.textManager.add(dirN);
		this.textManager.add(dirS);
		this.textManager.add(dirE);
		this.textManager.add(dirW);
	}
	
	public void updateParams() {		
		this.worldData = minimap.getWorldData();
		this.mapSkin = minimap.getSkin();
		
		int winW = minecraft.getWindow().getWidth();
		int winH = minecraft.getWindow().getHeight();
		if (winWidth != winW || winHeight != winH) {
			minimap.updateMapParams();
			this.winWidth = winW;
			this.winHeight = winH;
		}
		
		int mapW = minimap.getWidth();
		int mapH = minimap.getHeight();
		int mapX = minimap.getMapX();
		int mapY = minimap.getMapY();
		float scale = minimap.getScale();
		boolean rotateMap = minimap.isRotated();
		if (mapWidth != mapW || mapHeight != mapH ||
			this.mapX != mapX || this.mapY != mapY ||
			mapScale != scale || mapRotation != rotateMap) {
			
			this.mapWidth = mapW;
			this.mapHeight = mapH;
			this.mapRotation = rotateMap;
			this.mapScale = scale;
			this.mapX = minimap.getMapX();
			this.mapY = minimap.getMapY();			
			this.imgW = minimap.getScaledWidth();
			if (imgW < mapWidth) {
				imgW = mapWidth;
			}
			this.imgH = minimap.getScaledHeight();
			if (imgH < mapHeight) {
				imgH = mapHeight;
			}
			this.imgX = mapX - (imgW - mapWidth) / 2;
			this.imgY = mapY - (imgH - mapHeight) / 2;
			this.imgW += 8;
			this.imgH += 8;
			this.imgX -= 4;
			this.imgY -= 4;
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
		
		this.rotation = 180;
		if (mapRotation) {
			this.rotation = minecraft.player.headYaw;
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
	
	public void draw(MatrixStack matrices) {
		if (!minimap.isMapVisible() || !JustMapClient.canMapping()) return;
		
		this.updateParams();
		
		if (worldData == null) return;
		
		int winH = minecraft.getWindow().getFramebufferHeight();
		double scale = minecraft.getWindow().getScaleFactor();
		
		int scaledX = (int) (mapX * scale);
		int scaledY = (int) (winH - (mapY + mapHeight) * scale);
		int scaledW = (int) (mapWidth * scale);
		int scaledH = (int) (mapHeight * scale);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableDepthTest();
		//RenderUtil.enableScissor();
		//RenderUtil.applyScissor(scaledX, scaledY, scaledW, scaledH);
		
		if (Minimap.isRound()) {
			RenderSystem.enableBlend();
			RenderSystem.colorMask(false, false, false, true);
			RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
			RenderSystem.clear(GL11.GL_COLOR_BUFFER_BIT, false);
			RenderSystem.colorMask(true, true, true, true);
			RenderUtil.bindTexture(roundMask);
			RenderUtil.startDraw();
			RenderUtil.addQuad(mapX, mapY, mapWidth, mapHeight);
			RenderUtil.endDraw();
			RenderSystem.blendFunc(GL11.GL_DST_ALPHA, GL11.GL_ONE_MINUS_DST_ALPHA);
		}

		RenderSystem.pushMatrix();
		if (mapRotation) {
			float moveX = mapX + mapWidth / 2.0F;
			float moveY = mapY + mapHeight / 2.0F;
			RenderSystem.translatef(moveX, moveY, 0.0F);
			RenderSystem.rotatef(-rotation + 180, 0.0F, 0.0F, 1.0F);
			RenderSystem.translatef(-moveX, -moveY, 0.0F);
		}
		float offX = (float) (DataUtil.doubleX() - minimap.getLastX()) / mapScale;
		float offY = (float) (DataUtil.doubleZ() - minimap.getLastZ()) / mapScale;
		RenderSystem.translatef(-offX, -offY, 0.0F);
		this.drawMap();
		VertexConsumerProvider.Immediate consumerProvider = minecraft.getBufferBuilders().getEntityVertexConsumers();
		for (MapIcon<?> icon : minimap.getDrawedIcons()) {
			icon.draw(matrices, consumerProvider, mapX, mapY, rotation);
		}
		consumerProvider.draw();
		RenderSystem.popMatrix();
		for (WaypointIcon icon : minimap.getWaypoints()) {
			icon.draw(matrices, consumerProvider, mapX, mapY, offX, offY, rotation);
		}
		consumerProvider.draw();
		
		//RenderUtil.disableScissor();
		
		if (mapSkin != null) {
			int skinX = minimap.getSkinX();
			int skinY = minimap.getSkinY();
			int brd = minimap.getBorder() * 2;
			this.mapSkin.draw(matrices, skinX, skinY, mapWidth + brd, mapHeight + brd);
		}
		
		RenderUtil.drawRightAlignedString(
				matrices, Float.toString(mapScale),
				mapX + mapWidth - 3, mapY + mapHeight - 10, Colors.WHITE);
		
		double centerX = mapX + mapWidth / 2.0;
		double centerY = mapY + mapHeight / 2.0;
		int iconSize = ClientParams.arrowIconSize;
		if (ClientParams.arrowIconType == ArrowType.DIRECTION_ARROW) {
			float direction = mapRotation ? 180 : minecraft.player.headYaw;
			DirectionArrow.draw(centerX, centerY, iconSize, direction);
		} else {
			MapPlayerManager.getPlayer(minecraft.player).getIcon().draw(centerX, centerY, iconSize, true);
		}
		
		this.textManager.draw(matrices);
		
		RenderSystem.enableDepthTest();
	}
	
	private void drawMap() {
		BlockPos center = DataUtil.currentPos();
		int cornerX = center.getX() - minimap.getScaledWidth() / 2 - 4;
		int cornerZ = center.getZ() - minimap.getScaledHeight() / 2 - 4;
		int right = imgX + imgW;
		int bottom = imgY + imgH;
		int cY = center.getY();
		
		int picX = 0, picW = 0;
		double drawX = imgX + picX;
		BlockPos.Mutable currentPos = new BlockPos.Mutable();
		while(drawX < right) {
			int cX = cornerX + picX;
			int picY = 0, picH = 0;
			double drawY = imgY + picY;
			while (drawY < bottom) {				
				int cZ = cornerZ + picY;
				
				RegionData region = worldData.getRegion(minimap, currentPos.set(cX, cY, cZ));
				region.swapLayer(minimap.getLayer(), minimap.getLevel());
				
				int textureX = cX - (region.getX() << 9);
				int textureY = cZ - (region.getZ() << 9);
				
				picW = (int) (512 * mapScale);
				picH = (int) (512 * mapScale);
				if (textureX + picW >= 512) picW = 512 - textureX;
				if (textureY + picH >= 512) picH = 512 - textureY;
				if (drawX + picW >= right) picW = (int) (right - drawX);
				if (drawY + picH >= bottom) picH = (int) (bottom - drawY);
				
				region.draw(drawX, drawY, textureX, textureY, picW, picH, mapScale);
				
				RenderUtil.drawLine(drawX, imgY, drawX, imgY + imgH, Colors.YELLOW);
				RenderUtil.drawLine(imgX, drawY, imgX + imgW, drawY, Colors.YELLOW);
				
				picY += picH > 0 ? picH : 512;
				drawY = imgY + (picY / mapScale);
			}
			picX += picW > 0 ? picW : 512;
			drawX = imgX + (picX / mapScale);
		}
		RenderUtil.drawLine(imgX, imgY, imgX + imgW, imgY, Colors.RED);
		RenderUtil.drawLine(imgX, imgY, imgX, imgY + imgH, Colors.RED);
		RenderUtil.drawLine(imgX, imgY + imgH, imgX + imgW, imgY + imgH, Colors.RED);
		RenderUtil.drawLine(imgX + imgW, imgY, imgX + imgW, imgY + imgH, Colors.RED);
	}
}