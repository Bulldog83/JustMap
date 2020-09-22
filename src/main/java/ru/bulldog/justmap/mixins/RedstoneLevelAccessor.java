package ru.bulldog.justmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.client.util.math.Vector3f;

@Mixin(RedstoneWireBlock.class)
public interface RedstoneLevelAccessor {
	@Accessor(value = "field_24466")
	static Vector3f[] getPowerVectors() {
		return null;
	}
}