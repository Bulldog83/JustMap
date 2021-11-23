package ru.bulldog.justmap.server.config;

import com.google.gson.JsonObject;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import ru.bulldog.justmap.config.Config;
import ru.bulldog.justmap.config.ConfigKeeper.BooleanEntry;
import ru.bulldog.justmap.config.ConfigWriter;

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
		KEEPER.registerEntry("use_game_rules", new BooleanEntry(ServerSettings.useGameRules, (b) -> ServerSettings.useGameRules = b, () -> ServerSettings.useGameRules));
		KEEPER.registerEntry("allow_caves_map", new BooleanEntry(ServerSettings.allowCavesMap, (b) -> ServerSettings.allowCavesMap = b, () -> ServerSettings.allowCavesMap));
		KEEPER.registerEntry("allow_entities_radar", new BooleanEntry(ServerSettings.allowEntities, (b) -> ServerSettings.allowEntities = b, () -> ServerSettings.allowEntities));
		KEEPER.registerEntry("allow_hostile_radar", new BooleanEntry(ServerSettings.allowHostile, (b) -> ServerSettings.allowHostile = b, () -> ServerSettings.allowHostile));
		KEEPER.registerEntry("allow_creatures_radar", new BooleanEntry(ServerSettings.allowCreatures, (b) -> ServerSettings.allowCreatures = b, () -> ServerSettings.allowCreatures));
		KEEPER.registerEntry("allow_players_radar", new BooleanEntry(ServerSettings.allowPlayers, (b) -> ServerSettings.allowPlayers = b, () -> ServerSettings.allowPlayers));
		KEEPER.registerEntry("allow_slime_chunks", new BooleanEntry(ServerSettings.allowSlime, (b) -> ServerSettings.allowSlime = b, () -> ServerSettings.allowSlime));
		KEEPER.registerEntry("allow_waypoints_jumps", new BooleanEntry(ServerSettings.allowTeleportation, (b) -> ServerSettings.allowTeleportation = b, () -> ServerSettings.allowTeleportation));
		
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
