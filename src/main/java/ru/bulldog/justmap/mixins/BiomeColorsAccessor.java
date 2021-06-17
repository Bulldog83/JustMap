package ru.bulldog.justmap.mixins;

import java.util.Optional;
import net.minecraft.world.level.biome.BiomeSpecialEffects;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(BiomeSpecialEffects.class)
public interface BiomeColorsAccessor {
	@Accessor
	int getWaterColor();
	@Accessor
	Optional<Integer> getFoliageColorOverride();
	@Accessor
	Optional<Integer> getGrassColorOverride();
	@Accessor
	BiomeSpecialEffects.GrassColorModifier getGrassColorModifier();
}
