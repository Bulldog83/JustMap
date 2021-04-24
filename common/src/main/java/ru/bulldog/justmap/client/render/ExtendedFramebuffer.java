package ru.bulldog.justmap.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.TextureUtil;
import org.lwjgl.opengl.EXTFramebufferObject;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.Minecraft;

public class ExtendedFramebuffer extends RenderTarget {
	private int colorAttachment;
	private int depthAttachment;
	private FboType fboType;
	
	public ExtendedFramebuffer(int width, int height, boolean useDepthIn) {
		super(width, height, useDepthIn, Minecraft.ON_OSX);
	}

	public static boolean canUseFramebuffer() {
        return GL.getCapabilities().OpenGL14 && (
        		GL.getCapabilities().GL_ARB_framebuffer_object ||
        	    GL.getCapabilities().GL_EXT_framebuffer_object ||
        	    GL.getCapabilities().OpenGL30);
    }
	
	@Override
	public void resize(int width, int height, boolean isMac) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		GlStateManager._enableDepthTest();
		if (frameBufferId >= 0) {
			delete();
		}
		createBuffers(width, height, isMac);
		bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
	}
	
	@Override
	public void createBuffers(int width, int height, boolean isMac) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.viewWidth = width;
		this.viewHeight = height;
		this.width = width;
		this.height = height;
		this.frameBufferId = genFrameBuffers();
		this.colorAttachment = TextureUtil.generateTextureId();
		if (useDepth) {
			depthAttachment = genRenderbuffers();
			GlStateManager._bindTexture(depthAttachment);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_NEAREST);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_NEAREST);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, 34892, 0);
			GlStateManager._texImage2D(GLC.GL_TEXTURE_2D, 0, 6402, width, height, 0, 6402, 5126, null);
		}
		setFilterMode(GLC.GL_NEAREST);
		GlStateManager._bindTexture(colorAttachment);
		GlStateManager._texImage2D(GLC.GL_TEXTURE_2D, 0, GLC.GL_RGBA8, width, height, 0, GLC.GL_RGBA, GLC.GL_UNSIGNED_BYTE, null);
		bindFramebuffer(GLC.GL_FRAMEBUFFER, frameBufferId);
		framebufferTexture2D(GLC.GL_FRAMEBUFFER, GLC.GL_COLOR_ATTACHMENT, GLC.GL_TEXTURE_2D, colorAttachment, 0);
		if (useDepth) {
			bindRenderbuffer(GLC.GL_RENDERBUFFER, depthAttachment);
			renderBufferStorage(GLC.GL_RENDERBUFFER, GLC.GL_DEPTH_COMPONENT24, width, height);
			framebufferRenderbuffer(GLC.GL_FRAMEBUFFER, GLC.GL_DEPTH_ATTACHMENT, GLC.GL_RENDERBUFFER, depthAttachment);
		}
		try {
			checkStatus();
			clear(isMac);
		} catch (Exception ignored) {}
		unbindRead();
	}
	
	private int genFrameBuffers() {
		int frameBufferId = -1;
		fboType = FboType.NONE;
		if (GL.getCapabilities().OpenGL30) {
			frameBufferId = GL30.glGenFramebuffers();
			fboType = FboType.BASE;
		}
		else if (GL.getCapabilities().GL_ARB_framebuffer_object) {
			frameBufferId = ARBFramebufferObject.glGenFramebuffers();
			fboType = FboType.ARB;
		}
		else if (GL.getCapabilities().GL_EXT_framebuffer_object) {
			frameBufferId = EXTFramebufferObject.glGenFramebuffersEXT();
			fboType = FboType.EXT;
		}
		return frameBufferId;
	}
	
	public int genRenderbuffers() {
		switch (fboType) {
			case BASE: {
				return GL30.glGenRenderbuffers();
			}
			case ARB: {
				return ARBFramebufferObject.glGenRenderbuffers();
			}
			case EXT: {
				return EXTFramebufferObject.glGenRenderbuffersEXT();
			}
			default: {
				return -1;
			}
		}
	}
	
	public void delete() {
		unbindRead();
		unbindWrite();
		if (depthAttachment > -1) {
			deleteRenderbuffers(depthAttachment);
			depthAttachment = -1;
		}
		if (colorAttachment > -1) {
			TextureUtil.releaseTextureId(colorAttachment);
			colorAttachment = -1;
		}
		if (frameBufferId > -1) {
			bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
			deleteFramebuffers(frameBufferId);
			frameBufferId = -1;
		}
	}
	
	private void deleteFramebuffers(int framebufferIn) {
		switch (fboType) {
			case BASE: {
				GL30.glDeleteFramebuffers(framebufferIn);
				break;
			}
			case ARB: {
				ARBFramebufferObject.glDeleteFramebuffers(framebufferIn);
				break;
			}
			case EXT: {
				EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferIn);
				break;
			}
			default: {}
		}
	}
	
	private void deleteRenderbuffers(int renderbuffer) {
		switch (fboType) {
			case BASE: {
				GL30.glDeleteRenderbuffers(renderbuffer);
				break;
			}
			case ARB: {
				ARBFramebufferObject.glDeleteRenderbuffers(renderbuffer);
				break;
			}
			case EXT: {
				EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffer);
				break;
			}
			default: {}
		}
	}
	
	@Override
	public void checkStatus() {
		int status = checkFramebufferStatus(GLC.GL_FRAMEBUFFER);
		switch (status) {
			case GLC.GL_FRAMEBUFFER_COMPLETE: {
				return;
			}
			case GLC.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT: {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			}
			case GLC.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT: {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			}
			case GLC.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER: {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
			}
			case GLC.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER: {
				throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
			}
			default: {
				throw new RuntimeException("glCheckFramebufferStatus returned unknown status: " + status);
			}
		}
	}
	
	private int checkFramebufferStatus(int target) {
		switch (fboType) {
			case BASE: {
				return GL30.glCheckFramebufferStatus(target);
			}
			case ARB: {
				return ARBFramebufferObject.glCheckFramebufferStatus(target);
			}
			case EXT: {
				return EXTFramebufferObject.glCheckFramebufferStatusEXT(target);
			}
			default: {
				return -1;
			}
		}
	}
	
	public void bindFramebuffer(int target, int framebufferIn) {
		switch (fboType) {
			case BASE: {
				GL30.glBindFramebuffer(target, framebufferIn);
				break;
			}
			case ARB: {
				ARBFramebufferObject.glBindFramebuffer(target, framebufferIn);
				break;
			}
			case EXT: {
				EXTFramebufferObject.glBindFramebufferEXT(target, framebufferIn);
				break;
			}
			default: {
				throw new RuntimeException("bindFramebuffer: Invalid FBO type.");
			}
		}
	}
	
	public void framebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		switch (fboType) {
			case BASE: {
				GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
				break;
			}
			case ARB: {
				ARBFramebufferObject.glFramebufferTexture2D(target, attachment, textarget, texture, level);
				break;
			}
			case EXT: {
				EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
				break;
			}
			default: {
				throw new RuntimeException("framebufferTexture2D: Invalid FBO type.");
			}
		}
	}
	
	public void bindRenderbuffer(int target, int renderbuffer) {
		switch (fboType) {
			case BASE: {
				GL30.glBindRenderbuffer(target, renderbuffer);
				break;
			}
			case ARB: {
				ARBFramebufferObject.glBindRenderbuffer(target, renderbuffer);
				break;
			}
			case EXT: {
				EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
				break;
			}
			default: {
				throw new RuntimeException("bindRenderbuffer: Invalid FBO type.");
			}
		}
	}
	
	public void renderBufferStorage(int target, int internalFormat, int width, int height) {
		switch (fboType) {
			case BASE: {
				GL30.glRenderbufferStorage(target, internalFormat, width, height);
				break;
			}
			case ARB: {
				ARBFramebufferObject.glRenderbufferStorage(target, internalFormat, width, height);
				break;
			}
			case EXT: {
				EXTFramebufferObject.glRenderbufferStorageEXT(target, internalFormat, width, height);
				break;
			}
			default: {
				throw new RuntimeException("renderbufferStorage: Invalid FBO type.");
			}
		}
	}
	
	public void framebufferRenderbuffer(int target, int attachment, int renderBufferTarget, int renderBuffer) {
		switch (fboType) {
			case BASE: {
				GL30.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
				break;
			}
			case ARB: {
				ARBFramebufferObject.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
				break;
			}
			case EXT: {
				EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderBufferTarget, renderBuffer);
				break;
			}
			default: {
				throw new RuntimeException("framebufferRenderbuffer: Invalid FBO type.");
			}
		}
	}
	
	@Override
	public void bindWrite(boolean setViewport) {
		bindFramebuffer(GLC.GL_FRAMEBUFFER, frameBufferId);
		if (setViewport) {
			GlStateManager._viewport(0, 0, viewWidth, viewHeight);
		}
	}
	
	@Override
	public void unbindWrite() {
		bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
	}
	
	@Override
	public void bindRead() {
		GlStateManager._bindTexture(colorAttachment);
	}
	
	@Override
	public void unbindRead() {
		GlStateManager._bindTexture(0);
	}
	
	@Override
	public void setFilterMode(int filter) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		filterMode = filter;
		GlStateManager._bindTexture(colorAttachment);
		GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, filter);
		GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, filter);
		GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP);
		GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP);
		GlStateManager._bindTexture(0);
		bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
	}
	
	public enum FboType {
		BASE,
		ARB,
		EXT,
		NONE;
	}
}
