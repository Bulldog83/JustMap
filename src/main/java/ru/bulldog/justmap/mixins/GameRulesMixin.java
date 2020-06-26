package ru.bulldog.justmap.mixins;

import org.spongepowered.asm.mixin.Mixin;

import net.minecraft.world.GameRules;
import ru.bulldog.justmap.map.MapGameRules;

@Mixin(GameRules.class)
public abstract class GameRulesMixin {	
	static {
		MapGameRules.init();
	}
}
