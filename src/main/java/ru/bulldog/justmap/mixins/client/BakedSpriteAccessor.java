package ru.bulldog.justmap.mixins.client;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BakedQuad.class)
public interface BakedSpriteAccessor {	
	@Accessor(value = "sprite")
	TextureAtlasSprite getSprite();
}