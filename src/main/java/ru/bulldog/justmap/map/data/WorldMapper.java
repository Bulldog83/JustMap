package ru.bulldog.justmap.map.data;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.server.command.ServerCommandSource;

import ru.bulldog.justmap.map.IMap;

public interface WorldMapper {
	MapRegion getMapRegion(IMap map, int blockX, int blockZ);

	int getMapHeight(Layer mapLayer, int mapLevel, int posX, int posZ);

	void onWorldMapperClose();

	int onMapCommand(CommandContext<ServerCommandSource> context);
}
