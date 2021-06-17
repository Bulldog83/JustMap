package ru.bulldog.justmap.map;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.HttpTexture;
import net.minecraft.client.renderer.texture.SimpleTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.StringUtil;
import net.minecraft.world.entity.player.Player;
import ru.bulldog.justmap.map.icon.PlayerHeadIcon;

public class MapPlayer extends AbstractClientPlayer {

	private final PlayerHeadIcon icon;
	
	public MapPlayer(ClientLevel world, Player player) {
		super(world, player.getGameProfile());
		
		this.icon = new PlayerHeadIcon();
		this.icon.getPlayerSkin(this);
	}
	
	public PlayerHeadIcon getIcon() {
		long now = System.currentTimeMillis();		
		if (!icon.success) {
			if (now - icon.lastCheck >= icon.delay) {
				this.icon.updatePlayerSkin(this);
			}
		} else if (now - icon.lastCheck >= 300000) {
			this.icon.updatePlayerSkin(this);
		}
		
		return this.icon;
	}
	
	public static SimpleTexture loadSkinTexture(ResourceLocation id, String playerName) {
		TextureManager textureManager = Minecraft.getInstance().getTextureManager();
		AbstractTexture abstractTexture = textureManager.getTexture(id);
		if (abstractTexture == null) {
			abstractTexture = new HttpTexture(null, String.format("http://skins.minecraft.net/MinecraftSkins/%s.png", StringUtil.stripColor(playerName)), DefaultPlayerSkin.getDefaultSkin(createPlayerUUID(playerName)), true, null);
			textureManager.register(id, (AbstractTexture) abstractTexture);
		}
		return (SimpleTexture) abstractTexture;
	}
}
