package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.At;

import net.fabricmc.fabric.impl.networking.PacketTypes;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.CustomPayloadS2CPacket;
import net.minecraft.network.packet.s2c.play.GameMessageS2CPacket;
import net.minecraft.network.packet.s2c.play.HealthUpdateS2CPacket;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.network.MessageType;
import net.minecraft.network.PacketByteBuf;

import ru.bulldog.justmap.map.MapGameRules;
import ru.bulldog.justmap.map.waypoint.Waypoint;

@Mixin(value = ClientPlayNetworkHandler.class, priority = 100)
public abstract class ClientPlayNetworkHandlerMixin {
	
	@Shadow
	private MinecraftClient client;
	
	@Inject(method = "onCustomPayload", at = @At("HEAD"))
	public void onCustomPayload(CustomPayloadS2CPacket packet, CallbackInfo cinfo) {
		System.out.println(packet.getChannel());
		PacketByteBuf buf = packet.getData();
		if (packet.getChannel().equals(PacketTypes.BRAND)) {
			System.out.println("Lenth: " + buf.readByte());
		}
		StringBuilder sb = new StringBuilder();
		char c;

		try {
			while (buf.readerIndex() < buf.writerIndex()) {
				c = (char) buf.readByte();

				if (c == 0) {
					String s = sb.toString();

					if (!s.isEmpty()) {
						System.out.println(s);
					}

					sb = new StringBuilder();
				} else {
					sb.append(c);
				}
			}
		} finally {
			buf.release();
		}

		String s = sb.toString();

		if (!s.isEmpty()) {
			System.out.println(s);
		}
	}
	
	@Inject(method = "onGameMessage", at = @At("HEAD"), cancellable = true)
	public void onGameMessage(GameMessageS2CPacket gameMessageS2CPacket, CallbackInfo ci) {
		if (gameMessageS2CPacket.getLocation() == MessageType.SYSTEM) {
			String pref = "§0§0", suff = "§f§f";
			String message = gameMessageS2CPacket.getMessage().getString().replaceAll("[&\\$]", "§");
			
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
	    	Identifier dimension = this.client.world.getDimensionRegistryKey().getValue();
	    	BlockPos playerPos = this.client.player.getBlockPos();
	    	Waypoint.createOnDeath(dimension, playerPos);
	    }
	}
}
