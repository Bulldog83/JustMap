package ru.bulldog.justmap.map.icon;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.MapPlayer;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.IOException;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;

public class PlayerHeadIcon {
	
	public long lastCheck;
	public int delay = 5000;
	public boolean success = false;
	
	private SimpleTexture playerSkin;
	
	public void draw(PoseStack matrices, double x, double y) {
		int size = ClientSettings.entityIconSize;
		this.draw(matrices, x, y, size, ClientSettings.showIconsOutline);
	}
	
	public void draw(double x, double y, int size, boolean outline) {
		PoseStack matrix = new PoseStack();
		this.draw(matrix, x, y, size, outline);
	}

	public void draw(PoseStack matrices, double x, double y, int size, boolean outline) {		
		double drawX = x - size / 2;
		double drawY = y - size / 2;
		y -= size / 2;
		if (outline) {
			double thickness = ClientSettings.entityOutlineSize;
			RenderUtil.fill(drawX - thickness / 2, drawY - thickness / 2, size + thickness, size + thickness, Colors.LIGHT_GRAY);
		}
		this.playerSkin.bind();	
		RenderUtil.drawPlayerHead(matrices, drawX, drawY, size, size);
	}
	
	public void updatePlayerSkin(MapPlayer player) {
		JustMap.WORKER.execute("Update skin for: " + player.getName().getString(),
				() -> this.getPlayerSkin(player));
	}
	
	public void getPlayerSkin(MapPlayer player) {
		this.lastCheck = System.currentTimeMillis();
		
		ResourceLocation defaultSkin = DefaultPlayerSkin.getDefaultSkin(player.getUUID());
		if (!player.getSkinTextureLocation().equals(defaultSkin)) {
			SimpleTexture skinTexture = MapPlayer.loadSkinTexture(player.getSkinTextureLocation(), player.getName().getString());
			if (skinTexture != this.playerSkin) {
				if (this.playerSkin != null) {
					this.playerSkin.releaseId();
				}
				this.playerSkin = skinTexture;

				try {
					this.playerSkin.load(Minecraft.getInstance().getResourceManager());
				} catch (IOException ex) {
					JustMap.LOGGER.warning(ex.getLocalizedMessage());
				}
				this.success = true;
			}
		} else if (this.playerSkin == null) {
			this.playerSkin = new SimpleTexture(defaultSkin);
			this.success = false;
			
			try {
				this.playerSkin.load(Minecraft.getInstance().getResourceManager());
			} catch (IOException ex) {
				JustMap.LOGGER.warning(ex.getLocalizedMessage());
			}
		}
	}
}
