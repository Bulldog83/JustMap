package ru.bulldog.justmap.map.minimap;

import java.util.HashMap;
import java.util.Map;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.mixins.BooleanRuleProcessor;
import ru.bulldog.justmap.mixins.GameRulesProcessor;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.Key;

public class MapGameRules {

	public final static GameRules.Key<GameRules.BooleanRule> ALLOW_CAVES_MAP = register("allowCavesMap", false);
	public final static GameRules.Key<GameRules.BooleanRule> ALLOW_ENTITY_RADAR = register("allowEntityRadar", false);
	public final static GameRules.Key<GameRules.BooleanRule> ALLOW_PLAYER_RADAR = register("allowPlayerRadar", false);
	public final static GameRules.Key<GameRules.BooleanRule> ALLOW_CREATURE_RADAR = register("allowCreatureRadar", true);
	public final static GameRules.Key<GameRules.BooleanRule> ALLOW_HOSTILE_RADAR = register("allowHostileRadar", true);
	
	private MapGameRules() {}
	
	public static void init() {
		JustMap.LOGGER.logInfo("Map gamerules loaded.");
	}

	private static GameRules.Key<GameRules.BooleanRule> register(String name, boolean defaultValue) {
		return GameRulesProcessor.callRegister(name, GameRules.Category.MISC, BooleanRuleProcessor.callCreate(defaultValue));
	}
	
	private static Map<String, Key<GameRules.BooleanRule>> codes;
	
	static {
		codes = new HashMap<>();
		
		codes.put("§a", ALLOW_CAVES_MAP);
		codes.put("§b", ALLOW_ENTITY_RADAR);
		codes.put("§c", ALLOW_PLAYER_RADAR);
		codes.put("§d", ALLOW_CREATURE_RADAR);
		codes.put("§e", ALLOW_HOSTILE_RADAR);
	}
	
	/*
	 *	§0§0: prefix
	 *  §f§f: suffix
	 *  
	 *	§a: cave mapping
	 *	§b: entities radar (all)
	 *	§c: entities radar (player)
	 *	§d: entities radar (animal)
	 *	§e: entities radar (hostile)
	 *
	 *  §1: enable
	 *  §0: disable
	 */	
	@Environment(EnvType.CLIENT)
	public static void parseCommand(String command) {
		MinecraftClient client = MinecraftClient.getInstance();
		GameRules gameRules = client.world.getGameRules();
		codes.forEach((key, rule) -> {
			if (command.contains(key)) {
				int valPos = command.indexOf(key) + 2;
				boolean value = command.substring(valPos, valPos + 2).equals("§1");
				gameRules.get(rule).set(value, null);
				JustMap.LOGGER.logInfo(String.format("Map rule %s switched to: %s.", rule, value));
			}
		});
	}
	
	@Environment(EnvType.CLIENT)
	public static boolean isAllowed(GameRules.Key<GameRules.BooleanRule> rule) {
		MinecraftClient client = MinecraftClient.getInstance();
		
		boolean allow = true;
		if (client.isIntegratedServerRunning()) {
			allow = client.getServer().getGameRules().getBoolean(rule);
		} else if (!client.isInSingleplayer()) {
			allow = client.world.getGameRules().getBoolean(rule);
		}
		
		return allow;
	}
}
