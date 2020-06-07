package ru.bulldog.justmap.map.icon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Identifier;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;

import java.util.Map;
import java.io.IOException;
import java.util.HashMap;
import java.util.UUID;

public class PlayerHeadIcon {
	private static Map<UUID, PlayerHeadIcon> playerIcons = new HashMap<>();
	
	private long lastCheck;
	private int delay = 5000;
	private boolean success = false;
	
	private ResourceTexture playerSkin;	
	private ClientPlayerEntity player;
	
	private PlayerHeadIcon(ClientPlayerEntity player) {
		this.player = player;
	}
	
	public static PlayerHeadIcon getIcon(ClientPlayerEntity player) {
		PlayerHeadIcon icon;
		long now = System.currentTimeMillis();
		
		if (playerIcons.containsKey(player.getUuid())) {
			icon = playerIcons.get(player.getUuid());
			
			if (!icon.success) {
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
	
	public void draw(MatrixStack matrix, double x, double y) {
		int size = ClientParams.entityIconSize;
		this.draw(matrix, x, y, size, ClientParams.showIconsOutline);
	}
	
	public void draw(double x, double y, int size, boolean outline) {
		MatrixStack matrix = new MatrixStack();
		this.draw(matrix, x, y, size, outline);
	}

	public void draw(MatrixStack matrix, double x, double y, int size, boolean outline) {		
		double drawX = x - size / 2;
		double drawY = y - size / 2;
		y -= size / 2;
		if (outline) {
			DrawHelper.fill(drawX - 0.5, drawY - 0.5, drawX + size + 0.5, drawY + size + 0.5, Colors.LIGHT_GRAY);
		}
		this.playerSkin.bindTexture();	
		DrawHelper.drawPlayerHead(matrix, drawX, drawY, size, size);
	}
	
	private static void updatePlayerSkin(PlayerHeadIcon icon) {
		JustMap.WORKER.execute(() -> {
			getPlayerSkin(icon);
		});
	}	
	
	private static void registerIcon(PlayerHeadIcon icon) {
		getPlayerSkin(icon);
		playerIcons.put(icon.player.getUuid(), icon);
	}
	
	private static void getPlayerSkin(PlayerHeadIcon icon) {
		icon.lastCheck = System.currentTimeMillis();
		
		Identifier defaultSkin = DefaultSkinHelper.getTexture(icon.player.getUuid());
		if (!icon.player.getSkinTexture().equals(defaultSkin)) {
			PlayerSkinTexture skinTexture = ClientPlayerEntity.loadSkin(icon.player.getSkinTexture(), icon.player.getName().getString());
			if (skinTexture != icon.playerSkin) {
				if (icon.playerSkin != null) {
					icon.playerSkin.clearGlId();
				}
				icon.playerSkin = skinTexture;

				try {
					icon.playerSkin.load(MinecraftClient.getInstance().getResourceManager());
				} catch (IOException ex) {
					JustMap.LOGGER.logWarning(ex.getLocalizedMessage());
				}
				icon.success = true;
			}
		} else if (icon.playerSkin == null) {
			icon.playerSkin = new ResourceTexture(defaultSkin);
			icon.success = false;
			
			try {
				icon.playerSkin.load(MinecraftClient.getInstance().getResourceManager());
			} catch (IOException ex) {
				JustMap.LOGGER.logWarning(ex.getLocalizedMessage());
			}
		}
	}
}
