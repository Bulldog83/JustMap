package ru.bulldog.justmap.map;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;

public class MapPlayerManager {

	private MapPlayerManager() {}
	
	private static Map<UUID, MapPlayer> players = new HashMap<>();
	
	public static MapPlayer getPlayer(PlayerEntity player) {
		UUID id = player.getUuid();
		if (players.containsKey(id)) {
			return players.get(id);
		}
		
		MinecraftClient client = MinecraftClient.getInstance();
		MapPlayer mapPlayer = new MapPlayer(client.world, player);
		players.put(id, mapPlayer);
		
		return mapPlayer;
	}
}
