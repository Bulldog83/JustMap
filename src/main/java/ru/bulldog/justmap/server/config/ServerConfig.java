package ru.bulldog.justmap.server.config;

import com.google.gson.JsonObject;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import ru.bulldog.justmap.config.Config;
import ru.bulldog.justmap.config.ConfigWriter;
import ru.bulldog.justmap.config.ConfigKeeper.BooleanEntry;

@Environment(EnvType.SERVER)
public class ServerConfig extends Config {
	
	private static ServerConfig instance;
	
	public static ServerConfig get() {
		if (instance == null) {
			instance = new ServerConfig();
		}
		
		return instance;
	}
	
	private ServerConfig() {
		KEEPER.registerEntry("use_game_rules", new BooleanEntry(ServerParams.useGameRules, (b) -> ServerParams.useGameRules = b, () -> ServerParams.useGameRules));
		KEEPER.registerEntry("allow_caves_map", new BooleanEntry(ServerParams.allowCavesMap, (b) -> ServerParams.allowCavesMap = b, () -> ServerParams.allowCavesMap));
		KEEPER.registerEntry("allow_entities_radar", new BooleanEntry(ServerParams.allowEntities, (b) -> ServerParams.allowEntities = b, () -> ServerParams.allowEntities));
		KEEPER.registerEntry("allow_hostile_radar", new BooleanEntry(ServerParams.allowHostile, (b) -> ServerParams.allowHostile = b, () -> ServerParams.allowHostile));
		KEEPER.registerEntry("allow_creatures_radar", new BooleanEntry(ServerParams.allowCreatures, (b) -> ServerParams.allowCreatures = b, () -> ServerParams.allowCreatures));
		KEEPER.registerEntry("allow_players_radar", new BooleanEntry(ServerParams.allowPlayers, (b) -> ServerParams.allowPlayers = b, () -> ServerParams.allowPlayers));
		KEEPER.registerEntry("allow_slime_chunks", new BooleanEntry(ServerParams.allowSlime, (b) -> ServerParams.allowSlime = b, () -> ServerParams.allowSlime));
		KEEPER.registerEntry("allow_waypoints_jumps", new BooleanEntry(ServerParams.allowTeleportation, (b) -> ServerParams.allowTeleportation = b, () -> ServerParams.allowTeleportation));
		
		JsonObject config = ConfigWriter.load();
		if (config.size() > 0) {
			KEEPER.fromJson(config);
		} else {
			ConfigWriter.save(KEEPER.toJson());
		}
	}

	@Override
	public void saveChanges()  {
		ConfigWriter.save(KEEPER.toJson());
	}
}
