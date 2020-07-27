package ru.bulldog.justmap.client.network;

import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;

public class ServerDataConsumer implements PacketConsumer {

	@Override
	public void accept(PacketContext context, PacketByteBuf buffer) {
		System.out.println(buffer);
	}

}
