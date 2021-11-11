package ru.bulldog.justmap.mixins.client;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedQuad.class)
public interface BakedSpriteAccessor {
	@Accessor(value = "sprite")
	Sprite getSprite();
}
