package ru.bulldog.justmap.mixins;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import net.minecraft.world.GameRules;

@Mixin(GameRules.BooleanRule.class)
public interface BooleanRuleAccessor {
	@Invoker
	static GameRules.RuleType<GameRules.BooleanRule> callCreate(boolean value) {
		throw new AssertionError("@Invoker dummy body called");
	}
}
