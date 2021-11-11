package ru.bulldog.justmap.util;

import net.fabricmc.api.EnvType;
import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.MapGameRules;
import ru.bulldog.justmap.server.config.ServerSettings;

public class RuleUtil {

	public static boolean isAllowed(boolean param, GameRules.Key<GameRules.BooleanRule> rule, boolean isServer) {
		if (isServer) {
			if (ServerSettings.useGameRules) {
				return MapGameRules.isAllowed(rule);
			} else {
				return param;
			}
		} else if (param) {
			return MinecraftClient.getInstance().isInSingleplayer() || MapGameRules.isAllowed(rule);
		}

		return false;
	}

	public static boolean detectMultiworlds() {
		return ClientSettings.detectMultiworlds;
	}

	public static boolean needRenderCaves(World world, BlockPos pos) {
		boolean allowCaves;
		if (JustMap.getSide() == EnvType.SERVER) {
			allowCaves = isAllowed(ServerSettings.allowCavesMap, MapGameRules.ALLOW_CAVES_MAP, true);
		} else {
			allowCaves = isAllowed(ClientSettings.drawCaves, MapGameRules.ALLOW_CAVES_MAP, false);
		}

		if (Dimension.isEnd(world)) {
			return false;
		}
		DimensionType dimType = world.getDimension();
		if (!dimType.hasCeiling() && dimType.hasSkyLight()) {
			return allowCaves && (!world.isSkyVisibleAllowingSea(pos) && !DataUtil.hasSkyLight(world, pos) ||
				   world.getRegistryKey().getValue().equals(DimensionType.OVERWORLD_CAVES_REGISTRY_KEY.getValue()));
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
