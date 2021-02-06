package ru.bulldog.justmap.util;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.ResourceLocation;
import ru.bulldog.justmap.JustMap;

public class SpriteAtlas {
	public final static SpriteAtlasTexture MAP_ICONS = new SpriteAtlasTexture(new ResourceLocation(JustMap.MOD_ID, "textures/atlas/map_icons.png"));
	public final static SpriteAtlasTexture ENTITY_HEAD_ICONS = new SpriteAtlasTexture(new ResourceLocation(JustMap.MOD_ID, "textures/atlas/entity_head_icons.png"));
	public final static SpriteAtlasTexture WAYPOINT_ICONS = new SpriteAtlasTexture(new ResourceLocation(JustMap.MOD_ID, "textures/atlas/waypoint_icons.png"));
}
