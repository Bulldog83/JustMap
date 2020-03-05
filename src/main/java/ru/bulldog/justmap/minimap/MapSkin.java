package ru.bulldog.justmap.minimap;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

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
import net.minecraft.util.Identifier;

@Environment(EnvType.CLIENT)
public class MapSkin extends Sprite {

	private final static SpriteAtlasTexture ATLAS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/map_skins.png"));
	private final static List<MapSkin> SKINS = new ArrayList<>();
	
	public final int id, border;
	public final boolean resizable;
	public final String name;
	
	private final static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	
	private MapSkin(int id, String name, Identifier texture, int w, int h, int border, boolean resize) {
		super(ATLAS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	
		this.id = id;
		this.border = border;
		this.resizable = resize;
		this.name = name;
	}
	
	public static void addSkin(String name, Identifier texture, int w, int h, int border, boolean resizable) {
		int id = SKINS.size();
		SKINS.add(id, new MapSkin(id, name, texture, w, h, border, resizable));
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
	
	public void draw(int x, int y, int w, int h) {
		if (this.resizable) {
			textureManager.bindTexture(this.getTexture());
		} else {
			bindPavedTexture(w, h);
		}
		RenderSystem.enableAlphaTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		DrawHelper.blit(x, y, 0, w, h, this);
	}
	
	public void draw(int x, int y, int size) {
		this.draw(x, y, size, size);
	}
	
	public String getName() {
		return this.name;
	}
	
	@Override
	public String toString() {
		return this.getName();
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
	
	static {
		addSkin("Minecraft Map", new Identifier("textures/map/map_background.png"), 64, 64, 4, true);
		addSkin("Minecraft Gui", new Identifier(JustMap.MODID, "textures/skin/mad_def_gui_2.png"), 128, 128, 5, true);
		addSkin("Minecraft Gui Fancy", new Identifier(JustMap.MODID, "textures/skin/mad_def_gui.png"), 128, 128, 7, true);
		addSkin("Metal Frame", new Identifier(JustMap.MODID, "textures/skin/frame_simple_metal.png"), 128, 128, 5, true);
		addSkin("Oak Frame", new Identifier(JustMap.MODID, "textures/skin/map_frame_oak.png"), 64, 64, 10, true);
		addSkin("Bamboo Frame", new Identifier(JustMap.MODID, "textures/skin/map_frame_bamboo.png"), 64, 64, 10, true);
		addSkin("Stone", new Identifier("textures/block/stone.png"), 16, 16, 4, false);
		addSkin("Cobblestone", new Identifier("textures/block/cobblestone.png"), 16, 16, 4, false);
		addSkin("Mossy Cobblestone", new Identifier("textures/block/mossy_cobblestone.png"), 16, 16, 4, false);
		addSkin("Andesite", new Identifier("textures/block/andesite.png"), 16, 16, 4, false);
		addSkin("Diorite", new Identifier("textures/block/diorite.png"), 16, 16, 4, false);
		addSkin("Granite", new Identifier("textures/block/granite.png"), 16, 16, 4, false);
		addSkin("Bedrock", new Identifier("textures/block/bedrock.png"), 16, 16, 4, false);
		addSkin("Bricks", new Identifier("textures/block/bricks.png"), 16, 16, 4, false);
		addSkin("Dark Prizmarine", new Identifier("textures/block/dark_prismarine.png"), 16, 16, 4, false);
		addSkin("End Stone", new Identifier("textures/block/end_stone.png"), 16, 16, 4, false);
		addSkin("Glowstone", new Identifier("textures/block/glowstone.png"), 16, 16, 4, false);
		addSkin("Netherrack", new Identifier("textures/block/netherrack.png"), 16, 16, 4, false);
		addSkin("Obsidian", new Identifier("textures/block/obsidian.png"), 16, 16, 4, false);
		addSkin("Purpur", new Identifier("textures/block/purpur_block.png"), 16, 16, 4, false);
		addSkin("Quartz", new Identifier("textures/block/quartz_block_side.png"), 16, 16, 4, false);
		addSkin("Sand", new Identifier("textures/block/sand.png"), 16, 16, 4, false);
		addSkin("Mushroom Inside", new Identifier("textures/block/mushroom_block_inside.png"), 16, 16, 4, false);
		addSkin("Brown Mushroom", new Identifier("textures/block/brown_mushroom_block.png"), 16, 16, 4, false);
		addSkin("Acacia Planks", new Identifier("textures/block/acacia_planks.png"), 16, 16, 4, false);
		addSkin("Birch Planks", new Identifier("textures/block/birch_planks.png"), 16, 16, 4, false);
		addSkin("Oak Planks", new Identifier("textures/block/oak_planks.png"), 16, 16, 4, false);
		addSkin("Dark Oak Planks", new Identifier("textures/block/dark_oak_planks.png"), 16, 16, 4, false);
		addSkin("Jungle Planks", new Identifier("textures/block/jungle_planks.png"), 16, 16, 4, false);
		addSkin("Spruce Planks", new Identifier("textures/block/spruce_planks.png"), 16, 16, 4, false);
		addSkin("Nether Bricks", new Identifier("textures/block/nether_bricks.png"), 16, 16, 4, false);
		addSkin("Lava", new Identifier("textures/block/lava_still.png"), 16, 128, 4, false);
		addSkin("Water", new Identifier("textures/block/water_still.png"), 16, 128, 4, false);
		addSkin("Nether Portal", new Identifier("textures/block/nether_portal.png"), 16, 128, 4, false);
		addSkin("Blue Ice", new Identifier("textures/block/blue_ice.png"), 16, 16, 4, false);
		addSkin("Bone Block", new Identifier("textures/block/bone_block_side.png"), 16, 16, 4, false);
		addSkin("Brain Coral", new Identifier("textures/block/brain_coral_block.png"), 16, 16, 4, false);
		addSkin("Bubble Coral", new Identifier("textures/block/bubble_coral_block.png"), 16, 16, 4, false);
		addSkin("Fire Coral", new Identifier("textures/block/fire_coral_block.png"), 16, 16, 4, false);
		addSkin("Horn Coral", new Identifier("textures/block/horn_coral_block.png"), 16, 16, 4, false);
		addSkin("Tube Coral", new Identifier("textures/block/tube_coral_block.png"), 16, 16, 4, false);
		addSkin("Blue Wool", new Identifier("textures/block/blue_wool.png"), 16, 16, 4, false);
		addSkin("Brown Wool", new Identifier("textures/block/brown_wool.png"), 16, 16, 4, false);
		addSkin("Cyan Wool", new Identifier("textures/block/cyan_wool.png"), 16, 16, 4, false);
		addSkin("Green Wool", new Identifier("textures/block/green_wool.png"), 16, 16, 4, false);
		addSkin("Light Blue Wool", new Identifier("textures/block/light_blue_wool.png"), 16, 16, 4, false);
		addSkin("Lime Wool", new Identifier("textures/block/lime_wool.png"), 16, 16, 4, false);
		addSkin("Magenta Wool", new Identifier("textures/block/magenta_wool.png"), 16, 16, 4, false);
		addSkin("Orange Wool", new Identifier("textures/block/orange_wool.png"), 16, 16, 4, false);
		addSkin("Pink Wool", new Identifier("textures/block/pink_wool.png"), 16, 16, 4, false);
		addSkin("Purple Wool", new Identifier("textures/block/purple_wool.png"), 16, 16, 4, false);
		addSkin("Red Wool", new Identifier("textures/block/red_wool.png"), 16, 16, 4, false);
		addSkin("White Wool", new Identifier("textures/block/white_wool.png"), 16, 16, 4, false);
		addSkin("Yellow Wool", new Identifier("textures/block/yellow_wool.png"), 16, 16, 4, false);
		addSkin("Magma", new Identifier("textures/block/magma.png"), 16, 48, 4, false);
		addSkin("Mycelium", new Identifier("textures/block/mycelium_top.png"), 16, 16, 4, false);
		addSkin("Podzol", new Identifier("textures/block/podzol_top.png"), 16, 16, 4, false);
		addSkin("Nether Wart", new Identifier("textures/block/nether_wart_block.png"), 16, 16, 4, false);
		addSkin("Sponge", new Identifier("textures/block/sponge.png"), 16, 16, 4, false);
	}
}