package ru.bulldog.justmap.map;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.minecraft.entity.player.PlayerEntity;

import ru.bulldog.justmap.util.DataUtil;

public class MapPlayerManager {

	private MapPlayerManager() {}

	private static final Map<UUID, MapPlayer> players = new HashMap<>();

	public static MapPlayer getPlayer(PlayerEntity player) {
		UUID id = player.getUuid();
		if (players.containsKey(id)) {
			return players.get(id);
		}

		MapPlayer mapPlayer = new MapPlayer(DataUtil.getClientWorld(), player);
		players.put(id, mapPlayer);

		return mapPlayer;
	}
}
