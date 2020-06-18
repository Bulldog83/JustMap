package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.ChatMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.MessageType;

import ru.bulldog.justmap.map.minimap.MapGameRules;
import ru.bulldog.justmap.map.waypoint.Waypoint;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	
	@Shadow
	private MinecraftClient client;
	
	@Inject(method = "onChatMessage", at = @At("HEAD"), cancellable = true)
	public void onChatMessage(ChatMessageS2CPacket chatMessageS2CPacket, CallbackInfo ci) {
		if (chatMessageS2CPacket.getLocation() == MessageType.SYSTEM) {
			String pref = "§0§0", suff = "§f§f";
			String message = chatMessageS2CPacket.getMessage().getString().replaceAll("[&\\$]", "§");
			
			if (message.contains(pref) && message.contains(suff)) {
				int start = message.indexOf(pref) + 4;
				int end = message.indexOf(suff);
				
				MapGameRules.parseCommand(message.substring(start, end));				
			
				if (message.matches("^§0§0.+§f§f$")) {
					ci.cancel();
				}
			}
		}
	}
	
	@Inject(method = "onHealthUpdate", at = @At("RETURN"))
	public void onHealthUpdate(HealthUpdateS2CPacket healthUpdateS2CPacket, CallbackInfo ci) {
		float health = healthUpdateS2CPacket.getHealth();
		if (health <= 0.0F) {
	    	int dimension = this.client.player.dimension.getRawId();
	    	BlockPos playerPos = this.client.player.getBlockPos();
	    	Waypoint.createOnDeath(dimension, playerPos);
	    }
	}
}
