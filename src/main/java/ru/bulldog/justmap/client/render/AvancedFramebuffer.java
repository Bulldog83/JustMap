package ru.bulldog.justmap.client.render;

import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;

import com.mojang.blaze3d.platform.GlStateManager;

import net.minecraft.client.texture.TextureUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;

public class AvancedFramebuffer extends Framebuffer {
	private int type;
	private int colorAttachment;
	private int depthAttachment;
	
	public AvancedFramebuffer(int width, int height, boolean useDepthIn) {
		super(width, height, useDepthIn, MinecraftClient.IS_SYSTEM_MAC);
	}
	
	@Override
	public void resize(int width, int height, boolean isMac) {
		GlStateManager.enableDepthTest();
		if (fbo >= 0) {
			this.delete();
		}
		this.initFbo(width, height, isMac);
		this.checkFramebufferStatus();
		bindFramebuffer(type, GL30.GL_FRAMEBUFFER, 0);
	}
	
	@Override
	public void initFbo(int width, int height, boolean isMac) {
		this.viewportWidth = width;
		this.viewportHeight = height;
		this.textureWidth = width;
		this.textureHeight = height;
		this.fbo = this.genFrameBuffers();
		if (fbo == -1) {
			this.clear(isMac);
			return;
		}
		this.colorAttachment = TextureUtil.generateId();
		if (colorAttachment == -1) {
			this.clear(isMac);
			return;
		}
		if (useDepthAttachment) {
			this.depthAttachment = this.genRenderbuffers();
			if (depthAttachment == -1) {
				this.clear(isMac);
				return;
			}
		}
		this.setTexFilter(GL11.GL_NEAREST);
		GlStateManager.bindTexture(colorAttachment);
		GlStateManager.texImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA8, textureWidth, textureHeight, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, null);
		bindFramebuffer(type, GL30.GL_FRAMEBUFFER, fbo);
		framebufferTexture2D(type, GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, colorAttachment, 0);
		if (useDepthAttachment) {
			bindRenderbuffer(type, GL30.GL_RENDERBUFFER, depthAttachment);
			renderbufferStorage(type, GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, textureWidth, textureHeight);
			framebufferRenderbuffer(type, GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, depthAttachment);
		}
		this.clear(isMac);
		this.endRead();
	}
	
	private int genFrameBuffers() {
		int fbo = -1;
		this.type = -1;
		if (GL.getCapabilities().OpenGL30) {
			fbo = GL30.glGenFramebuffers();
			this.type = 0;
		}
		else if (GL.getCapabilities().GL_ARB_framebuffer_object) {
			fbo = ARBFramebufferObject.glGenFramebuffers();
			this.type = 1;
		}
		else if (GL.getCapabilities().GL_EXT_framebuffer_object) {
			fbo = EXTFramebufferObject.glGenFramebuffersEXT();
			this.type = 2;
		}
		return fbo;
	}
	
	public int genRenderbuffers() {
		int rbo = -1;
		switch (type) {
			case 0: {
				rbo = GL30.glGenRenderbuffers();
				break;
			}
			case 1: {
				rbo = ARBFramebufferObject.glGenRenderbuffers();
				break;
			}
			case 2: {
				rbo = EXTFramebufferObject.glGenRenderbuffersEXT();
				break;
			}
		}
		return rbo;
	}
	
	public void delete() {
		this.endRead();
		this.endWrite();
		if (depthAttachment > -1) {
			this.deleteRenderbuffers(depthAttachment);
			this.depthAttachment = -1;
		}
		if (colorAttachment > -1) {
			TextureUtil.deleteId(colorAttachment);
			this.colorAttachment = -1;
		}
		if (fbo > -1) {
			bindFramebuffer(type, GL30.GL_FRAMEBUFFER, 0);
			this.deleteFramebuffers(fbo);
			this.fbo = -1;
		}
	}
	
	private void deleteFramebuffers(int framebufferIn) {
		switch (type) {
			case 0: {
				GL30.glDeleteFramebuffers(framebufferIn);
				break;
			}
			case 1: {
				ARBFramebufferObject.glDeleteFramebuffers(framebufferIn);
				break;
			}
			case 2: {
				EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferIn);
				break;
			}
		}
	}
	
	private void deleteRenderbuffers(int renderbuffer) {
		switch (type) {
			case 0: {
				GL30.glDeleteRenderbuffers(renderbuffer);
				break;
			}
			case 1: {
				ARBFramebufferObject.glDeleteRenderbuffers(renderbuffer);
				break;
			}
			case 2: {
				EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffer);
				break;
			}
		}
	}
	
	public void checkFramebufferStatus() {
		int i = this.checkFramebufferStatus(GL30.GL_FRAMEBUFFER);
		if (i == GL30.GL_FRAMEBUFFER_COMPLETE) return;
		
		if (i == GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT) {
			throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
		}
		if (i == GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT) {
			throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
		}
		if (i == GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER) {
			throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
		}
		if (i == GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER) {
			throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
		}
		
		throw new RuntimeException("glCheckFramebufferStatus returned unknown status:" + i);
	}
	
	private int checkFramebufferStatus(int target) {
		switch (type) {
			case 0: {
				return GL30.glCheckFramebufferStatus(target);
			}
			case 1: {
				return ARBFramebufferObject.glCheckFramebufferStatus(target);
			}
			case 2: {
				return EXTFramebufferObject.glCheckFramebufferStatusEXT(target);
			}
			default: {
				return -1;
			}
		}
	}
	
	public static void bindFramebuffer(int type, int target, int framebufferIn) {
		switch (type) {
			case 0: {
				GL30.glBindFramebuffer(target, framebufferIn);
				break;
			}
			case 1: {
				ARBFramebufferObject.glBindFramebuffer(target, framebufferIn);
				break;
			}
			case 2: {
				EXTFramebufferObject.glBindFramebufferEXT(target, framebufferIn);
				break;
			}
		}
	}
	
	public static void framebufferTexture2D(int type, int target, int attachment, int textarget, int texture, int level) {
		switch (type) {
			case 0: {
				GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
				break;
			}
			case 1: {
				ARBFramebufferObject.glFramebufferTexture2D(target, attachment, textarget, texture, level);
				break;
			}
			case 2: {
				EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
				break;
			}
		}
	}
	
	public static void bindRenderbuffer(int type, int target, int renderbuffer) {
		switch (type) {
			case 0: {
				GL30.glBindRenderbuffer(target, renderbuffer);
				break;
			}
			case 1: {
				ARBFramebufferObject.glBindRenderbuffer(target, renderbuffer);
				break;
			}
			case 2: {
				EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
				break;
			}
		}
	}
	
	public static void renderbufferStorage(int type, int target, int internalFormat, int width, int height) {
		switch (type) {
			case 0: {
				GL30.glRenderbufferStorage(target, internalFormat, width, height);
				break;
			}
			case 1: {
				ARBFramebufferObject.glRenderbufferStorage(target, internalFormat, width, height);
				break;
			}
			case 2: {
				EXTFramebufferObject.glRenderbufferStorageEXT(target, internalFormat, width, height);
				break;
			}
		}
	}
	
	public static void framebufferRenderbuffer(int type, int target, int attachment, int renderBufferTarget, int renderBuffer) {
		switch (type) {
			case 0: {
				GL30.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
				break;
			}
			case 1: {
				ARBFramebufferObject.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
				break;
			}
			case 2: {
				EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderBufferTarget, renderBuffer);
				break;
			}
		}
	}
	
	@Override
	public void beginWrite(boolean setViewport) {
		bindFramebuffer(type, GL30.GL_FRAMEBUFFER, fbo);
		if (setViewport) {
			GlStateManager.viewport(0, 0, viewportWidth, viewportHeight);
		}
	}
	
	public void endWrite() {
		bindFramebuffer(type, GL30.GL_FRAMEBUFFER, 0);
	}
	
	public void beginRead() {
		GlStateManager.bindTexture(colorAttachment);
	}
	
	public void endRead() {
		GlStateManager.bindTexture(0);
	}
	
	public void setTexFilter(int framebufferFilterIn) {
		this.texFilter = framebufferFilterIn;
		GlStateManager.bindTexture(colorAttachment);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, framebufferFilterIn);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, framebufferFilterIn);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GlStateManager.texParameter(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GlStateManager.bindTexture(0);
	}
}
