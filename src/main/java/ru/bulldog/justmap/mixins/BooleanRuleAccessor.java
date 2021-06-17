package ru.bulldog.justmap.mixins;

import net.minecraft.world.level.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(GameRules.BooleanValue.class)
public interface BooleanRuleAccessor {
	@Invoker
	static GameRules.Type<GameRules.BooleanValue> callCreate(boolean value) {
		throw new AssertionError("@Invoker dummy body called");
	}
}
