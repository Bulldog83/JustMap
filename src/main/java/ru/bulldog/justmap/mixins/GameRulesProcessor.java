package ru.bulldog.justmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.GameRules;

@Mixin(GameRules.class)
public interface GameRulesProcessor {
	@Invoker
	static <T extends GameRules.Rule<T>> GameRules.RuleKey<T> callRegister(String name, GameRules.RuleType<T> type) {
		throw new AssertionError("@Invoker dummy body called");
	}
}
