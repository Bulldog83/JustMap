package ru.bulldog.justmap.client.render;

import java.util.List;

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
import ru.bulldog.justmap.map.ChunkGrid;
import ru.bulldog.justmap.map.DirectionArrow;
import ru.bulldog.justmap.map.MapPlayerManager;
import ru.bulldog.justmap.map.data.RegionData;
import ru.bulldog.justmap.map.data.WorldData;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
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
	
	private int winWidth, winHeight;
	private int mapWidth, mapHeight;
	private int scaledW, scaledH;
	private int centerX, centerY;
	private int lastX, lastZ;
	private int mapX, mapY;
	private int imgX, imgY;
	private int imgW, imgH;
	private float mapScale;
	private float rotation;
	private float offX, offY;
	private boolean mapRotation = false;
	private final Minimap minimap;
	private BlockPos.Mutable playerPos;
	private WorldData worldData;
	private ChunkGrid chunkGrid;
	private MapSkin mapSkin;
	private TextManager textManager;	
	private InfoText dirN = new MapText(TextAlignment.CENTER, "N");
	private InfoText dirS = new MapText(TextAlignment.CENTER, "S");
	private InfoText dirE = new MapText(TextAlignment.CENTER, "E");
	private InfoText dirW = new MapText(TextAlignment.CENTER, "W");
	
	public MapRenderer(Minimap map) {
		this.minimap = map;
		this.playerPos = new BlockPos.Mutable(0, 0, 0);
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
		int lastX = MathUtil.floor(DataUtil.doubleX());
		int lastZ = MathUtil.floor(DataUtil.doubleZ());
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
			this.centerX = mapX + mapWidth / 2;
			this.centerY = mapY + mapHeight / 2;
			
			if (mapRotation) {
				double mult = minimap.isBigMap() ? 1.9 : 1.44;
				this.scaledW = (int) Math.ceil(mapWidth * mapScale * mult);
				this.scaledH = (int) Math.ceil(mapHeight * mapScale * mult);
				this.imgW = minimap.getScaledWidth();
				this.imgH = minimap.getScaledHeight();
				this.imgX = (int) Math.ceil(mapX - (imgW - mapW) / 2F);
				this.imgY = (int) Math.ceil(mapY - (imgH - mapH) / 2F);
			} else {
				this.scaledW = minimap.getScaledWidth();
				this.scaledH = minimap.getScaledHeight();
				this.imgW = (int) (mapW * 1.125F);
				this.imgH = (int) (mapH * 1.125F);
				int deltaX = imgW - mapW;
				int deltaY = imgH - mapH;
				this.imgX = mapX - deltaX / 2;
				this.imgY = mapY - deltaY / 2;
				this.scaledW += deltaX * mapScale;
				this.scaledH += deltaY * mapScale;
			}
			
			if (chunkGrid == null) {
				this.chunkGrid = new ChunkGrid(lastX, lastZ, imgX, imgY, imgW, imgH, mapScale);
			} else {
				this.chunkGrid.updateRange(imgX, imgY, imgW, imgH, mapScale);
				this.chunkGrid.updateGrid();
			}
		}
		
		if (this.lastX != lastX || this.lastZ != lastZ) {
			this.lastX = lastX;
			this.lastZ = lastZ;
			this.chunkGrid.updateCenter(lastX, lastZ);
			this.chunkGrid.updateGrid();
			this.playerPos.set(lastX, DataUtil.coordY(), lastZ);
		}
		
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
			double rotate = MathUtil.correctAngle(rotation) + 180;
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

		this.offX = this.calcOffset(DataUtil.doubleX(), lastX, mapScale);
		this.offY = this.calcOffset(DataUtil.doubleZ(), lastZ, mapScale);
		
		List<MapIcon<?>> drawedEntities = minimap.getDrawedIcons(lastX, lastZ, centerX, centerY);
		List<WaypointIcon> drawedWaypoints = minimap.getWaypoints(playerPos, centerX, centerY);
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.disableDepthTest();
		RenderUtil.enableScissor();
		RenderUtil.applyScissor(scaledX, scaledY, scaledW, scaledH);
		
		RenderSystem.enableBlend();
		if (Minimap.isRound()) {
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
		RenderSystem.pushMatrix();
		RenderSystem.translatef(-offX, -offY, 0.0F);
		this.drawMap();
		if (ClientParams.showGrid) {
			this.chunkGrid.draw();
		}
		RenderSystem.popMatrix();
		VertexConsumerProvider.Immediate consumerProvider = minecraft.getBufferBuilders().getEntityVertexConsumers();
		matrices.push();
		matrices.translate(-offX, -offY, 0.0);
		for (MapIcon<?> icon : drawedEntities) {
			icon.draw(matrices, consumerProvider, mapX, mapY, rotation);
		}
		consumerProvider.draw();
		matrices.pop();
		RenderSystem.popMatrix();
		for (WaypointIcon icon : drawedWaypoints) {
			icon.draw(matrices, consumerProvider, mapX, mapY, offX, offY, rotation);
		}
		consumerProvider.draw();
		
		RenderUtil.disableScissor();
		
		if (mapSkin != null) {
			int skinX = minimap.getSkinX();
			int skinY = minimap.getSkinY();
			int brd = minimap.getBorder() * 2;
			this.mapSkin.draw(matrices, skinX, skinY, mapWidth + brd, mapHeight + brd);
		}
		
		RenderUtil.drawRightAlignedString(
				matrices, Double.toString(mapScale),
				mapX + mapWidth - 3, mapY + mapHeight - 10, Colors.WHITE);
		
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
		int cornerX = lastX - scaledW / 2;
		int cornerZ = lastZ - scaledH / 2;
		
		BlockPos.Mutable currentPos = new BlockPos.Mutable();
		
		int picX = 0;
		while(picX < scaledW) {
			int texW = 512;
			if (picX + texW > scaledW) texW = (int) (scaledW - picX);
			
			int picY = 0;
			int cX = cornerX + picX;
			while (picY < scaledH) {
				int texH = 512;
				if (picY + texH > scaledH) texH = (int) (scaledH - picY);
				
				int cZ = cornerZ + picY;
				RegionData region = worldData.getRegion(minimap, currentPos.set(cX, 0, cZ));
				region.swapLayer(minimap.getLayer(), minimap.getLevel());
				
				int texX = cX - (region.getX() << 9);
				int texY = cZ - (region.getZ() << 9);
				if (texX + texW >= 512) texW = 512 - texX;
				if (texY + texH >= 512) texH = 512 - texY;
				
				double scX = picX / mapScale;
				double scY = picY / mapScale;
				double scW = texW / mapScale;
				double scH = texH / mapScale;
				
				region.draw(imgX + scX, imgY + scY, scW, scH, texX, texY, texW, texH);
				
				picY += texH > 0 ? texH : 512;
			}
			
			picX += texW > 0 ? texW : 512;
		}
	}
	
	private float calcOffset(double x, double lastX, double scale) {
		return (float) ((x - lastX) / scale);
	}
}