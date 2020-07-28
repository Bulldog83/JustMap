package ru.bulldog.justmap.client.network;

import io.netty.buffer.Unpooled;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.network.PacketByteBuf;

public class DataRequest {

	private final PacketByteBuf data;
	
	public DataRequest() {
		this.data = new PacketByteBuf(Unpooled.buffer());		
		this.data.writeByte((byte) 2 & 0xFF);
		this.data.writeChar((int) 'H');
	}
	
	@Environment(EnvType.CLIENT)
	public PacketByteBuf getData() {
		return new PacketByteBuf(data.copy());
	}
}
