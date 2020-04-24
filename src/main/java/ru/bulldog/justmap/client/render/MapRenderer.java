package ru.bulldog.justmap.client.render;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.DirectionArrow;
import ru.bulldog.justmap.map.icon.EntityIcon;
import ru.bulldog.justmap.map.icon.PlayerIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.minimap.ChunkGrid;
import ru.bulldog.justmap.map.minimap.MapPosition;
import ru.bulldog.justmap.map.minimap.MapSkin;
import ru.bulldog.justmap.map.minimap.MapText;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.minimap.TextManager;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.Line.Point;
import ru.bulldog.justmap.util.math.MathUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MapRenderer {
	
	private static MapRenderer instance;
	
	protected MapPosition mapPosition;
	protected int border = 2;
	
	private int offset;
	private int posX, posY;
	private int mapX, mapY;
	private int mapW, mapH;
	private float rotation;

	private final Minimap minimap;
	
	private NativeImage backingImage;
	private NativeImageBackedTexture texture;
	private Identifier mapTexture;
	
	private TextManager textManager;
	
	private MapText dirN = new MapText(TextAlignment.CENTER, "N");
	private MapText dirS = new MapText(TextAlignment.CENTER, "S");
	private MapText dirE = new MapText(TextAlignment.CENTER, "E");
	private MapText dirW = new MapText(TextAlignment.CENTER, "W");
	
	private final MinecraftClient client = MinecraftClient.getInstance();
	
	private MapSkin mapSkin;
	private ChunkGrid chunkGrid;
	
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
		
		backingImage = minimap.getImage();		
		
		int winW = client.getWindow().getScaledWidth();
		int winH = client.getWindow().getScaledHeight();
		
		offset = ClientParams.positionOffset;
		mapPosition = ClientParams.mapPosition;
		
		mapW = minimap.getWidth();
		mapH = minimap.getHeight();
		posX = offset;
		posY = offset;
		mapX = posX + border;
		mapY = posY + border;
		
		rotation = client.player.headYaw;
		
		TextManager.TextPosition textPos = TextManager.TextPosition.UNDER;
		
		switch (mapPosition) {
			case TOP_LEFT:
				break;
			case TOP_CENTER:
				mapX = winW / 2 - mapW / 2;
				posX = mapX - border;
				break;
			case TOP_RIGHT:
				mapX = winW - offset - mapW - border;
				posX = mapX - border;
				break;
			case MIDDLE_RIGHT:
				mapX = winW - offset - mapW - border;
				mapY = winH / 2 - mapH / 2;
				posX = mapX - border;
				posY = mapY - border;
				break;
			case MIDDLE_LEFT:
				mapY = winH / 2 - mapH / 2;
				posY = mapY - border;
				break;
			case BOTTOM_LEFT:
				textPos = TextManager.TextPosition.ABOVE;
				mapY = winH - offset - mapH - border;
				posY = mapY - border;
				break;
			case BOTTOM_RIGHT:
				textPos = TextManager.TextPosition.ABOVE;
				mapX = winW - offset - mapW - border;
				posX = mapX - border;
				mapY = winH - offset - mapH - border;
				posY = mapY - border;
				break;
		}
		
		textManager.setPosition(
			mapX, mapY + (textPos == TextManager.TextPosition.UNDER && minimap.isMapVisible() ?
				mapH + border + 3 :
				-(border + 3))
		);
		textManager.setDirection(textPos);
		textManager.setSpacing(12);
		
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
		
		textManager.add(dirN, pointN.x, pointN.y - 5);
		textManager.add(dirS, pointS.x, pointS.y - 5);
		textManager.add(dirE, pointE.x, pointE.y - 5);
		textManager.add(dirW, pointW.x, pointW.y - 5);
		
		if (ClientParams.useSkins) {
			mapSkin = MapSkin.getSkin(ClientParams.currentSkin);
			
			border = mapSkin.resizable ?
						  (int) (mapW * ((float)(mapSkin.border) / mapSkin.getWidth())) :
						  mapSkin.border;
		}
	}
	
	private void calculatePos(Point center, Point dir, int mr, int mb, double angle) {		
		int posX = (int) (center.x + (dir.x - center.x) * Math.cos(angle) - (dir.y - center.y) * Math.sin(angle));
		int posY = (int) (center.y + (dir.y - center.y) * Math.cos(angle) + (dir.x - center.x) * Math.sin(angle));
		posX = MathUtil.clamp(posX, mapX, mr);
		posY = MathUtil.clamp(posY, mapY, mb);
		
		dir.x = posX; dir.y = posY;
	}
	
	public void markDirty() {
		NativeImage img = minimap.getImage();
		if (img != backingImage) {
			backingImage = img;
			if (texture != null) {
				texture.close();
			}
			texture = null;
		}
		
		if (texture != null) {
			texture.upload();
		}
	}
	
	public void draw(MatrixStack matrixStack) {
		if (!minimap.isMapVisible() || client.player == null) {
			return;
		}
		
		updateParams();
		
		if (texture == null) {
			texture = new NativeImageBackedTexture(backingImage);
			mapTexture = client.getTextureManager().registerDynamicTexture(JustMap.MODID + "_map_texture", texture);
		}
		
		RenderSystem.disableDepthTest();
		
		if (ClientParams.useSkins) {
			mapSkin.draw(matrixStack, posX, posY, mapW + border * 2);
		}

		int winH = client.getWindow().getFramebufferHeight();
		double scale = client.getWindow().getScaleFactor();
		
		int scaledX = (int) (mapX * scale);
		int scaledY = (int) (winH - (mapY + mapH) * scale);
		int scaledW = (int) (mapW * scale);
		int scaledH = (int) (mapH * scale);
		
		GL11.glEnable(GL11.GL_SCISSOR_TEST);
		GL11.glScissor(scaledX, scaledY, scaledW, scaledH);
		
		drawMap();

		if (ClientParams.drawChunkGrid) {
			drawChunkGrid();
		}
		if (Minimap.allowEntityRadar()) {
			if (Minimap.allowPlayerRadar()) {
				for (PlayerIcon player : minimap.getPlayerIcons()) {
					player.draw(matrixStack, mapX, mapY, rotation);
				}
			}
			if (Minimap.allowCreatureRadar() || Minimap.allowHostileRadar()) {
				for (EntityIcon entity : minimap.getEntities()) {
					entity.draw(matrixStack, mapX, mapY, rotation);
				}
			}
		}
		for (WaypointIcon waypoint : minimap.getWaypoints()) {
			if (!waypoint.isHidden()) {
				waypoint.draw(matrixStack, mapX, mapY, rotation);
			}
		}
		
		int arrowX = mapX + mapW / 2;
		int arrowY = mapY + mapH / 2;
		
		DirectionArrow.draw(arrowX, arrowY, ClientParams.rotateMap ? 180 : rotation);
		
		GL11.glDisable(GL11.GL_SCISSOR_TEST);
		
		textManager.draw(matrixStack);
		
		RenderSystem.enableDepthTest();
	}
	
	private void drawMap() {		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(7, VertexFormats.POSITION_TEXTURE);
		
		double z = 0.09;
		
		client.getTextureManager().bindTexture(mapTexture);
		
		float f1 = 0.0F, f2 = 1.0F;		
		if (ClientParams.rotateMap) {
			f1 = 0.15F;
			f2 = 0.85F;
			
			RenderSystem.enableTexture();
			RenderSystem.matrixMode(GL11.GL_TEXTURE);
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.5F, 0.5F, 0);
			RenderSystem.rotatef(rotation + 180, 0, 0, 1.0F);
			RenderSystem.translatef(-0.5F, -0.5F, 0);
		}
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);		
		builder.vertex(mapX, mapY, z).texture(f1, f1).next();
		builder.vertex(mapX, mapY + mapH, z).texture(f1, f2).next();
		builder.vertex(mapX + mapW, mapY + mapH, z).texture(f2, f2).next();
		builder.vertex(mapX + mapW, mapY, z).texture(f2, f1).next();
		
		tessellator.draw();
		
		if (ClientParams.rotateMap) {		
			RenderSystem.popMatrix();
			RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		}
	}
	
	private void drawChunkGrid() {
		int px = (int) client.player.getPos().getX();
		int pz = (int) client.player.getPos().getZ();
		
		if (ClientParams.rotateMap) {
			int picW = (int) (mapW * 1.5);
			int picH = (int) (mapH * 1.5);
			int picX = mapX - (picW - mapW) / 2;
			int picY = mapY - (picH - mapH) / 2;			
			int centerX = mapX + mapW / 2;
			int centerY = mapY + mapH / 2;
			
			chunkGrid = new ChunkGrid(px, pz, picX, picY, picW, picH);
			chunkGrid.draw(centerX, centerY, rotation);
		} else {		
			chunkGrid = new ChunkGrid(px, pz, mapX, mapY, mapW, mapH);
			chunkGrid.draw();
		}
	}
}