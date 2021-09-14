package ru.bulldog.justmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Vec3f;

@Mixin(RedstoneWireBlock.class)
public interface RedstoneLevelAccessor {
	@Accessor(value = "field_24466")
	static Vec3f[] getPowerVectors() {
		return null;
	}
}