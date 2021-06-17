package ru.bulldog.justmap.mixins.client;

import java.util.function.Supplier;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import ru.bulldog.justmap.event.ChunkUpdateEvent;
import ru.bulldog.justmap.event.ChunkUpdateListener;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.data.ChunkData;
import ru.bulldog.justmap.map.data.WorldData;
import ru.bulldog.justmap.map.data.WorldManager;
import ru.bulldog.justmap.map.data.Layer;
import ru.bulldog.justmap.util.DataUtil;

@Mixin(ClientLevel.class)
public abstract class ClientWorldMixin extends Level {
	
	protected ClientWorldMixin(WritableLevelData properties, ResourceKey<Level> registryKey,
			DimensionType dimensionType, Supplier<ProfilerFiller> supplier, boolean bl, boolean debugWorld, long l) {
		super(properties, registryKey, dimensionType, supplier, bl, debugWorld, l);
	}

	@Inject(method = "setKnownState", at = @At("TAIL"))
	public void onSetBlockState(BlockPos pos, BlockState state, CallbackInfo info) {
		LevelChunk worldChunk = this.getChunkAt(pos);
		if (!worldChunk.isEmpty()) {
			IMap map = DataUtil.getMap();
			Layer layer = DataUtil.getLayer(this, pos);
			int level = DataUtil.getLevel(layer, pos.getY());
			if (layer.equals(map.getLayer()) && level == map.getLevel()) {
				WorldData mapData = WorldManager.getData();
				if (mapData == null) return;
				ChunkPos chunkPos = worldChunk.getPos();
				int chunkX = chunkPos.x;
				int chunkZ = chunkPos.z;
				int x = (pos.getX() - chunkX) - 1;
				int z = (pos.getZ() - chunkZ) - 1;
				if (x < 0 && z < 0) {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, 0, 0, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX - 1, chunkZ, 14, 0, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX, chunkZ - 1, 0, 14, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX - 1, chunkZ - 1, 14, 14, 2, 2);
				} else if (x < 0 && z > 13) {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, 0, 14, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX - 1, chunkZ, 14, 14, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX, chunkZ + 1, 0, 0, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX - 1, chunkZ + 1, 14, 0, 2, 2);
				} else if (x > 13 && z < 0) {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, 14, 0, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX + 1, chunkZ, 0, 0, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX, chunkZ - 1, 14, 14, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX + 1, chunkZ - 1, 0, 14, 2, 2);
				} else if (x > 13 && z > 13) {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, 14, 14, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX + 1, chunkZ, 0, 14, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX, chunkZ + 1, 14, 0, 2, 2);
					this.updateChunk(mapData, layer, level, chunkX + 1, chunkZ + 1, 0, 0, 2, 2);
				} else if (x < 0) {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, 0, z, 2, 3);
					this.updateChunk(mapData, layer, level, chunkX - 1, chunkZ, 14, z, 2, 3);
				} else if (x > 13) {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, 14, z, 2, 3);
					this.updateChunk(mapData, layer, level, chunkX + 1, chunkZ, 0, z, 2, 3);
				} else if (z < 0) {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, x, 0, 3, 2);
					this.updateChunk(mapData, layer, level, chunkX, chunkZ - 1, x, 14, 3, 2);
				} else if (z > 13) {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, x, 14, 3, 2);
					this.updateChunk(mapData, layer, level, chunkX, chunkZ + 1, x, 0, 3, 2);
				} else {
					this.updateChunk(mapData, layer, level, chunkX, chunkZ, x, z, 3, 3);
				}
			}
		}
	}
	
	private void updateChunk(WorldData mapData, Layer layer, int level, int chx, int chz, int x, int z, int w, int h) {
		LevelChunk worldChunk = this.getChunk(chx, chz);
		if (worldChunk.isEmpty()) return;
		ChunkData mapChunk = mapData.getChunk(worldChunk.getPos());
		ChunkUpdateListener.accept(new ChunkUpdateEvent(worldChunk, mapChunk, layer, level, x, z, w, h, true));
	}
}
