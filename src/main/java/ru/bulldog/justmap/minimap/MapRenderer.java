package ru.bulldog.justmap.minimap;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.icon.EntityIcon;
import ru.bulldog.justmap.minimap.icon.PlayerIcon;
import ru.bulldog.justmap.minimap.icon.WaypointIcon;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
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
		offset = ClientParams.positionOffset;
		mapPosition = ClientParams.mapPosition;
		
		backingImage = minimap.getImage();
	
		mapW = minimap.getMapSize();
		mapH = minimap.getMapSize();
		
		if (ClientParams.useSkins) {
			mapSkin = MapSkin.getSkin(ClientParams.currentSkin);
			
			border = mapSkin.resizable ?
						  (int) (mapW * ((float)(mapSkin.border) / mapSkin.getWidth())) :
						  mapSkin.border;
		}		
		
		posX = offset;
		posY = offset;
		mapX = posX + border;
		mapY = posY + border;
		int winW = client.getWindow().getScaledWidth();
		int winH = client.getWindow().getScaledHeight();
	
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
		
		textManager.add(dirN, centerX, centerY - mapH / 2 - 3);
		textManager.add(dirS, centerX, centerY + mapH / 2 - 3);
		textManager.add(dirE, centerX + mapW / 2, centerY - 5);
		textManager.add(dirW, centerX - mapW / 2, centerY - 5);
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
	
	public void draw() {
		if (!minimap.isMapVisible() || client.player == null) {
			return;
		}
		
		if (client.currentScreen != null) {
			if (!(ClientParams.showInChat && client.currentScreen instanceof ChatScreen)) {
				return;
			}
		}
		
		updateParams();
		
		if (texture == null) {
			texture = new NativeImageBackedTexture(backingImage);
			mapTexture = client.getTextureManager().registerDynamicTexture(JustMap.MODID + "_map_texture", texture);
		}
		
		RenderSystem.disableDepthTest();
		
		if (ClientParams.useSkins) {
			mapSkin.draw(posX, posY, mapW + border * 2);
		}

		drawMap();

		float rotation = client.player.headYaw;
		if (ClientParams.drawChunkGrid) {
			drawChunkGrid();
		}
		if (Minimap.allowEntityRadar()) {
			if (Minimap.allowPlayerRadar()) {
				for (PlayerIcon player : minimap.getPlayerIcons()) {
					player.draw(mapX, mapY, rotation);
				}
			}
			if (Minimap.allowCreatureRadar() || Minimap.allowHostileRadar()) {
				for (EntityIcon entity : minimap.getEntities()) {
					entity.draw(mapX, mapY, rotation);
				}
			}
		}
		for (WaypointIcon waypoint : minimap.getWaypoints()) {
			if (!waypoint.isHidden()) {
				waypoint.draw(mapX, mapY, rotation);
			}
		}
		
		int arrowX = mapX + mapW / 2;
		int arrowY = mapY + mapH / 2;
		
		DirectionArrow.draw(arrowX, arrowY, rotation);
		
		textManager.draw();
		
		RenderSystem.enableDepthTest();
	}
	
	private void drawMap() {		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(7, VertexFormats.POSITION_TEXTURE);
		
		double z = 0.09;
		float angle = client.player.headYaw + 180;
		
		client.getTextureManager().bindTexture(mapTexture);
		
		float f1 = 0, f2 = 1;		
		if (ClientParams.rotateMap) {		
			f1 = 0.15F;
			f2 = 0.85F;
			
			RenderSystem.enableTexture();
			RenderSystem.matrixMode(GL11.GL_TEXTURE);
			RenderSystem.pushMatrix();
			RenderSystem.translatef(0.5F, 0.5F, 0);
			RenderSystem.rotatef(angle, 0, 0, 1.0F);
			RenderSystem.translatef(-0.5F, -0.5F, 0);
		}
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);		
		builder.vertex(mapX, mapY + mapH, z).texture(f1, f2).next();
		builder.vertex(mapX + mapW, mapY + mapH, z).texture(f2, f2).next();
		builder.vertex(mapX + mapW, mapY, z).texture(f2, f1).next();
		builder.vertex(mapX, mapY, z).texture(f1, f1).next();
		
		tessellator.draw();
		
		if (ClientParams.rotateMap) {		
			RenderSystem.popMatrix();
			RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		}

	}
	
	private void drawChunkGrid() {
		int color = 0x66333333;
		int px = client.player.getBlockPos().getX();
		int pz = client.player.getBlockPos().getZ();
		
		float scale = ClientParams.mapScale;
		
		int xOff = (int) ((((px / 16) * 16) - px) / scale);
		int yOff = (int) ((((pz / 16) * 16) - pz) / scale);
		
		int step = (int) (16 / scale);
		for (int cH = yOff; cH < mapH; cH += step) {
			int yp = mapY + cH;
			if (yp < mapY || yp > mapY + mapH) {
				continue;
			}
			DrawHelper.drawLine(mapX, yp, mapX + mapW, yp, color);
		}
	
		for (int v = xOff; v < mapW; v += step) {
			int xp = mapX + v;
			if (xp < mapX || xp >= mapX + mapW) {
				continue;
			}
			
			DrawHelper.drawLine(xp, mapY, xp, mapY + mapH, color);
		}
	}
}