package ru.bulldog.justmap.util;

import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.util.Identifier;

import ru.bulldog.justmap.JustMap;

public class SpriteAtlas {
	public final static SpriteAtlasTexture MAP_ICONS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/map_icons.png"));
	public final static SpriteAtlasTexture ENTITY_HEAD_ICONS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/entity_head_icons.png"));
	public final static SpriteAtlasTexture WAYPOINT_ICONS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/waypoint_icons.png"));
}
