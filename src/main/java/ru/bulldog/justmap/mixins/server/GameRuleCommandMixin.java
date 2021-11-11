package ru.bulldog.justmap.mixins.server;

import com.mojang.brigadier.context.CommandContext;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.command.GameRuleCommand;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Util;
import net.minecraft.world.GameRules;
import net.minecraft.world.GameRules.BooleanRule;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import ru.bulldog.justmap.server.config.ServerSettings;

@Mixin(GameRuleCommand.class)
public abstract class GameRuleCommandMixin {

	@Inject(method = "executeSet", at = @At("RETURN"))
	private static <T extends GameRules.Rule<T>> void executeSet(CommandContext<ServerCommandSource> commandContext, GameRules.Key<T> Key, CallbackInfoReturnable<Integer> cir) {
		if (ServerSettings.useGameRules) {
			ServerCommandSource serverCommandSource = commandContext.getSource();
			T rule = serverCommandSource.getServer().getGameRules().get(Key);

			if (rule instanceof BooleanRule) {
				Text command;

				String val = ((BooleanRule) rule).get() ? "§1" : "§0";
				switch (Key.getName()) {
					case "allowCavesMap":
						command = new LiteralText(String.format("§0§0§a%s§f§f", val));
						serverCommandSource.getServer().getPlayerManager().sendToAll(
								new GameMessageS2CPacket(command, MessageType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowEntityRadar":
						command = new LiteralText(String.format("§0§0§b%s§f§f", val));
						serverCommandSource.getServer().getPlayerManager().sendToAll(
								new GameMessageS2CPacket(command, MessageType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowPlayerRadar":
						command = new LiteralText(String.format("§0§0§c%s§f§f", val));
						serverCommandSource.getServer().getPlayerManager().sendToAll(
								new GameMessageS2CPacket(command, MessageType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowCreatureRadar":
						command = new LiteralText(String.format("§0§0§d%s§f§f", val));
						serverCommandSource.getServer().getPlayerManager().sendToAll(
								new GameMessageS2CPacket(command, MessageType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowHostileRadar":
						command = new LiteralText(String.format("§0§0§e%s§f§f", val));
						serverCommandSource.getServer().getPlayerManager().sendToAll(
								new GameMessageS2CPacket(command, MessageType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowSlimeChunks":
						command = new LiteralText(String.format("§0§0§s%s§f§f", val));
						serverCommandSource.getServer().getPlayerManager().sendToAll(
								new GameMessageS2CPacket(command, MessageType.SYSTEM, Util.NIL_UUID));
						break;
					case "allowWaypointsJump":
						command = new LiteralText(String.format("§0§0§t%s§f§f", val));
						serverCommandSource.getServer().getPlayerManager().sendToAll(
								new GameMessageS2CPacket(command, MessageType.SYSTEM, Util.NIL_UUID));
						break;
				}
			}
		}
	}
}
