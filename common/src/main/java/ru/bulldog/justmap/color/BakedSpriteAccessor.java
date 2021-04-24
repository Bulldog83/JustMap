package ru.bulldog.justmap.color;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import java.lang.reflect.Field;

public class BakedSpriteAccessor {

	public static TextureAtlasSprite getSprite(BakedQuad source) {
		try {
			Field sprite = BakedQuad.class.getDeclaredField("sprite");
			sprite.setAccessible(true);
			return (TextureAtlasSprite) sprite.get(source);
		} catch (Exception ex) {
			return null;
		}
	}
}
