package ru.bulldog.justmap.map.icon;

import java.io.IOException;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.texture.AbstractTexture;
import net.minecraft.client.texture.PlayerSkinTexture;
import net.minecraft.client.texture.ResourceTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Identifier;

import net.minecraft.util.StringHelper;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.MapPlayer;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public class PlayerHeadIconImage {

	public long lastCheck;
	public final int delay = 5000;
	public boolean success = false;

	private ResourceTexture playerSkin;
	private Identifier skinId;

	public void draw(MatrixStack matrices, double x, double y) {
		// Draw other players
		int size = ClientSettings.entityIconSize;
		this.draw(matrices, x, y, size, ClientSettings.showIconsOutline);
	}

	public void draw(double x, double y, int size, boolean outline) {
		// Draw yourself
		MatrixStack matrix = new MatrixStack();
		this.draw(matrix, x, y, size, outline);
	}

	public void draw(MatrixStack matrices, double x, double y, int size, boolean outline) {
		double drawX = x - size / 2;
		double drawY = y - size / 2;
		if (outline) {
			double thickness = ClientSettings.entityOutlineSize;
			RenderUtil.fill(matrices, drawX - thickness / 2, drawY - thickness / 2, size + thickness, size + thickness, Colors.LIGHT_GRAY);
		}
		RenderUtil.bindTexture(this.skinId);
		RenderUtil.drawPlayerHead(matrices, drawX, drawY, size, size);
	}

	public void updatePlayerSkin(MapPlayer player) {
		JustMap.WORKER.execute("Update skin for: " + player.getName().getString(),
				() -> this.getPlayerSkin(player));
	}

	public void checkForUpdate(MapPlayer player) {
		long now = System.currentTimeMillis();
		if (!this.success) {
			if (now - this.lastCheck >= this.delay) {
				this.updatePlayerSkin(player);
			}
		} else if (now - this.lastCheck >= 300000) {
			this.updatePlayerSkin(player);
		}
	}

	public void getPlayerSkin(MapPlayer player) {
		this.lastCheck = System.currentTimeMillis();

		Identifier defaultSkin = DefaultSkinHelper.getTexture(player.getUuid());
		if (!player.getSkinTexture().equals(defaultSkin)) {
			ResourceTexture skinTexture = loadSkinTexture(player.getSkinTexture(), player.getName().getString());
			if (skinTexture != this.playerSkin) {
				if (this.playerSkin != null) {
					this.playerSkin.clearGlId();
				}
				this.playerSkin = skinTexture;
				this.skinId = player.getSkinTexture();

				try {
					this.playerSkin.load(MinecraftClient.getInstance().getResourceManager());
				} catch (IOException ex) {
					JustMap.LOGGER.warning(ex.getLocalizedMessage());
				}
				this.success = true;
			}
		} else if (this.playerSkin == null) {
			this.playerSkin = new ResourceTexture(defaultSkin);
			this.skinId = defaultSkin;
			this.success = false;

			try {
				this.playerSkin.load(MinecraftClient.getInstance().getResourceManager());
			} catch (IOException ex) {
				JustMap.LOGGER.warning(ex.getLocalizedMessage());
			}
		}
	}

	private ResourceTexture loadSkinTexture(Identifier id, String playerName) {
		TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
		AbstractTexture abstractTexture = textureManager.getTexture(id);
		if (abstractTexture == null) {
			abstractTexture = new PlayerSkinTexture(null, String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringHelper.stripTextFormat(playerName)), DefaultSkinHelper.getTexture(PlayerEntity.getOfflinePlayerUuid(playerName)), true, null);
			textureManager.registerTexture(id, abstractTexture);
		}
		return (ResourceTexture) abstractTexture;
	}
}
