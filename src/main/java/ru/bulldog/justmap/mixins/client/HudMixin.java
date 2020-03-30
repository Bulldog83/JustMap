package ru.bulldog.justmap.mixins.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.MapRenderer;
import ru.bulldog.justmap.map.minimap.MapPosition;
import ru.bulldog.justmap.util.Colors;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.InGameHud;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.StatusEffectSpriteManager;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.util.math.MathHelper;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(InGameHud.class)
abstract class HudMixin extends DrawableHelper {
	
	@Shadow
	private MinecraftClient client;
	
	@Shadow
	private int scaledWidth;
	
	@Inject(at = @At("RETURN"), method = "render")
	public void draw(float delta, CallbackInfo info) {
		MapRenderer mapGui = MapRenderer.getInstance();
		if (mapGui != null) {
			mapGui.draw();
		}
	}
	
	@Inject(at = @At("HEAD"), method = "renderStatusEffectOverlay", cancellable = true)
	protected void renderStatusEffects(CallbackInfo info) {
		if (ClientParams.moveEffects) {
			Collection<StatusEffectInstance> collection = this.client.player.getStatusEffects();
			if (!collection.isEmpty()) {
				int posX = this.scaledWidth;
				if (ClientParams.mapPosition == MapPosition.TOP_RIGHT) {
					posX = MapRenderer.getInstance().getX();
				}
				
				RenderSystem.enableBlend();
				int i = 0;
				int j = 0;
				
				int size = 24;
				int hOffset = 6;
				int vOffset = 10;
				if (!ClientParams.showEffectTimers) {
					hOffset = 1;
					vOffset = 2;
				}
				
				StatusEffectSpriteManager statusEffectSpriteManager = this.client.getStatusEffectSpriteManager();
				List<Runnable> list = Lists.newArrayListWithExpectedSize(collection.size());
				List<Runnable> timers = Lists.newArrayListWithExpectedSize(collection.size());
				this.client.getTextureManager().bindTexture(HandledScreen.BACKGROUND_TEXTURE);
				Iterator<StatusEffectInstance> var6 = Ordering.natural().reverse().sortedCopy(collection).iterator();
	
			 	while(var6.hasNext()) {
			 		StatusEffectInstance statusEffectInstance = (StatusEffectInstance)var6.next();
					StatusEffect statusEffect = statusEffectInstance.getEffectType();
					if (statusEffectInstance.shouldShowIcon()) {
						int k = posX;
					   	int l = ClientParams.positionOffset;
					   	if (this.client.isDemo()) {
						   l += 15;
					   	}
	
					   	if (statusEffect.isBeneficial()) {
					   		++i;
						  	k -= (size + hOffset) * i;
					   	} else {
					   		++j;
						  	k -= (size + hOffset) * j;
						  	l += size + vOffset;
					   	}
	
				   		int effectDuration = statusEffectInstance.getDuration();
				   		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
				   		float f = 1.0F;
				   		if (statusEffectInstance.isAmbient()) {
				   			this.drawTexture(k, l, 165, 166, size, size);
				   		} else {
					   		this.drawTexture(k, l, 141, 166, size, size);
					  		if (effectDuration <= 200) {
						  		int m = 10 - effectDuration / 20;
						 		f = MathHelper.clamp((float)effectDuration / 10.0F / 5.0F * 0.5F, 0.0F, 0.5F) + MathHelper.cos((float)effectDuration * 3.1415927F / 5.0F) * MathHelper.clamp((float)m / 10.0F * 0.25F, 0.0F, 0.25F);
					  		}
				   		}
				   		
				   		Sprite sprite = statusEffectSpriteManager.getSprite(statusEffect);
				   		final int fk = k, fl = l;
				   		final float ff = f;
				   		list.add(() -> {
				   			this.client.getTextureManager().bindTexture(sprite.getAtlas().getId());
							RenderSystem.color4f(1.0F, 1.0F, 1.0F, ff);
							drawSprite(fk + 3, fl + 3, this.getZOffset(), 18, 18, sprite);
				   		});
				   		if (ClientParams.showEffectTimers) {
					   		timers.add(() -> {
					   			drawCenteredString(client.textRenderer, convertDuration(effectDuration), fk + size / 2, fl + (size + 1), Colors.WHITE);
					   		});
				   		}
					}
			 	}
	
			 	list.forEach(Runnable::run);
			 	timers.forEach(Runnable::run);
				
				info.cancel();
			}
		}
	}
	
	private String convertDuration(int time) {
		int mils = time * 50;
		int s = (mils / 1000) % 60;
		int m = (mils / (1000 * 60)) % 60;

		return String.format("%02d:%02d", m, s);
	}
}
