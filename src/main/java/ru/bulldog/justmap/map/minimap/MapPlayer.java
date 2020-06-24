package ru.bulldog.justmap.map.minimap;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.player.PlayerEntity;

import ru.bulldog.justmap.map.icon.PlayerHeadIcon;

public class MapPlayer extends AbstractClientPlayerEntity {

	private final PlayerHeadIcon icon;
	
	public MapPlayer(PlayerEntity player) {
		super(MinecraftClient.getInstance().world, player.getGameProfile());
		
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
}
