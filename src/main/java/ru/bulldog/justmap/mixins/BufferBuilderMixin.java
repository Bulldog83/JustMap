package ru.bulldog.justmap.mixins;

import java.nio.ByteBuffer;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexFormat;

@Mixin(BufferBuilder.class)
public abstract class BufferBuilderMixin {
	
	@Shadow
	private boolean building;
	@Shadow
	private ByteBuffer buffer;	
	
	@Inject(at = @At("HEAD"), method = "begin", cancellable = true)
	public void begin(int i, VertexFormat vertexFormat, CallbackInfo ci) {
		if (this.building) {
			this.restart();
		}
	}
	
	@Shadow
	public abstract void end();
	
	@Shadow
	public abstract void reset();
	
	private void restart() {
		this.end();
		this.reset();
		this.buffer.clear();
	}
}
