package ru.bulldog.justmap.client.map.skin;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.resources.ResourceLocation;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.Settings;
import ru.bulldog.justmap.config.PathConfig;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.render.Image;
import ru.bulldog.justmap.util.render.RenderUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

@Environment(EnvType.CLIENT)
public class MapSkin extends Image {	
	public enum SkinType {
		UNIVERSAL,
		SQUARE,
		ROUND
	}

	private final static List<MapSkin> SKINS = new ArrayList<>();
	private final static TextureManager textureManager = Minecraft.getInstance().getTextureManager();
	private final static PathConfig CONFIG = Settings.MAP_APPEARANCE;
	
	private final RenderData renderData;
	
	public final SkinType type;
	public final int id, border;
	public final boolean resizable;
	public final boolean repeating;
	public final String name;
	
	private MapSkin(int id, String name, SkinType type, ResourceLocation texture, int w, int h, int border, boolean resize, boolean repeat) {
		this(id, name, type, texture, ImageUtil.loadImage(texture, w, h), w, h, border, resize, repeat);
	}
	
	private MapSkin(int id, String name, SkinType type, ResourceLocation texture, NativeImage image, int w, int h, int border, boolean resize, boolean repeat) {
		super(texture, image);
		
		this.id = id;
		this.type = type;
		this.border = border;
		this.resizable = resize;
		this.repeating = repeat;
		this.name = name;
		this.width = w;
		this.height = h;
		
		this.renderData = new RenderData();
	}
	
	public static void addSkin(SkinType type, String name, ResourceLocation texture, int w, int h, int border, boolean resizable, boolean repeat) {
		int id = SKINS.size();
		SKINS.add(id, new MapSkin(id, name, type, texture, w, h, border, resizable, repeat));
	}
	
	public static void addSkin(SkinType type, String name, ResourceLocation texture, NativeImage image, int w, int h, int border, boolean resizable, boolean repeat) {
		int id = SKINS.size();
		SKINS.add(id, new MapSkin(id, name, type, texture, image, w, h, border, resizable, repeat));
	}
	
	public static void addSquareSkin(String name, ResourceLocation texture, int w, int h, int border, boolean resizable, boolean repeat) {
		addSkin(SkinType.SQUARE, name, texture, w, h, border, resizable, repeat);
	}
	
	public static void addSquareSkin(String name, ResourceLocation texture, NativeImage image, int w, int h, int border, boolean resizable, boolean repeat) {
		addSkin(SkinType.SQUARE, name, texture, image, w, h, border, resizable, repeat);
	}
	
	public static void addSquareSkin(String name, ResourceLocation texture, int w, int h, int border, boolean resizable) {
		addSkin(SkinType.SQUARE, name, texture, w, h, border, resizable, false);
	}
	
	public static void addRoundSkin(String name, ResourceLocation texture, int w, int h, int border) {
		addSkin(SkinType.ROUND, name, texture, w, h, border, true, false);
	}
	
	public static void addRoundSkin(String name, ResourceLocation texture, NativeImage image, int w, int h, int border) {
		addSkin(SkinType.ROUND, name, texture, image, w, h, border, true, false);
	}
	
	public static void addUniversalSkin(String name, ResourceLocation texture, int w, int h, int border) {
		addSkin(SkinType.UNIVERSAL, name, texture, w, h, border, false, false);
	}
	
	public static void addUniversalSkin(String name, ResourceLocation texture, NativeImage image, int w, int h, int border) {
		addSkin(SkinType.UNIVERSAL, name, texture, image, w, h, border, false, false);
	}
	
	public static MapSkin getSkin(int id) {
		try {
			return SKINS.get(id);
		} catch(IndexOutOfBoundsException ex) {
			JustMap.LOGGER.warning(ex.getMessage());
		}
		
		return SKINS.get(0);
	}
	
