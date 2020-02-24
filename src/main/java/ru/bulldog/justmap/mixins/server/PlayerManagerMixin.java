package ru.bulldog.justmap.mixins.server;

import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.network.ClientConnection;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.world.GameRules;

import ru.bulldog.justmap.minimap.MapGameRules;
import ru.bulldog.justmap.server.config.ServerParams;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
	
	@Final
	@Shadow
	private MinecraftServer server;
	
	@Inject(method = "onPlayerConnect", at = @At("RETURN"))
	public void onPlayerConnect(ClientConnection clientConnection, ServerPlayerEntity serverPlayerEntity, CallbackInfo ci) {
		if (ServerParams.useGameRules) {
			GameRules gameRules = server.getGameRules();
			
			Text command;
			if (gameRules.getBoolean(MapGameRules.ALLOW_CAVES_MAP)) {
				command = new LiteralText("§0§0§a§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_ENTITY_RADAR)) {
				command = new LiteralText("§0§0§b§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_PLAYER_RADAR)) {
				command = new LiteralText("§0§0§c§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_CREATURE_RADAR)) {
				command = new LiteralText("§0§0§d§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
			if (gameRules.getBoolean(MapGameRules.ALLOW_HOSTILE_RADAR)) {
				command = new LiteralText("§0§0§e§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
		} else {
			Text command;
			if (ServerParams.allowCavesMap) {
				command = new LiteralText("§0§0§a§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
			if (ServerParams.allowEntities) {
				command = new LiteralText("§0§0§b§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
			if (ServerParams.allowPlayers) {
				command = new LiteralText("§0§0§c§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
			if (ServerParams.allowCreatures) {
				command = new LiteralText("§0§0§d§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
			if (ServerParams.allowHostile) {
				command = new LiteralText("§0§0§e§1§f§f");
				serverPlayerEntity.sendMessage(command);
			}
		}
	}
}
