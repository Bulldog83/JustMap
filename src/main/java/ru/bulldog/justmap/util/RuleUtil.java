package ru.bulldog.justmap.util;

import net.fabricmc.api.EnvType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.MapGameRules;
import ru.bulldog.justmap.server.config.ServerSettings;

public class RuleUtil {

	public static boolean isAllowed(boolean param, GameRules.Key<GameRules.BooleanValue> rule, boolean isServer) {
		if (isServer) {
			if (ServerSettings.useGameRules) {
				return MapGameRules.isAllowed(rule);
			} else {
				return param;
			}
		} else if (param) {
			return Minecraft.getInstance().isLocalServer() || MapGameRules.isAllowed(rule);
		}
		
		return false;
	}
	
	public static boolean detectMultiworlds() {
		return ClientSettings.detectMultiworlds;
	}

	public static boolean needRenderCaves(Level world, BlockPos pos) {
		boolean allowCaves = false;
		if (JustMap.getSide() == EnvType.SERVER) {
			allowCaves = isAllowed(ServerSettings.allowCavesMap, MapGameRules.ALLOW_CAVES_MAP, true);
		} else {
			allowCaves = isAllowed(ClientSettings.drawCaves, MapGameRules.ALLOW_CAVES_MAP, false);
		}
		
		if (Dimension.isEnd(world)) {
			return false;
		}
		DimensionType dimType = world.dimensionType();
		if (!dimType.hasCeiling() && dimType.hasSkyLight()) {
			return allowCaves && (!world.canSeeSkyFromBelowWater(pos) && !DataUtil.hasSkyLight(world, pos) ||
				   world.dimension().location().equals(DimensionType.OVERWORLD_CAVES_LOCATION.location()));
		}
		
		return allowCaves;
	}
	
	public static boolean allowEntityRadar() {
		if (JustMap.getSide() == EnvType.SERVER) {
			return isAllowed(ServerSettings.allowEntities, MapGameRules.ALLOW_ENTITY_RADAR, true);
		}
		return isAllowed(ClientSettings.showEntities, MapGameRules.ALLOW_ENTITY_RADAR, false);
	}

	public static boolean allowHostileRadar() {
		if (JustMap.getSide() == EnvType.SERVER) {
			return isAllowed(ServerSettings.allowHostile, MapGameRules.ALLOW_HOSTILE_RADAR, true);
		}
		return isAllowed(ClientSettings.showHostile, MapGameRules.ALLOW_HOSTILE_RADAR, false);
	}

	public static boolean allowCreatureRadar() {
		if (JustMap.getSide() == EnvType.SERVER) {
			return isAllowed(ServerSettings.allowCreatures, MapGameRules.ALLOW_CREATURE_RADAR, true);
		}
		return isAllowed(ClientSettings.showCreatures, MapGameRules.ALLOW_CREATURE_RADAR, false);
	}

	public static boolean allowPlayerRadar() {
		if (JustMap.getSide() == EnvType.SERVER) {
			return isAllowed(ServerSettings.allowPlayers, MapGameRules.ALLOW_PLAYER_RADAR, true);
		}
		return isAllowed(ClientSettings.showPlayers, MapGameRules.ALLOW_PLAYER_RADAR, false);
	}

	public static boolean allowSlimeChunks() {
		if (JustMap.getSide() == EnvType.SERVER) {
			return isAllowed(ServerSettings.allowSlime, MapGameRules.ALLOW_SLIME_CHUNKS, true);
		}
		return isAllowed(ClientSettings.showSlime, MapGameRules.ALLOW_SLIME_CHUNKS, false);
	}

	public static boolean allowTeleportation() {
		if (JustMap.getSide() == EnvType.SERVER) {
			return isAllowed(ServerSettings.allowTeleportation, MapGameRules.ALLOW_TELEPORTATION, true);
		}
		return isAllowed(ClientSettings.jumpToWaypoints, MapGameRules.ALLOW_TELEPORTATION, false);
	}
}
