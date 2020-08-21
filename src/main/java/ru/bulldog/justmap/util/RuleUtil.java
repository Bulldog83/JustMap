package ru.bulldog.justmap.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.MapGameRules;

public class RuleUtil {

	public static boolean isAllowed(boolean param, GameRules.Key<GameRules.BooleanRule> rule) {
		if (param) {
			return DataUtil.getMinecraft().isInSingleplayer() || MapGameRules.isAllowed(rule);
		}
		
		return false;
	}
	
	public static boolean detectMultiworlds() {
		return ClientParams.detectMultiworlds;
	}

	public static boolean needRenderCaves(World world, BlockPos pos) {
		boolean allowCaves = isAllowed(ClientParams.drawCaves, MapGameRules.ALLOW_CAVES_MAP);
		
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
		return isAllowed(ClientParams.showEntities, MapGameRules.ALLOW_ENTITY_RADAR);
	}

	public static boolean allowHostileRadar() {
		return isAllowed(ClientParams.showHostile, MapGameRules.ALLOW_HOSTILE_RADAR);
	}

	public static boolean allowCreatureRadar() {
		return isAllowed(ClientParams.showCreatures, MapGameRules.ALLOW_CREATURE_RADAR);
	}

	public static boolean allowPlayerRadar() {
		return isAllowed(ClientParams.showPlayers, MapGameRules.ALLOW_PLAYER_RADAR);
	}

	public static boolean allowSlimeChunks() {
		return isAllowed(ClientParams.showSlime, MapGameRules.ALLOW_SLIME_CHUNKS);
	}

	public static boolean allowTeleportation() {
		return isAllowed(ClientParams.jumpToWaypoints, MapGameRules.ALLOW_TELEPORTATION);
	}
}
