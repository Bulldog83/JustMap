package ru.bulldog.justmap.client.network;

import net.fabricmc.fabric.api.network.ClientSidePacketRegistry;
import net.fabricmc.fabric.api.network.PacketConsumer;
import net.fabricmc.fabric.api.network.PacketContext;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;

import ru.bulldog.justmap.JustMap;

public final class SpigotAgent implements PacketConsumer {
	public final static ClientSidePacketRegistry PACKET_REGISTRY = ClientSidePacketRegistry.INSTANCE;
	public final static Identifier CHANNEL_ID = new Identifier(JustMap.MODID, "spigot-agent");
	public final static SpigotAgent CONSUMER = new SpigotAgent();
	
	private SpigotAgent() {}
	
	private static boolean worldChanged = false;
	
	public static void sendRequest(String request) {
		if (!worldChanged) return;
		System.out.println("Can send: " + PACKET_REGISTRY.canServerReceive(CHANNEL_ID));
		PACKET_REGISTRY.sendToServer(CHANNEL_ID, new DataRequest().getData());
		worldChanged = false;
	}
	
	public static void onWorldChanged() {
		worldChanged = true;
	}
	
	@Override
	public void accept(PacketContext context, PacketByteBuf buffer) {
		StringBuilder sb = new StringBuilder();
		char c;

		try {
			while (buffer.readerIndex() < buffer.writerIndex()) {
				c = (char) buffer.readByte();

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
			buffer.release();
		}

		String s = sb.toString();

		if (!s.isEmpty()) {
			System.out.println(s);
		}
	}
}
