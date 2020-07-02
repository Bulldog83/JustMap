package ru.bulldog.justmap.map.minimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.RenderUtil;

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
	public static enum SkinType {
		UNIVERSAL,
		SQUARE,
		ROUND
	}

	private final static SpriteAtlasTexture ATLAS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/map_skins"));
	private final static List<MapSkin> SKINS = new ArrayList<>();
	private final static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	
	private final RenderData renderData;
	
	public final SkinType type;
	public final int id, border;
	public final boolean resizable;
	public final boolean repeating;
	public final String name;
	
	private MapSkin(int id, String name, SkinType type, Identifier texture, int w, int h, int border, boolean resize, boolean repeat) {
		super(ATLAS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	
		this.id = id;
		this.type = type;
		this.border = border;
		this.resizable = resize;
		this.repeating = repeat;
		this.name = name;
		
		this.renderData = new RenderData();
	}
	
	public static void addSkin(SkinType type, String name, Identifier texture, int w, int h, int border, boolean resizable, boolean repeat) {
		int id = SKINS.size();
		SKINS.add(id, new MapSkin(id, name, type, texture, w, h, border, resizable, repeat));
	}
	
	public static void addSquareSkin(String name, Identifier texture, int w, int h, int border, boolean resizable, boolean repeat) {
		addSkin(SkinType.SQUARE, name, texture, w, h, border, resizable, repeat);
	}
	
	public static void addSquareSkin(String name, Identifier texture, int w, int h, int border, boolean resizable) {
		addSkin(SkinType.SQUARE, name, texture, w, h, border, resizable, false);
	}
	
	public static void addUniversalSkin(String name, Identifier texture, int w, int h, int border) {
		addSkin(SkinType.UNIVERSAL, name, texture, w, h, border, false, false);
	}
	
	public static void addRoundSkin(String name, Identifier texture, int w, int h, int border) {
		addSkin(SkinType.ROUND, name, texture, w, h, border, true, false);
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
		int border = (int) (this.border * ClientParams.skinScale);
		Identifier id = new Identifier(JustMap.MODID, String.format("skin_%d_%dx%d_%d", this.id, w, h, border));
		if (textureManager.getTexture(id) == null) {
			NativeImage pavedImage = new NativeImage(w, h, false);
			int imgW = this.images[0].getWidth();
			int imgH = this.images[0].getHeight();
			int imgX = 0, imgY = 0;
			int y = 0;
			while(y < h) {
				int x = 0;
				while(x < w) {
					if (imgX >= imgW) imgX = 0;
					if (x < w - border && y < h - border &&
						x > border && y > border) {							
							x++;
							imgX++;					
							if (imgX >= imgW) imgX = 0;
							continue;
					}
					int pixel = this.images[0].getPixelColor(imgX, imgY);
					pavedImage.setPixelColor(x, y, pixel);
					imgX++;
					if (imgX >= imgW) imgX = 0;
					x++;
				}
				imgY++;
				if (imgY >= imgH) imgY = 0;
				y++;
			}
			textureManager.registerTexture(id, new NativeImageBackedTexture(pavedImage));
		}
		textureManager.bindTexture(id);
	}
	
	public void draw(MatrixStack matrixStack, int x, int y, int w, int h) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		float hMult = (float) this.getWidth() / this.getHeight();
		if (resizable) {
			textureManager.bindTexture(this.getTexture());
			if (type != SkinType.ROUND && (w > this.getWidth() || h > this.getHeight())) {
				RenderUtil.drawSkin(matrixStack, this, x, y, w, h);
			} else {
				RenderUtil.drawSprite(matrixStack, x, y, 0, w, (int) (h / hMult), this);
			}
		} else {
			this.bindPavedTexture(w, h);
			RenderUtil.drawSprite(matrixStack, x, y, 0, w, h, this);
		}
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
		public double scaleFactor = 1.0;
		public double leftC, rightC;
		public double topC, bottomC;
		public float width, height;
		public float scaledBorder;
		public float hSide, vSide;		
		public float leftU, rightU;
		public float topV, bottomV;
		public float tail, tailU;
		
		public boolean scaleChanged = false;
		
		private RenderData() {
			this.updateScale();
		}
		
		public void updateScale() {
			double scale = ClientParams.skinScale;
			if (this.scaleFactor != scale) {
				this.scaleFactor = scale;
				this.scaleChanged = true;
			}
		}
		
		public void calculate(double x, double y, float w, float h) {
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
			
			this.scaleChanged = false;
		}
	}
	
	static {
		addSquareSkin("Minecraft Map", new Identifier(JustMap.MODID, "textures/skin/map_background.png"), 64, 64, 5, true);
		addSquareSkin("Minecraft Gui", new Identifier(JustMap.MODID, "textures/skin/mad_def_gui_2.png"), 64, 64, 5, true);
		addSquareSkin("Minecraft Gui Fancy", new Identifier(JustMap.MODID, "textures/skin/mad_def_gui.png"), 64, 64, 7, true);
		addSquareSkin("Metal Frame", new Identifier(JustMap.MODID, "textures/skin/frame_simple_metal.png"), 64, 64, 4, true);
		addSquareSkin("Oak Frame", new Identifier(JustMap.MODID, "textures/skin/map_frame_oak.png"), 64, 64, 10, true, true);
		addSquareSkin("Bamboo Frame", new Identifier(JustMap.MODID, "textures/skin/map_frame_bamboo.png"), 64, 64, 9, true, true);
		addRoundSkin("Minecraft Round", new Identifier(JustMap.MODID, "textures/skin/mad_def_gui_2_round.png"), 256, 256, 16);
		addRoundSkin("Minecraft Fancy Round", new Identifier(JustMap.MODID, "textures/skin/mad_def_gui_round.png"), 68, 68, 7);
		addRoundSkin("Frame Round", new Identifier(JustMap.MODID, "textures/skin/frame_round.png"), 256, 256, 12);
		addRoundSkin("Frame Runed Round", new Identifier(JustMap.MODID, "textures/skin/frame_runed_round.png"), 256, 256, 19);
		addRoundSkin("Frame Frozen Round", new Identifier(JustMap.MODID, "textures/skin/frame_frozen_round.png"), 256, 273, 17);
		addUniversalSkin("Stone", new Identifier("textures/block/stone.png"), 16, 16, 4);
		addUniversalSkin("Cobblestone", new Identifier("textures/block/cobblestone.png"), 16, 16, 4);
		addUniversalSkin("Mossy Cobblestone", new Identifier("textures/block/mossy_cobblestone.png"), 16, 16, 4);
		addUniversalSkin("Andesite", new Identifier("textures/block/andesite.png"), 16, 16, 4);
		addUniversalSkin("Diorite", new Identifier("textures/block/diorite.png"), 16, 16, 4);
		addUniversalSkin("Granite", new Identifier("textures/block/granite.png"), 16, 16, 4);
		addUniversalSkin("Bedrock", new Identifier("textures/block/bedrock.png"), 16, 16, 4);
		addUniversalSkin("Bricks", new Identifier("textures/block/bricks.png"), 16, 16, 4);
		addUniversalSkin("Dark Prizmarine", new Identifier("textures/block/dark_prismarine.png"), 16, 16, 4);
		addUniversalSkin("End Stone", new Identifier("textures/block/end_stone.png"), 16, 16, 4);
		addUniversalSkin("Glowstone", new Identifier("textures/block/glowstone.png"), 16, 16, 4);
		addUniversalSkin("Netherrack", new Identifier("textures/block/netherrack.png"), 16, 16, 4);
		addUniversalSkin("Obsidian", new Identifier("textures/block/obsidian.png"), 16, 16, 4);
		addUniversalSkin("Purpur", new Identifier("textures/block/purpur_block.png"), 16, 16, 4);
		addUniversalSkin("Quartz", new Identifier("textures/block/quartz_block_side.png"), 16, 16, 4);
		addUniversalSkin("Sand", new Identifier("textures/block/sand.png"), 16, 16, 4);
		addUniversalSkin("Mushroom Inside", new Identifier("textures/block/mushroom_block_inside.png"), 16, 16, 4);
		addUniversalSkin("Brown Mushroom", new Identifier("textures/block/brown_mushroom_block.png"), 16, 16, 4);
		addUniversalSkin("Acacia Planks", new Identifier("textures/block/acacia_planks.png"), 16, 16, 4);
		addUniversalSkin("Birch Planks", new Identifier("textures/block/birch_planks.png"), 16, 16, 4);
		addUniversalSkin("Oak Planks", new Identifier("textures/block/oak_planks.png"), 16, 16, 4);
		addUniversalSkin("Dark Oak Planks", new Identifier("textures/block/dark_oak_planks.png"), 16, 16, 4);
		addUniversalSkin("Jungle Planks", new Identifier("textures/block/jungle_planks.png"), 16, 16, 4);
		addUniversalSkin("Spruce Planks", new Identifier("textures/block/spruce_planks.png"), 16, 16, 4);
		addUniversalSkin("Nether Bricks", new Identifier("textures/block/nether_bricks.png"), 16, 16, 4);
		addUniversalSkin("Lava", new Identifier("textures/block/lava_still.png"), 16, 128, 4);
		addUniversalSkin("Water", new Identifier("textures/block/water_still.png"), 16, 128, 4);
		addUniversalSkin("Nether Portal", new Identifier("textures/block/nether_portal.png"), 16, 128, 4);
		addUniversalSkin("Blue Ice", new Identifier("textures/block/blue_ice.png"), 16, 16, 4);
		addUniversalSkin("Bone Block", new Identifier("textures/block/bone_block_side.png"), 16, 16, 4);
		addUniversalSkin("Brain Coral", new Identifier("textures/block/brain_coral_block.png"), 16, 16, 4);
		addUniversalSkin("Bubble Coral", new Identifier("textures/block/bubble_coral_block.png"), 16, 16, 4);
		addUniversalSkin("Fire Coral", new Identifier("textures/block/fire_coral_block.png"), 16, 16, 4);
		addUniversalSkin("Horn Coral", new Identifier("textures/block/horn_coral_block.png"), 16, 16, 4);
		addUniversalSkin("Tube Coral", new Identifier("textures/block/tube_coral_block.png"), 16, 16, 4);
		addUniversalSkin("Blue Wool", new Identifier("textures/block/blue_wool.png"), 16, 16, 4);
		addUniversalSkin("Brown Wool", new Identifier("textures/block/brown_wool.png"), 16, 16, 4);
		addUniversalSkin("Cyan Wool", new Identifier("textures/block/cyan_wool.png"), 16, 16, 4);
		addUniversalSkin("Green Wool", new Identifier("textures/block/green_wool.png"), 16, 16, 4);
		addUniversalSkin("Light Blue Wool", new Identifier("textures/block/light_blue_wool.png"), 16, 16, 4);
		addUniversalSkin("Lime Wool", new Identifier("textures/block/lime_wool.png"), 16, 16, 4);
		addUniversalSkin("Magenta Wool", new Identifier("textures/block/magenta_wool.png"), 16, 16, 4);
		addUniversalSkin("Orange Wool", new Identifier("textures/block/orange_wool.png"), 16, 16, 4);
		addUniversalSkin("Pink Wool", new Identifier("textures/block/pink_wool.png"), 16, 16, 4);
		addUniversalSkin("Purple Wool", new Identifier("textures/block/purple_wool.png"), 16, 16, 4);
		addUniversalSkin("Red Wool", new Identifier("textures/block/red_wool.png"), 16, 16, 4);
		addUniversalSkin("White Wool", new Identifier("textures/block/white_wool.png"), 16, 16, 4);
		addUniversalSkin("Yellow Wool", new Identifier("textures/block/yellow_wool.png"), 16, 16, 4);
		addUniversalSkin("Magma", new Identifier("textures/block/magma.png"), 16, 48, 4);
		addUniversalSkin("Mycelium", new Identifier("textures/block/mycelium_top.png"), 16, 16, 4);
		addUniversalSkin("Podzol", new Identifier("textures/block/podzol_top.png"), 16, 16, 4);
		addUniversalSkin("Nether Wart", new Identifier("textures/block/nether_wart_block.png"), 16, 16, 4);
		addUniversalSkin("Sponge", new Identifier("textures/block/sponge.png"), 16, 16, 4);
	}
}