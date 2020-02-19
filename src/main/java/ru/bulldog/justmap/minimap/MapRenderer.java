package ru.bulldog.justmap.minimap;

import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.config.Params;
import ru.bulldog.justmap.minimap.icon.EntityIcon;
import ru.bulldog.justmap.minimap.icon.PlayerIcon;
import ru.bulldog.justmap.minimap.icon.WaypointIcon;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.Drawer;
import ru.bulldog.justmap.util.Drawer.TextAlignment;
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
	protected int borderColor = Colors.GRAY;
	
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
		this.minimap = JustMap.MINIMAP;
		this.offset = Params.positionOffset;
		this.mapPosition = Params.mapPosition;
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
		offset = Params.positionOffset;
		mapPosition = Params.mapPosition;
		
		backingImage = minimap.getImage();
	
		mapW = minimap.getSize();
		mapH = minimap.getSize();
		
		if (Params.useSkins) {
			mapSkin = MapSkin.getSkin(Params.currentSkin);
			
			border = mapSkin.resizable ?
						  (int) (mapW * ((float)(mapSkin.border) / mapSkin.getWidth())) :
						  mapSkin.border;
		} else {
			border = 2;
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
			if (!(Params.showInChat && client.currentScreen instanceof ChatScreen)) {
				return;
			}
		}
		
		updateParams();
		
		if (texture == null) {
			texture = new NativeImageBackedTexture(backingImage);
			mapTexture = client.getTextureManager().registerDynamicTexture(JustMap.MODID + "_map_texture", texture);
		}
		
		RenderSystem.disableDepthTest();
		
		if (Params.useSkins) {
			mapSkin.draw(posX, posY, mapW + border * 2);
		} else {
			Drawer.fill(posX, posY, mapX + mapW + border, mapY + mapH + border, borderColor);
		}

		drawMap();

		if (Params.drawChunkGrid) {
			drawChunkGrid();
		}
		
		int arrowX = mapX + mapW / 2;
		int arrowY = mapY + mapH / 2;
		
		PlayerArrow.draw(arrowX, arrowY, client.player.headYaw);
		if (Minimap.allowEntityRadar()) {
			if (Minimap.allowPlayerRadar()) {
				for (PlayerIcon player : minimap.getPlayerIcons()) {
					player.draw(mapX, mapY);
				}
			}
			if (Minimap.allowCreatureRadar() || Minimap.allowHostileRadar()) {
				for (EntityIcon entity : minimap.getEntities()) {
					entity.draw(mapX, mapY);
				}
			}
		}
		for (WaypointIcon waypoint : minimap.getWaypoints()) {
			if (!waypoint.isHidden()) {
				waypoint.draw(mapX, mapY);
			}
		}
		
		textManager.draw();
		
		RenderSystem.enableDepthTest();
	}
	
	private void drawMap() {		
		client.getTextureManager().bindTexture(mapTexture);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(7, VertexFormats.POSITION_TEXTURE);
		
		double z = 0.09;
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		builder.vertex(mapX, mapY + mapH, z).texture(0, 1).next();
		builder.vertex(mapX + mapW, mapY + mapH, z).texture(1, 1).next();
		builder.vertex(mapX + mapW, mapY, z).texture(1, 0).next();
		builder.vertex(mapX, mapY, z).texture(0, 0).next();
		
		tessellator.draw();
	}
	
	private void drawChunkGrid() {
		int color = 0x66333333;
		int px = client.player.getBlockPos().getX();
		int pz = client.player.getBlockPos().getZ();
		
		float scale = Params.mapScale;
		
		int xOff = (int) ((((px / 16) * 16) - px) / scale);
		int yOff = (int) ((((pz / 16) * 16) - pz) / scale);
		
		int step = (int) (16 / scale);
		for (int cH = yOff; cH < mapH; cH += step) {
			int yp = mapY + cH;
			if (yp < mapY || yp > mapY + mapH) {
				continue;
			}
			Drawer.drawLine(mapX, yp, mapX + mapW, yp, color);
		}
	
		for (int v = xOff; v < mapW; v += step) {
			int xp = mapX + v;
			if (xp < mapX || xp >= mapX + mapW) {
				continue;
			}
			
			Drawer.drawLine(xp, mapY, xp, mapY + mapH, color);
		}
	}
}