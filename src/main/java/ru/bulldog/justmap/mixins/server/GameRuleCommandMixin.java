package ru.bulldog.justmap.mixins.server;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.Util;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.commands.GameRuleCommand;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameRules.BooleanValue;
import ru.bulldog.justmap.server.config.ServerSettings;

@Mixin(GameRuleCommand.class)
public abstract class GameRuleCommandMixin {
	
	@Inject(method = "executeSet", at = @At("RETURN"))
	private static <T extends GameRules.Value<T>> void executeSet(CommandContext<CommandSourceStack> commandContext, GameRules.Key<T> Key, CallbackInfoReturnable<Integer> cir) {
		if (ServerSettings.useGameRules) {
			CommandSourceStack serverCommandSource = (CommandSourceStack)commandContext.getSource();
			T rule = serverCommandSource.getServer().getGameRules().getRule(Key);
			
			if (rule instanceof BooleanValue) {
				Component command;
				
				String val = ((BooleanValue) rule).get() ? "§1" : "§0";
				switch (Key.getId()) {
					case "allowCavesMap":
						command = new TextComponent(String.format("§0§0§a%s§f§f", val));
						serverCommandSource.getServer().getPlayerList().broadcastAll(
								new ClientboundChatPacket(command, ChatType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowEntityRadar":
						command = new TextComponent(String.format("§0§0§b%s§f§f", val));
						serverCommandSource.getServer().getPlayerList().broadcastAll(
								new ClientboundChatPacket(command, ChatType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowPlayerRadar":
						command = new TextComponent(String.format("§0§0§c%s§f§f", val));
						serverCommandSource.getServer().getPlayerList().broadcastAll(
								new ClientboundChatPacket(command, ChatType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowCreatureRadar":
						command = new TextComponent(String.format("§0§0§d%s§f§f", val));
						serverCommandSource.getServer().getPlayerList().broadcastAll(
								new ClientboundChatPacket(command, ChatType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowHostileRadar":
						command = new TextComponent(String.format("§0§0§e%s§f§f", val));
						serverCommandSource.getServer().getPlayerList().broadcastAll(
								new ClientboundChatPacket(command, ChatType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowSlimeChunks":
						command = new TextComponent(String.format("§0§0§s%s§f§f", val));
						serverCommandSource.getServer().getPlayerList().broadcastAll(
								new ClientboundChatPacket(command, ChatType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowWaypointsJump":
						command = new TextComponent(String.format("§0§0§t%s§f§f", val));
						serverCommandSource.getServer().getPlayerList().broadcastAll(
								new ClientboundChatPacket(command, ChatType.SYSTEM, Util.NIL_UUID));
						break;
				}
			}
		}
	}
}
