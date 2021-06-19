package ru.bulldog.justmap.mixins.server;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.BaseComponent;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextComponent;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.minecraft.world.level.GameRules;
import ru.bulldog.justmap.map.MapGameRules;
import ru.bulldog.justmap.network.ServerNetworkHandler;
import ru.bulldog.justmap.server.JustMapServer;
import ru.bulldog.justmap.server.config.ServerSettings;

@Mixin(PlayerList.class)
public abstract class PlayerManagerMixin {
	
	@Final
	@Shadow
	private MinecraftServer server;
	
	@Inject(method = "placeNewPlayer", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/DifficultyS2CPacket;<init>(Lnet/minecraft/world/Difficulty;Z)V"))
	public void onPlayerConnectPre(Connection connection, ServerPlayer player, CallbackInfo info) {
		ServerNetworkHandler networkHandler = JustMapServer.getNetworkHandler();
		if (networkHandler != null) {
			networkHandler.onPlayerConnect(player);
		}
	}
	
	@Inject(method = "placeNewPlayer", at = @At("TAIL"))
	public void onPlayerConnectPost(Connection clientConnection, ServerPlayer serverPlayerEntity, CallbackInfo info) {
		BaseComponent command = new TextComponent("§0§0");
		if (ServerSettings.useGameRules) {
			GameRules gameRules = server.getGameRules();
			if (gameRules.getBoolean(MapGameRules.ALLOW_CAVES_MAP)) {
				command.append("§a§1");
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_ENTITY_RADAR)) {
				command.append("§b§1");
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_PLAYER_RADAR)) {
				command.append("§c§1");
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_CREATURE_RADAR)) {
				command.append("§d§1");
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_HOSTILE_RADAR)) {
				command.append("§e§1");
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_SLIME_CHUNKS)) {
				command.append("§s§1");
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_TELEPORTATION)) {
				command.append("§t§1");
			}
		} else {
			if (ServerSettings.allowCavesMap) {
				command.append("§a§1");
			}
			if (ServerSettings.allowEntities) {
				command.append("§b§1");
			}
			if (ServerSettings.allowPlayers) {
				command.append("§c§1");
			}
			if (ServerSettings.allowCreatures) {
				command.append("§d§1");
			}
			if (ServerSettings.allowHostile) {
				command.append("§e§1");
			}
			if (ServerSettings.allowSlime) {
				command.append("§s§1");
			}
			if (ServerSettings.allowTeleportation) {
				command.append("§t§1");
			}
		}
		command.append("§f§f");
		
		if (command.getString().length() > 8) {
			this.sendCommand(serverPlayerEntity, command);
		}
	}
	
	private void sendCommand(ServerPlayer serverPlayerEntity, Component command) {
		serverPlayerEntity.connection.send(new ClientboundChatPacket(command, ChatType.SYSTEM, serverPlayerEntity.getUUID()));
	}
}
