package ru.bulldog.justmap.map;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;

import ru.bulldog.justmap.map.icon.PlayerHeadIconImage;

public class MapPlayer extends AbstractClientPlayerEntity {

	private final PlayerHeadIconImage icon;

	public MapPlayer(ClientWorld world, PlayerEntity player) {
		super(world, player.getGameProfile());

		this.icon = new PlayerHeadIconImage();
		this.icon.getPlayerSkin(this);
	}

	public PlayerHeadIconImage getIcon() {
		this.icon.checkForUpdate(this);
		return this.icon;
	}
}
