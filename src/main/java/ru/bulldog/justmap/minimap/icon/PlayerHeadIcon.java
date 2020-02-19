package ru.bulldog.justmap.minimap.icon;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class PlayerHeadIcon {
	private static MinecraftClient minecraftClient = MinecraftClient.getInstance();
	private static PlayerSkinProvider skinProvider = minecraftClient.getSkinProvider();
	private static TextureManager textureManager = minecraftClient.getTextureManager();
	
	private static Map<UUID, PlayerHeadIcon> playerIcons = new HashMap<>();
	
	private long lastCheck;
	private int delay = 5000;
	private boolean succsses = false;
	
	private Identifier skin;	
	private PlayerEntity player;
	
	private PlayerHeadIcon(PlayerEntity player) {
		this.player = player;
	}
	
	public static PlayerHeadIcon getIcon(PlayerEntity player) {
		PlayerHeadIcon icon;
		long now = System.currentTimeMillis();
		
		if (playerIcons.containsKey(player.getUuid())) {
			icon = playerIcons.get(player.getUuid());
			
			if (!icon.succsses) {
				if (now - icon.lastCheck - icon.delay >= 0) {
					getPlayerSkin(icon);
				}
			} else if (now - icon.lastCheck >= 300000) {
				getPlayerSkin(icon);
			}
		} else {
			icon = new PlayerHeadIcon(player);
			registerIcon(icon);
		}

		return icon;
	}
	
	public void draw(int x, int y) {
		textureManager.bindTexture(this.skin);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		DrawableHelper.blit(x, y, 8, 8, 8.0F, 8.0F, 8, 8, 64, 64);
	}
	
	
	private static void registerIcon(PlayerHeadIcon icon) {
		getPlayerSkin(icon);
		playerIcons.put(icon.player.getUuid(), icon);
	}
	
	private static void getPlayerSkin(PlayerHeadIcon icon) {
		Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = skinProvider.getTextures(icon.player.getGameProfile());
	
		if (textures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
			icon.skin = skinProvider.loadSkin(textures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
			icon.succsses = true;
		} else {
			icon.skin = DefaultSkinHelper.getTexture(icon.player.getUuid());
			icon.succsses = false;
		}
		
		icon.lastCheck = System.currentTimeMillis();
	}
}
