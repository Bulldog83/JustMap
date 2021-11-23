package ru.bulldog.justmap.mixins.server;

import net.minecraft.network.ClientConnection;
import net.minecraft.network.MessageType;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.BaseText;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import ru.bulldog.justmap.map.MapGameRules;
import ru.bulldog.justmap.network.ServerNetworkHandler;
import ru.bulldog.justmap.server.JustMapServer;
import ru.bulldog.justmap.server.config.ServerSettings;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
	
	@Final
	@Shadow
	private MinecraftServer server;
	
	@Inject(method = "onPlayerConnect", at = @At(value = "INVOKE", target = "Lnet/minecraft/network/packet/s2c/play/DifficultyS2CPacket;<init>(Lnet/minecraft/world/Difficulty;Z)V"))
	public void onPlayerConnectPre(ClientConnection connection, ServerPlayerEntity player, CallbackInfo info) {
		ServerNetworkHandler networkHandler = JustMapServer.getNetworkHandler();
		if (networkHandler != null) {
			networkHandler.onPlayerConnect(player);
		}
	}
	
	@Inject(method = "onPlayerConnect", at = @At("TAIL"))
	public void onPlayerConnectPost(ClientConnection clientConnection, ServerPlayerEntity serverPlayerEntity, CallbackInfo info) {
		BaseText command = new LiteralText("§0§0");
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
	
	private void sendCommand(ServerPlayerEntity serverPlayerEntity, Text command) {
		serverPlayerEntity.networkHandler.sendPacket(new GameMessageS2CPacket(command, MessageType.SYSTEM, serverPlayerEntity.getUuid()));
	}
}
