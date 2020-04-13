package ru.bulldog.justmap.map.icon;

import com.mojang.authlib.minecraft.MinecraftProfileTexture;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.*;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.DrawHelper;

import java.util.Map;
import java.util.HashMap;
import java.util.UUID;

public class PlayerHeadIcon {
	private static PlayerSkinProvider skinProvider = MinecraftClient.getInstance().getSkinProvider();
	private static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	
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
					updatePlayerSkin(icon);
				}
			} else if (now - icon.lastCheck >= 300000) {
				updatePlayerSkin(icon);
			}
		} else {
			icon = new PlayerHeadIcon(player);
			registerIcon(icon);			
		}

		return icon;
	}
	
	public void draw(double x, double y) {
		int size = ClientParams.entityIconSize;
		if (ClientParams.showIconsOutline) {
			DrawHelper.fill(x - 1, y - 1, x + size + 1, y + size + 1, 0xFF444444);
		}
		textureManager.bindTexture(this.skin);
		
		DrawHelper.draw(x, y, size, size);
	}
	
	private static void updatePlayerSkin(PlayerHeadIcon icon) {
		JustMap.EXECUTOR.execute(() -> {
			getPlayerSkin(icon);
		});
	}
	
	private static void registerIcon(PlayerHeadIcon icon) {
		getPlayerSkin(icon);
		playerIcons.put(icon.player.getUuid(), icon);
	}
	
	private static void getPlayerSkin(PlayerHeadIcon icon) {
		Map<MinecraftProfileTexture.Type, MinecraftProfileTexture> textures = skinProvider.getTextures(icon.player.getGameProfile());
	
		icon.lastCheck = System.currentTimeMillis();
		
		if (textures.containsKey(MinecraftProfileTexture.Type.SKIN)) {
			icon.skin = skinProvider.loadSkin(textures.get(MinecraftProfileTexture.Type.SKIN), MinecraftProfileTexture.Type.SKIN);
			icon.succsses = true;
		} else {
			icon.skin = DefaultSkinHelper.getTexture(icon.player.getUuid());
			icon.succsses = false;
		}
	}
}
