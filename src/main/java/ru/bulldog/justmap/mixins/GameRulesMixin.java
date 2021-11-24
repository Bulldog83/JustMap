package ru.bulldog.justmap.mixins;

import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Mixin;

import ru.bulldog.justmap.map.MapGameRules;

@Mixin(GameRules.class)
public abstract class GameRulesMixin {
	static {
		MapGameRules.init();
	}
}