	private void registerPavedTexture(int w, int h) {
		int border = (int) (this.border * renderData.scaleFactor);
		String pattern = "skin_%d_%dx%d_%d";
		boolean isRound = Settings.isMapRound();
		if (isRound) pattern += "_round";
		this.textureId = new ResourceLocation(JustMap.MOD_ID, String.format(pattern, this.id, w, h, border));
		if (textureManager.getTexture(textureId) == null) {
			NativeImage pavedImage;
			if (isRound) {
				pavedImage = ImageUtil.createRoundSkin(image, w, h, border);
			} else {
				pavedImage = ImageUtil.createSquareSkin(image, w, h, border);
			}
			textureManager.register(textureId, new DynamicTexture(pavedImage));
		}
	}

	@Override
	public void draw(PoseStack matrixStack, double x, double y, int w, int h) {
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		float hMult = (float) this.getWidth() / this.getHeight();
		if (resizable || repeating) {
			if (type != SkinType.ROUND && (w > this.getWidth() || h > this.getHeight())) {
				RenderUtil.drawSkin(matrixStack, this, x, y, w, h);
			} else {
				RenderUtil.drawImage(matrixStack, this, x, y, w, h / hMult);
			}
		} else {
			this.registerPavedTexture(w, h);
			RenderUtil.drawImage(matrixStack, this, x, y, w, h);
		}
	}
	
	public boolean isRound() {
		return this.type == SkinType.ROUND ||
			   this.type == SkinType.UNIVERSAL;
	}
	
	public boolean isSquare() {
		return this.type != SkinType.ROUND;
	}
	
	public Component getName() {
		return new TextComponent(this.name);
	}
	
	@Override
	public String toString() {
		return this.getName().getString();
	}
	
	public static MapSkin getDefaultSkin() {
		return getSkins().get(0);
	}
	
	public static MapSkin getDefaultSquareSkin() {
		return getSquareSkins().get(0);
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
		if (Settings.isMapRound()) {
			return MapSkin.getRoundSkins();
		}
		return MapSkin.getSquareSkins();
	}
	
	public static List<MapSkin> getSquareSkins() {
		return Collections.unmodifiableList(SKINS.stream()
				.filter(MapSkin::isSquare).collect(Collectors.toList()));
	}
	
	public static List<MapSkin> getRoundSkins() {
		return Collections.unmodifiableList(SKINS.stream()
				.filter(MapSkin::isRound).collect(Collectors.toList()));
	}
	
	public static MapSkin getCurrentSkin() {
		return getSkin(CONFIG.getIntRoot("minimap_skin"));
	}
	
	public static MapSkin getBigMapSkin() {
		return getSkin(CONFIG.getIntRoot("big_map_skin"));
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
		public int hSegments, vSegments;
		public float hTail, vTail;
		public float hTailU, vTailV;
		
		public boolean scaleChanged = false;
		
		private RenderData() {
			this.updateScale();
		}
		
		public void updateScale(double scale) {
			if (this.scaleFactor != scale) {
				this.scaleFactor = scale;
				this.scaleChanged = true;
			}
		}
		
		public void updateScale() {
			this.updateScale(CONFIG.getFloatRoot("skin_scale"));
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
			this.hSegments = (int) Math.round(w / (spriteW * scaleFactor));
			this.vSegments = (int) Math.round(h / (spriteH * scaleFactor));
			this.scaledBorder = (float) (border * scaleFactor);
			if (MapSkin.this.resizable) {
				this.hSide = w - scaledBorder * 2;
				this.vSide = h - scaledBorder * 2;
			} else {
				this.hSide = (float) ((spriteW - border * 2) * scaleFactor);
				this.vSide = (float) ((spriteH - border * 2) * scaleFactor);
			}
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
			this.hTail = w - hSegments * hSide - scaledBorder * 2;
			if (hTail < 0) {
				this.hSegments--;
				this.hTail = w - hSegments * hSide - scaledBorder * 2;
			}
			this.vTail = h - vSegments * vSide - scaledBorder * 2;
			if (vTail < 0) {
				this.vSegments--;
				this.vTail = h - vSegments * vSide - scaledBorder * 2;
			}
			this.hTailU = (float) ((border + hTail / scaleFactor) / spriteW);
			this.vTailV = (float) ((border + vTail / scaleFactor) / spriteH);
			
			this.scaleChanged = false;
		}
	}
	
