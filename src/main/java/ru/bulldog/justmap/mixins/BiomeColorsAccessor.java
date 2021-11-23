package ru.bulldog.justmap.mixins;

import java.util.Optional;

import net.minecraft.world.biome.BiomeEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeEffects.class)
public interface BiomeColorsAccessor {
	@Accessor
	int getWaterColor();
	@Accessor
	Optional<Integer> getFoliageColor();
	@Accessor
	Optional<Integer> getGrassColor();
	@Accessor
	BiomeEffects.GrassColorModifier getGrassColorModifier();
}
