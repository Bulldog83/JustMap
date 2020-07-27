package ru.bulldog.justmap.client.network;

import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketByteBuf;

public class DataRequest {

	private final char id;
	private final byte index;
	private final PacketByteBuf byteBuff;
	
	public DataRequest(char id, byte index) {
		this.byteBuff = new PacketByteBuf(Unpooled.buffer());
		this.id = id;
		this.index = index;
	}
	
	public PacketByteBuf getBuffer() {
		this.byteBuff.writeByte(index & 0xFF);
		this.byteBuff.writeChar((int) id);
		return this.byteBuff;
	}
}