	static {
		addSquareSkin("Minecraft Map", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_def_map.png"), 64, 64, 5, false, true);
		addSquareSkin("Minecraft LaF", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_gui_laf.png"), 64, 64, 3, false, true);
		addSquareSkin("Minecraft Gui", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_def_gui.png"), 64, 64, 5, true);
		addSquareSkin("Minecraft Gui Fancy", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_def_gui_fancy.png"), 64, 64, 7, true);
		addSquareSkin("Metal Frame", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_simple_metal.png"), 64, 64, 4, true);
		addSquareSkin("Oak Frame", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_oak.png"), 64, 64, 10, false, true);
		addSquareSkin("Bamboo Frame", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_bamboo.png"), 64, 64, 9, false, true);
		addRoundSkin("Minecraft Gui", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_def_gui_round.png"), 256, 256, 10);
		addRoundSkin("Frame Round", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_frame_round.png"), 256, 256, 12);
		addRoundSkin("Frame Runed", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_runed_round.png"), 256, 256, 19);
		addRoundSkin("Frame Frozen", new ResourceLocation(JustMap.MOD_ID, "textures/skin/skin_frozen_round.png"), 256, 273, 17);
		addUniversalSkin("Stone", new ResourceLocation("textures/block/stone.png"), 256, 256, 8);
		addUniversalSkin("Cobblestone", new ResourceLocation("textures/block/cobblestone.png"), 256, 256, 8);
		addUniversalSkin("Mossy Cobblestone", new ResourceLocation("textures/block/mossy_cobblestone.png"), 256, 256, 8);
		addUniversalSkin("Andesite", new ResourceLocation("textures/block/andesite.png"), 256, 256, 8);
		addUniversalSkin("Diorite", new ResourceLocation("textures/block/diorite.png"), 256, 256, 8);
		addUniversalSkin("Granite", new ResourceLocation("textures/block/granite.png"), 256, 256, 8);
		addUniversalSkin("Bedrock", new ResourceLocation("textures/block/bedrock.png"), 256, 256, 8);
		addUniversalSkin("Bricks", new ResourceLocation("textures/block/bricks.png"), 256, 256, 8);
		addUniversalSkin("Dark Prizmarine", new ResourceLocation("textures/block/dark_prismarine.png"), 256, 256, 8);
		addUniversalSkin("End Stone", new ResourceLocation("textures/block/end_stone.png"), 256, 256, 8);
		addUniversalSkin("Glowstone", new ResourceLocation("textures/block/glowstone.png"), 256, 256, 8);
		addUniversalSkin("Netherrack", new ResourceLocation("textures/block/netherrack.png"), 256, 256, 8);
		addUniversalSkin("Obsidian", new ResourceLocation("textures/block/obsidian.png"), 256, 256, 8);
		addUniversalSkin("Purpur", new ResourceLocation("textures/block/purpur_block.png"), 256, 256, 8);
		addUniversalSkin("Quartz", new ResourceLocation("textures/block/quartz_block_side.png"), 256, 256, 8);
		addUniversalSkin("Sand", new ResourceLocation("textures/block/sand.png"), 256, 256, 8);
		addUniversalSkin("Mushroom Inside", new ResourceLocation("textures/block/mushroom_block_inside.png"), 256, 256, 8);
		addUniversalSkin("Brown Mushroom", new ResourceLocation("textures/block/brown_mushroom_block.png"), 256, 256, 8);
		addUniversalSkin("Acacia Planks", new ResourceLocation("textures/block/acacia_planks.png"), 256, 256, 8);
		addUniversalSkin("Birch Planks", new ResourceLocation("textures/block/birch_planks.png"), 256, 256, 8);
		addUniversalSkin("Oak Planks", new ResourceLocation("textures/block/oak_planks.png"), 256, 256, 8);
		addUniversalSkin("Dark Oak Planks", new ResourceLocation("textures/block/dark_oak_planks.png"), 256, 256, 8);
		addUniversalSkin("Jungle Planks", new ResourceLocation("textures/block/jungle_planks.png"), 256, 256, 8);
		addUniversalSkin("Spruce Planks", new ResourceLocation("textures/block/spruce_planks.png"), 256, 256, 8);
		addUniversalSkin("Nether Bricks", new ResourceLocation("textures/block/nether_bricks.png"), 256, 256, 8);
		addUniversalSkin("Lava", new ResourceLocation("textures/block/lava_still.png"), 256, 256, 8);
		addUniversalSkin("Water", new ResourceLocation("textures/block/water_still.png"), 256, 256, 8);
		addUniversalSkin("Nether Portal", new ResourceLocation("textures/block/nether_portal.png"), 256, 256, 8);
		addUniversalSkin("Blue Ice", new ResourceLocation("textures/block/blue_ice.png"), 256, 256, 8);
		addUniversalSkin("Bone Block", new ResourceLocation("textures/block/bone_block_side.png"), 256, 256, 8);
		addUniversalSkin("Brain Coral", new ResourceLocation("textures/block/brain_coral_block.png"), 256, 256, 8);
		addUniversalSkin("Bubble Coral", new ResourceLocation("textures/block/bubble_coral_block.png"), 256, 256, 8);
		addUniversalSkin("Fire Coral", new ResourceLocation("textures/block/fire_coral_block.png"), 256, 256, 8);
		addUniversalSkin("Horn Coral", new ResourceLocation("textures/block/horn_coral_block.png"), 256, 256, 8);
		addUniversalSkin("Tube Coral", new ResourceLocation("textures/block/tube_coral_block.png"), 256, 256, 8);
		addUniversalSkin("Blue Wool", new ResourceLocation("textures/block/blue_wool.png"), 256, 256, 8);
		addUniversalSkin("Brown Wool", new ResourceLocation("textures/block/brown_wool.png"), 256, 256, 8);
		addUniversalSkin("Cyan Wool", new ResourceLocation("textures/block/cyan_wool.png"), 256, 256, 8);
		addUniversalSkin("Green Wool", new ResourceLocation("textures/block/green_wool.png"), 256, 256, 8);
		addUniversalSkin("Light Blue Wool", new ResourceLocation("textures/block/light_blue_wool.png"), 256, 256, 8);
		addUniversalSkin("Lime Wool", new ResourceLocation("textures/block/lime_wool.png"), 256, 256, 8);
		addUniversalSkin("Magenta Wool", new ResourceLocation("textures/block/magenta_wool.png"), 256, 256, 8);
		addUniversalSkin("Orange Wool", new ResourceLocation("textures/block/orange_wool.png"), 256, 256, 8);
		addUniversalSkin("Pink Wool", new ResourceLocation("textures/block/pink_wool.png"), 256, 256, 8);
		addUniversalSkin("Purple Wool", new ResourceLocation("textures/block/purple_wool.png"), 256, 256, 8);
		addUniversalSkin("Red Wool", new ResourceLocation("textures/block/red_wool.png"), 256, 256, 8);
		addUniversalSkin("White Wool", new ResourceLocation("textures/block/white_wool.png"), 256, 256, 8);
		addUniversalSkin("Yellow Wool", new ResourceLocation("textures/block/yellow_wool.png"), 256, 256, 8);
		addUniversalSkin("Magma", new ResourceLocation("textures/block/magma.png"), 256, 256, 8);
		addUniversalSkin("Mycelium", new ResourceLocation("textures/block/mycelium_top.png"), 256, 256, 8);
		addUniversalSkin("Podzol", new ResourceLocation("textures/block/podzol_top.png"), 256, 256, 8);
		addUniversalSkin("Nether Wart", new ResourceLocation("textures/block/nether_wart_block.png"), 256, 256, 8);
		addUniversalSkin("Sponge", new ResourceLocation("textures/block/sponge.png"), 256, 256, 8);
		
		SkinLoader.loadSkins();
	}
}