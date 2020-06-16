package ru.bulldog.justmap.map.minimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.DrawHelper;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MapSkin extends Sprite {

	private final static SpriteAtlasTexture ATLAS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/map_skins.png"));
	private final static List<MapSkin> SKINS = new ArrayList<>();
	
	private final RenderData renderData;
	
	public final int id, border;
	public final boolean resizable;
	public final boolean repeating;
	public final String name;
	
	private final static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	
	private MapSkin(int id, String name, Identifier texture, int w, int h, int border, boolean resize, boolean repeat) {
		super(ATLAS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	
		this.id = id;
		this.border = border;
		this.resizable = resize;
		this.repeating = repeat;
		this.name = name;
		
		this.renderData = new RenderData();
	}
	
	public static void addSkin(String name, Identifier texture, int w, int h, int border, boolean resizable, boolean repeat) {
		int id = SKINS.size();
		SKINS.add(id, new MapSkin(id, name, texture, w, h, border, resizable, repeat));
	}
	
	public static void addSkin(String name, Identifier texture, int w, int h, int border, boolean resizable) {
		addSkin(name, texture, w, h, border, resizable, false);
	}
	
	public static void addSkin(String name, Identifier texture, int w, int h, int border) {
		addSkin(name, texture, w, h, border, false, false);
	}
	
	public static MapSkin getSkin(int id) {
		try {
			return SKINS.get(id);
		} catch(IndexOutOfBoundsException ex) {
			JustMap.LOGGER.logWarning(ex.getMessage());
		}
		
		return SKINS.get(0);
	}
	
	public Identifier getTexture() {
		return this.getId();
	}
	
	private void bindPavedTexture(int w, int h) {
		Identifier id = new Identifier(JustMap.MODID, String.format("skin_%d_%dx%d", this.id, w, h));
		if (textureManager.getTexture(id) == null) {
			NativeImage pavedImage = new NativeImage(w, h, false);
			int imgW = this.images[0].getWidth();
			int imgH = this.images[0].getHeight();
			int x = 0, y = 0;
			while(y < h) {
				while(x < w) {
					ImageUtil.writeTile(pavedImage, this.images[0], x, y);
					x += imgW;
				}
				y += imgH;
				x = 0;
			}
			textureManager.registerTexture(id, new NativeImageBackedTexture(pavedImage));
		}
		textureManager.bindTexture(id);
	}
	
	public void draw(MatrixStack matrixStack, int x, int y, int w, int h) {
		if (resizable) {
			textureManager.bindTexture(this.getTexture());
			DrawHelper.drawSkin(matrixStack, this, x, y, w, h);
		} else {
			this.bindPavedTexture(w, h);
			DrawHelper.drawSprite(matrixStack, x, y, 0, w, h, this);
		}
	}
	
	public void draw(MatrixStack matrixStack, int x, int y, int size) {
		this.draw(matrixStack, x, y, size, size);
	}
	
	public Text getName() {
		return new LiteralText(this.name);
	}
	
	@Override
	public String toString() {
		return this.getName().asString();
	}
	
	public static MapSkin getSkinByName(String name) {
		for (MapSkin skin : SKINS) {
			if (skin.name.equals(name)) {
				return skin;
			}
		}
		
		return null;
	}
	
	public static List<MapSkin> getSkins() {
		return Collections.unmodifiableList(SKINS);
	}
	
	public static MapSkin getCurrentSkin() {
		return getSkin(ClientParams.currentSkin);
	}
	
	public RenderData getRenderData() {
		return this.renderData;
	}
	
	public final class RenderData {
		public double x, y;
		public double scaleFactor = 1;
		public double leftC, rightC;
		public double topC, bottomC;
		public float width, height;
		public float scaledBorder;
		public float hSide, vSide;		
		public float leftU, rightU;
		public float topV, bottomV;
		public float tail, tailU;
		
		private RenderData() {}
		
		public void calculate(double x, double y, float w, float h, double scale) {
			int spriteW = MapSkin.this.getWidth();
			int spriteH = MapSkin.this.getHeight();
			int border = MapSkin.this.border;
			
			float sw = (spriteW * 10) / 16 - border * 2;
			float sTail = (spriteW - border * 2) - sw;
			double right = x + w;
			double bottom = y + h;
			
			this.x = x;
			this.y = y;
			this.width = w;
			this.height = h;			
			this.scaleFactor = scale;
			this.scaledBorder = (float) (border * scaleFactor);			
			this.hSide = w - scaledBorder * 2;
			this.vSide = h - scaledBorder * 2;
			this.leftC = x + scaledBorder;
			this.rightC = right - scaledBorder;
			this.topC = y + scaledBorder;
			this.bottomC = bottom - scaledBorder;			
			this.leftU = (float) border / spriteW;
			this.rightU = (float) (spriteW - border) / spriteW;
			this.topV = (float) border / spriteH;
			this.bottomV = (float) (spriteH - border) / spriteH;
			this.tail = hSide - vSide;
			this.tailU = (border + sTail) / spriteW;
		}
	}
	
	static {
		addSkin("Minecraft Map", new Identifier("textures/map/map_background.png"), 64, 64, 3, true);
		addSkin("Minecraft Gui", new Identifier(JustMap.MODID, "textures/skin/mad_def_gui_2.png"), 128, 128, 5, true);
		addSkin("Minecraft Gui Fancy", new Identifier(JustMap.MODID, "textures/skin/mad_def_gui.png"), 128, 128, 7, true);
		addSkin("Metal Frame", new Identifier(JustMap.MODID, "textures/skin/frame_simple_metal.png"), 128, 128, 5, true);
		addSkin("Oak Frame", new Identifier(JustMap.MODID, "textures/skin/map_frame_oak.png"), 64, 64, 10, true, true);
		addSkin("Bamboo Frame", new Identifier(JustMap.MODID, "textures/skin/map_frame_bamboo.png"), 64, 64, 9, true, true);
		addSkin("Stone", new Identifier("textures/block/stone.png"), 16, 16, 4);
		addSkin("Cobblestone", new Identifier("textures/block/cobblestone.png"), 16, 16, 4);
		addSkin("Mossy Cobblestone", new Identifier("textures/block/mossy_cobblestone.png"), 16, 16, 4);
		addSkin("Andesite", new Identifier("textures/block/andesite.png"), 16, 16, 4);
		addSkin("Diorite", new Identifier("textures/block/diorite.png"), 16, 16, 4);
		addSkin("Granite", new Identifier("textures/block/granite.png"), 16, 16, 4);
		addSkin("Bedrock", new Identifier("textures/block/bedrock.png"), 16, 16, 4);
		addSkin("Bricks", new Identifier("textures/block/bricks.png"), 16, 16, 4);
		addSkin("Dark Prizmarine", new Identifier("textures/block/dark_prismarine.png"), 16, 16, 4);
		addSkin("End Stone", new Identifier("textures/block/end_stone.png"), 16, 16, 4);
		addSkin("Glowstone", new Identifier("textures/block/glowstone.png"), 16, 16, 4);
		addSkin("Netherrack", new Identifier("textures/block/netherrack.png"), 16, 16, 4);
		addSkin("Obsidian", new Identifier("textures/block/obsidian.png"), 16, 16, 4);
		addSkin("Purpur", new Identifier("textures/block/purpur_block.png"), 16, 16, 4);
		addSkin("Quartz", new Identifier("textures/block/quartz_block_side.png"), 16, 16, 4);
		addSkin("Sand", new Identifier("textures/block/sand.png"), 16, 16, 4);
		addSkin("Mushroom Inside", new Identifier("textures/block/mushroom_block_inside.png"), 16, 16, 4);
		addSkin("Brown Mushroom", new Identifier("textures/block/brown_mushroom_block.png"), 16, 16, 4);
		addSkin("Acacia Planks", new Identifier("textures/block/acacia_planks.png"), 16, 16, 4);
		addSkin("Birch Planks", new Identifier("textures/block/birch_planks.png"), 16, 16, 4);
		addSkin("Oak Planks", new Identifier("textures/block/oak_planks.png"), 16, 16, 4);
		addSkin("Dark Oak Planks", new Identifier("textures/block/dark_oak_planks.png"), 16, 16, 4);
		addSkin("Jungle Planks", new Identifier("textures/block/jungle_planks.png"), 16, 16, 4);
		addSkin("Spruce Planks", new Identifier("textures/block/spruce_planks.png"), 16, 16, 4);
		addSkin("Nether Bricks", new Identifier("textures/block/nether_bricks.png"), 16, 16, 4);
		addSkin("Lava", new Identifier("textures/block/lava_still.png"), 16, 128, 4);
		addSkin("Water", new Identifier("textures/block/water_still.png"), 16, 128, 4);
		addSkin("Nether Portal", new Identifier("textures/block/nether_portal.png"), 16, 128, 4);
		addSkin("Blue Ice", new Identifier("textures/block/blue_ice.png"), 16, 16, 4);
		addSkin("Bone Block", new Identifier("textures/block/bone_block_side.png"), 16, 16, 4);
		addSkin("Brain Coral", new Identifier("textures/block/brain_coral_block.png"), 16, 16, 4);
		addSkin("Bubble Coral", new Identifier("textures/block/bubble_coral_block.png"), 16, 16, 4);
		addSkin("Fire Coral", new Identifier("textures/block/fire_coral_block.png"), 16, 16, 4);
		addSkin("Horn Coral", new Identifier("textures/block/horn_coral_block.png"), 16, 16, 4);
		addSkin("Tube Coral", new Identifier("textures/block/tube_coral_block.png"), 16, 16, 4);
		addSkin("Blue Wool", new Identifier("textures/block/blue_wool.png"), 16, 16, 4);
		addSkin("Brown Wool", new Identifier("textures/block/brown_wool.png"), 16, 16, 4);
		addSkin("Cyan Wool", new Identifier("textures/block/cyan_wool.png"), 16, 16, 4);
		addSkin("Green Wool", new Identifier("textures/block/green_wool.png"), 16, 16, 4);
		addSkin("Light Blue Wool", new Identifier("textures/block/light_blue_wool.png"), 16, 16, 4);
		addSkin("Lime Wool", new Identifier("textures/block/lime_wool.png"), 16, 16, 4);
		addSkin("Magenta Wool", new Identifier("textures/block/magenta_wool.png"), 16, 16, 4);
		addSkin("Orange Wool", new Identifier("textures/block/orange_wool.png"), 16, 16, 4);
		addSkin("Pink Wool", new Identifier("textures/block/pink_wool.png"), 16, 16, 4);
		addSkin("Purple Wool", new Identifier("textures/block/purple_wool.png"), 16, 16, 4);
		addSkin("Red Wool", new Identifier("textures/block/red_wool.png"), 16, 16, 4);
		addSkin("White Wool", new Identifier("textures/block/white_wool.png"), 16, 16, 4);
		addSkin("Yellow Wool", new Identifier("textures/block/yellow_wool.png"), 16, 16, 4);
		addSkin("Magma", new Identifier("textures/block/magma.png"), 16, 48, 4);
		addSkin("Mycelium", new Identifier("textures/block/mycelium_top.png"), 16, 16, 4);
		addSkin("Podzol", new Identifier("textures/block/podzol_top.png"), 16, 16, 4);
		addSkin("Nether Wart", new Identifier("textures/block/nether_wart_block.png"), 16, 16, 4);
		addSkin("Sponge", new Identifier("textures/block/sponge.png"), 16, 16, 4);
	}
}