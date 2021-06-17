package ru.bulldog.justmap.mixins.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.mojang.authlib.GameProfile;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.core.BlockPos;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.ChatType;
import net.minecraft.network.protocol.game.ClientboundChatPacket;
import net.minecraft.network.protocol.game.ClientboundSetDefaultSpawnPositionPacket;
import net.minecraft.network.protocol.game.ClientboundSetHealthPacket;
import org.spongepowered.asm.mixin.injection.At;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.MapGameRules;
import ru.bulldog.justmap.map.data.WorldKey;
import ru.bulldog.justmap.map.data.WorldManager;
import ru.bulldog.justmap.map.waypoint.Waypoint;

@Mixin(value = ClientPacketListener.class, priority = 100)
public abstract class ClientPlayNetworkHandlerMixin {
	
	@Shadow
	private Minecraft minecraft;
	
	@Inject(method = "<init>", at = @At("TAIL"))
	public void onConnect(Minecraft client, Screen screen, Connection connection, GameProfile profile, CallbackInfo cinfo) {
		WorldManager.load();
	}
	
	@Inject(method = "handleSetSpawn", at = @At("TAIL"))
	public void onPlayerSpawnPosition(ClientboundSetDefaultSpawnPositionPacket packet, CallbackInfo cinfo) {
		JustMap.LOGGER.debug("World spawn position set to {}", packet.getPos().toShortString());
		WorldManager.onWorldPosChanged(packet.getPos());
	}
	
	@Inject(method = "handleChat", at = @At("HEAD"), cancellable = true)
	public void onGameMessage(ClientboundChatPacket gameMessageS2CPacket, CallbackInfo cinfo) {
		if (gameMessageS2CPacket.getType() == ChatType.SYSTEM) {
			String pref = "§0§0", suff = "§f§f";
			String message = gameMessageS2CPacket.getMessage().getString().replaceAll("[&\\$]", "§");
			
			if (message.contains(pref) && message.contains(suff)) {
				int start = message.indexOf(pref) + 4;
				int end = message.indexOf(suff);
				
				MapGameRules.parseCommand(message.substring(start, end));				
			
				if (message.matches("^§0§0.+§f§f$")) {
					cinfo.cancel();
				}
			}
		}
	}
	
	@Inject(method = "handleSetHealth", at = @At("TAIL"))
	public void onHealthUpdate(ClientboundSetHealthPacket healthUpdateS2CPacket, CallbackInfo cinfo) {
		float health = healthUpdateS2CPacket.getHealth();
		if (health <= 0.0F) {
	    	WorldKey world = WorldManager.getWorldKey();
	    	BlockPos playerPos = this.minecraft.player.blockPosition();
	    	Waypoint.createOnDeath(world, playerPos);
	    }
	}
}
