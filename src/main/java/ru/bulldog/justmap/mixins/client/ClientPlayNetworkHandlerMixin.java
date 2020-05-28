package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.At;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.MessageType;

import ru.bulldog.justmap.map.minimap.MapGameRules;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientPlayNetworkHandlerMixin {
	
	@Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
	public void onGameMessage(GameMessageS2CPacket gameMessageS2CPacket, CallbackInfo ci) {
		if (gameMessageS2CPacket.getLocation() == MessageType.SYSTEM) {
			String pref = "§0§0", suff = "§f§f";
			String message = gameMessageS2CPacket.getMessage().getString();
			
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
}
