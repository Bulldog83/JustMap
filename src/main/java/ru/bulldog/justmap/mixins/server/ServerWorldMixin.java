package ru.bulldog.justmap.mixins.server;

import java.util.Objects;
import java.util.function.Supplier;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import net.minecraft.block.BlockState;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.profiler.Profiler;
import net.minecraft.util.registry.Registry;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.MutableWorldProperties;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.util.JsonFactory;
import ru.bulldog.justmap.util.StateUtil;

@Mixin(ServerWorld.class)
public abstract class ServerWorldMixin extends World {

	protected ServerWorldMixin(MutableWorldProperties mutableWorldProperties, RegistryKey<World> registryKey,
			RegistryKey<DimensionType> registryKey2, DimensionType dimensionType, Supplier<Profiler> profiler,
			boolean bl, boolean bl2, long l) {
		super(mutableWorldProperties, registryKey, registryKey2, dimensionType, profiler, bl, bl2, l);
	}

	@Inject(method = "onBlockChanged", at = @At("HEAD"))
	public void onBlockChanged(BlockPos pos, BlockState oldBlock, BlockState newBlock, CallbackInfo info) {
		if (!Objects.equals(oldBlock.getBlock(), newBlock.getBlock())) {
			if (!StateUtil.isAir(newBlock)) {
				Identifier blockId = Registry.BLOCK.getId(newBlock.getBlock());
				String modId = blockId.getNamespace();
				String id = blockId.getPath();
				String stateFile = String.format("/assets/%s/blockstates/%s.json", modId, id);
				try {
					JsonObject stateJson = JsonFactory.getJsonObject(stateFile);
					if (stateJson.has("variants")) {
						JsonObject variants = stateJson.getAsJsonObject("variants");
						variants.entrySet().forEach((entry) -> {
							System.out.println(entry.getKey());
							if (entry.getValue().isJsonArray()) {
								JsonArray entryArray = entry.getValue().getAsJsonArray();
								entryArray.forEach(elem -> {
								});
							} else {
							}
						});
					}
				} catch (Exception ex) {
					System.out.println(String.format("Blockstate %s not available.", stateFile));
				}
			}
		}
	}
}
