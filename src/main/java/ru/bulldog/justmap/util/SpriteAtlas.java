package ru.bulldog.justmap.util;

import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import ru.bulldog.justmap.JustMap;

public class SpriteAtlas {
	public final static TextureAtlas MAP_ICONS = new TextureAtlas(new ResourceLocation(JustMap.MODID, "textures/atlas/map_icons.png"));
	public final static TextureAtlas ENTITY_HEAD_ICONS = new TextureAtlas(new ResourceLocation(JustMap.MODID, "textures/atlas/entity_head_icons.png"));
	public final static TextureAtlas WAYPOINT_ICONS = new TextureAtlas(new ResourceLocation(JustMap.MODID, "textures/atlas/waypoint_icons.png"));
}
