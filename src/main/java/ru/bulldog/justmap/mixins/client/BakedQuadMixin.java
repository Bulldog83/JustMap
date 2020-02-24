package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import ru.bulldog.justmap.util.BakedData;

import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.Sprite;

@Mixin(BakedQuad.class)
public abstract class BakedQuadMixin implements BakedData {	
	@Shadow
	protected Sprite sprite;

	public Sprite getSprite() {
		return this.sprite;
	}	
}