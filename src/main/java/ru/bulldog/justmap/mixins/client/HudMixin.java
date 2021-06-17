package ru.bulldog.justmap.mixins.client;

import com.google.common.collect.Lists;
import com.google.common.collect.Ordering;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.util.colors.Colors;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.MobEffectTextureManager;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Gui.class)
abstract class HudMixin extends GuiComponent {
	
	@Shadow
	private Minecraft minecraft;
	
	@Shadow
	private int screenWidth;
	
	@Inject(at = @At("HEAD"), method = "renderEffects", cancellable = true)
	protected void renderStatusEffects(PoseStack matrices, CallbackInfo info) {
		if (ClientSettings.moveEffects) {
			int posX = screenWidth;
			int posY = ClientSettings.positionOffset;
			if (ClientSettings.mapPosition == ScreenPosition.TOP_RIGHT) {
				posX = JustMapClient.getMap().getSkinX();
			}
			
			drawMovedEffects(matrices, posX, posY);
			info.cancel();
		}
	}
	
	private void drawMovedEffects(PoseStack matrixStack, int screenX, int screenY) {
		Collection<MobEffectInstance> statusEffects = minecraft.player.getActiveEffects();
		if (statusEffects.isEmpty()) return;
		
		RenderSystem.enableBlend();
		
		int size = 24;
		int hOffset = 6;
		int vOffset = 10;
		
		if (!ClientSettings.showEffectTimers) {
			hOffset = 1;
			vOffset = 2;
		}
		
		MobEffectTextureManager statusEffectSpriteManager = minecraft.getMobEffectTextures();
		List<Runnable> icons = Lists.newArrayListWithExpectedSize(statusEffects.size());
		List<Runnable> timers = Lists.newArrayListWithExpectedSize(statusEffects.size());
		RenderSystem.setShaderTexture(0, AbstractContainerScreen.INVENTORY_LOCATION);
		Iterator<MobEffectInstance> effectsIterator = Ordering.natural().reverse().sortedCopy(statusEffects).iterator();

	 	int i = 0, j = 0;
		while(effectsIterator.hasNext()) {
	 		MobEffectInstance statusEffectInstance = effectsIterator.next();
			MobEffect statusEffect = statusEffectInstance.getEffect();
			if (statusEffectInstance.showIcon()) {
				int x = screenX;
			   	int y = screenY;
			   	if (this.minecraft.isDemo()) {
				   y += 15;
			   	}

			   	if (statusEffect.isBeneficial()) {
			   		++i;
				  	x -= (size + hOffset) * i;
			   	} else {
			   		++j;
				  	x -= (size + hOffset) * j;
				  	y += size + vOffset;
			   	}

		   		int effectDuration = statusEffectInstance.getDuration();
		   		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		   		float alpha = 1.0F;
		   		if (statusEffectInstance.isAmbient()) {
		   			this.blit(matrixStack, x, y, 165, 166, size, size);
		   		} else {
			   		this.blit(matrixStack, x, y, 141, 166, size, size);
			  		if (effectDuration <= 200) {
				  		int m = 10 - effectDuration / 20;
				 		alpha = Mth.clamp(effectDuration / 10F / 5F * 0.5F, 0F, 0.5F) + Mth.cos((float) (effectDuration * Math.PI) / 5F) * Mth.clamp(m / 10F * 0.25F, 0.0F, 0.25F);
			  		}
		   		}
		   		
		   		TextureAtlasSprite sprite = statusEffectSpriteManager.get(statusEffect);
		   		final int fx = x, fy = y;
		   		final float fa = alpha;
		   		icons.add(() -> {
				    RenderSystem.setShaderTexture(0, sprite.atlas().location());
					RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, fa);
					blit(matrixStack, fx + 3, fy + 3, getBlitOffset(), 18, 18, sprite);
		   		});
		   		if (ClientSettings.showEffectTimers) {
			   		timers.add(() -> {
			   			drawCenteredString(matrixStack, minecraft.font, convertDuration(effectDuration), fx + size / 2, fy + (size + 1), Colors.WHITE);
			   		});
		   		}
			}
	 	}

	 	icons.forEach(Runnable::run);
	 	timers.forEach(Runnable::run);
	}
	
	private String convertDuration(int time) {
		int mils = time * 50;
		int s = (mils / 1000) % 60;
		int m = (mils / (1000 * 60)) % 60;

		return String.format("%02d:%02d", m, s);
	}
}
