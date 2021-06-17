package ru.bulldog.justmap.mixins;

import net.minecraft.world.level.block.RedStoneWireBlock;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RedStoneWireBlock.class)
public interface RedstoneLevelAccessor {

	@Accessor(value = "COLORS")
	@NotNull static Vec3[] getColors() {
		return null;
	}
}