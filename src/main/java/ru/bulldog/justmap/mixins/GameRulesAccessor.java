package ru.bulldog.justmap.mixins;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.class)
public interface GameRulesAccessor {
	@Invoker
	static <T extends GameRules.Value<T>> GameRules.Key<T> callRegister(String name, GameRules.Category category, GameRules.Type<T> type) {
		throw new AssertionError("@Invoker dummy body called");
	}
}
