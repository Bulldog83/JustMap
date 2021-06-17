package ru.bulldog.justmap.mixins.server;

import java.util.Objects;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.storage.WritableLevelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.StateUtil;

@Mixin(ServerLevel.class)
public abstract class ServerWorldMixin extends Level {
	protected ServerWorldMixin(WritableLevelData properties, ResourceKey<Level> registryKey,
			DimensionType dimensionType, Supplier<ProfilerFiller> supplier, boolean bl, boolean debugWorld, long l) {
		super(properties, registryKey, dimensionType, supplier, bl, debugWorld, l);
	}

	@Inject(method = "onBlockChanged", at = @At("HEAD"))
	public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo info) {
//		if (!Objects.equals(oldBlock.getBlock(), newBlock.getBlock())) {
//			if (!StateUtil.isAir(newBlock)) {
//				Identifier blockId = Registry.BLOCK.getId(newBlock.getBlock());
//				String modId = blockId.getNamespace();
//				String id = blockId.getPath();
//				String stateFile = String.format("/assets/%s/blockstates/%s.json", modId, id);
//				try {
//					JsonObject stateJson = JsonFactory.getJsonObject(stateFile);
//					if (stateJson.has("variants")) {
//						JsonObject variants = stateJson.getAsJsonObject("variants");
//						variants.entrySet().forEach((entry) -> {
//							System.out.println(entry.getKey());
//							if (entry.getValue().isJsonArray()) {
//								JsonArray entryArray = entry.getValue().getAsJsonArray();
//								entryArray.forEach(elem -> {
//								});
//							} else {
//							}
//						});
//					}
//				} catch (Exception ex) {
//					System.out.println(String.format("Blockstate %s not available.", stateFile));
//				}
//			}
//		}
	}
}
