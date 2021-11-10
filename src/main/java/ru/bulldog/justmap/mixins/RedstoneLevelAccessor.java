package ru.bulldog.justmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import net.minecraft.block.RedstoneWireBlock;
import net.minecraft.util.math.Vec3d;

@Mixin(RedstoneWireBlock.class)
public interface RedstoneLevelAccessor {
	@Accessor(value = "COLORS")
	static Vec3d[] getPowerVectors() {
		return null;
	}
}
