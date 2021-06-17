package ru.bulldog.justmap.util.render;

import org.lwjgl.opengl.EXTFramebufferObject;

import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.GL30;
import org.lwjgl.opengl.GL;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;

public class ExtendedFramebuffer extends RenderTarget {
	private int colorAttachment;
	private int depthAttachment;
	private FboType fboType;
	
	public ExtendedFramebuffer(int width, int height, boolean useDepthIn) {
		super(useDepthIn);
		resize(width, height, Minecraft.ON_OSX);
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
			this.destroyBuffers();
		}
		this.createBuffers(width, height, isMac);
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
	}
	
	@Override
	public void createBuffers(int width, int height, boolean isMac) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.viewWidth = width;
		this.viewHeight = height;
		this.width = width;
		this.height = height;
		this.frameBufferId = this.genFrameBuffers();
		this.colorAttachment = TextureUtil.generateTextureId();
		if (useDepth) {
			this.depthAttachment = this.genRenderbuffers();
			GlStateManager._bindTexture(depthAttachment);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_NEAREST);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_NEAREST);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP);
			GlStateManager._texParameter(GLC.GL_TEXTURE_2D, 34892, 0);
			GlStateManager._texImage2D(GLC.GL_TEXTURE_2D, 0, 6402, width, height, 0, 6402, 5126, null);
		}
		this.setFilterMode(GLC.GL_NEAREST);
		GlStateManager._bindTexture(colorAttachment);
		GlStateManager._texImage2D(GLC.GL_TEXTURE_2D, 0, GLC.GL_RGBA8, width, height, 0, GLC.GL_RGBA, GLC.GL_UNSIGNED_BYTE, null);
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, frameBufferId);
		this.framebufferTexture2D(GLC.GL_FRAMEBUFFER, GLC.GL_COLOR_ATTACHMENT, GLC.GL_TEXTURE_2D, colorAttachment, 0);
		if (useDepth) {
			this.bindRenderbuffer(GLC.GL_RENDERBUFFER, depthAttachment);
			this.renderbufferStorage(GLC.GL_RENDERBUFFER, GLC.GL_DEPTH_COMPONENT24, width, height);
			this.framebufferRenderbuffer(GLC.GL_FRAMEBUFFER, GLC.GL_DEPTH_ATTACHMENT, GLC.GL_RENDERBUFFER, depthAttachment);
		}
		try {
			this.checkStatus();
			this.clear(isMac);
		} catch (Exception ignored) {}
		this.unbindRead();
	}
	
	private int genFrameBuffers() {
		int fbo = -1;
		this.fboType = FboType.NONE;
		if (GL.getCapabilities().OpenGL30) {
			fbo = GL30.glGenFramebuffers();
			this.fboType = FboType.BASE;
		}
		else if (GL.getCapabilities().GL_ARB_framebuffer_object) {
			fbo = ARBFramebufferObject.glGenFramebuffers();
			this.fboType = FboType.ARB;
		}
		else if (GL.getCapabilities().GL_EXT_framebuffer_object) {
			fbo = EXTFramebufferObject.glGenFramebuffersEXT();
			this.fboType = FboType.EXT;
		}
		return fbo;
	}
	
	public int genRenderbuffers() {
		switch (fboType) {
			case BASE -> {
				return GL30.glGenRenderbuffers();
			}
			case ARB -> {
				return ARBFramebufferObject.glGenRenderbuffers();
			}
			case EXT -> {
				return EXTFramebufferObject.glGenRenderbuffersEXT();
			}
			default -> {
				return -1;
			}
		}
	}
	
	public void destroyBuffers() {
		this.unbindRead();
		this.unbindWrite();
		if (depthAttachment > -1) {
			this.deleteRenderbuffers(depthAttachment);
			this.depthAttachment = -1;
		}
		if (colorAttachment > -1) {
			TextureUtil.releaseTextureId(colorAttachment);
			this.colorAttachment = -1;
		}
		if (frameBufferId > -1) {
			this.bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
			this.deleteFramebuffers(frameBufferId);
			this.frameBufferId = -1;
		}
	}
	
	private void deleteFramebuffers(int framebufferIn) {
		switch (fboType) {
			case BASE -> GL30.glDeleteFramebuffers(framebufferIn);
			case ARB -> ARBFramebufferObject.glDeleteFramebuffers(framebufferIn);
			case EXT -> EXTFramebufferObject.glDeleteFramebuffersEXT(framebufferIn);
		}
	}
	
	private void deleteRenderbuffers(int renderbuffer) {
		switch (fboType) {
			case BASE -> GL30.glDeleteRenderbuffers(renderbuffer);
			case ARB -> ARBFramebufferObject.glDeleteRenderbuffers(renderbuffer);
			case EXT -> EXTFramebufferObject.glDeleteRenderbuffersEXT(renderbuffer);
		}
	}
	
	@Override
	public void checkStatus() {
		int status = checkFramebufferStatus();
		switch (status) {
			case GLC.GL_FRAMEBUFFER_COMPLETE -> {}
			case GLC.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT -> throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT");
			case GLC.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT -> throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT");
			case GLC.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER -> throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER");
			case GLC.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER -> throw new RuntimeException("GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER");
			default -> throw new RuntimeException("glCheckFramebufferStatus returned unknown status: " + status);
		}
	}
	
	private int checkFramebufferStatus() {
		switch (fboType) {
			case BASE -> {
				return GL30.glCheckFramebufferStatus(GLC.GL_FRAMEBUFFER);
			}
			case ARB -> {
				return ARBFramebufferObject.glCheckFramebufferStatus(GLC.GL_FRAMEBUFFER);
			}
			case EXT -> {
				return EXTFramebufferObject.glCheckFramebufferStatusEXT(GLC.GL_FRAMEBUFFER);
			}
			default -> {
				return -1;
			}
		}
	}
	
	public void bindFramebuffer(int target, int framebufferIn) {
		switch (fboType) {
			case BASE -> GL30.glBindFramebuffer(target, framebufferIn);
			case ARB -> ARBFramebufferObject.glBindFramebuffer(target, framebufferIn);
			case EXT -> EXTFramebufferObject.glBindFramebufferEXT(target, framebufferIn);
			default -> throw new RuntimeException("bindFramebuffer: Invalid FBO type.");
		}
	}
	
	public void framebufferTexture2D(int target, int attachment, int textarget, int texture, int level) {
		switch (fboType) {
			case BASE -> GL30.glFramebufferTexture2D(target, attachment, textarget, texture, level);
			case ARB -> ARBFramebufferObject.glFramebufferTexture2D(target, attachment, textarget, texture, level);
			case EXT -> EXTFramebufferObject.glFramebufferTexture2DEXT(target, attachment, textarget, texture, level);
			default -> throw new RuntimeException("framebufferTexture2D: Invalid FBO type.");
		}
	}
	
	public void bindRenderbuffer(int target, int renderbuffer) {
		switch (fboType) {
			case BASE -> GL30.glBindRenderbuffer(target, renderbuffer);
			case ARB -> ARBFramebufferObject.glBindRenderbuffer(target, renderbuffer);
			case EXT -> EXTFramebufferObject.glBindRenderbufferEXT(target, renderbuffer);
			default -> throw new RuntimeException("bindRenderbuffer: Invalid FBO type.");
		}
	}
	
	public void renderbufferStorage(int target, int internalFormat, int width, int height) {
		switch (fboType) {
			case BASE -> GL30.glRenderbufferStorage(target, internalFormat, width, height);
			case ARB -> ARBFramebufferObject.glRenderbufferStorage(target, internalFormat, width, height);
			case EXT -> EXTFramebufferObject.glRenderbufferStorageEXT(target, internalFormat, width, height);
			default -> throw new RuntimeException("renderbufferStorage: Invalid FBO type.");
		}
	}
	
	public void framebufferRenderbuffer(int target, int attachment, int renderBufferTarget, int renderBuffer) {
		switch (fboType) {
			case BASE -> GL30.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
			case ARB -> ARBFramebufferObject.glFramebufferRenderbuffer(target, attachment, renderBufferTarget, renderBuffer);
			case EXT -> EXTFramebufferObject.glFramebufferRenderbufferEXT(target, attachment, renderBufferTarget, renderBuffer);
			default -> throw new RuntimeException("framebufferRenderbuffer: Invalid FBO type.");
		}
	}
	
	@Override
	public void bindWrite(boolean setViewport) {
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, frameBufferId);
		if (setViewport) {
			GlStateManager._viewport(0, 0, viewWidth, viewHeight);
		}
	}
	
	@Override
	public void unbindWrite() {
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
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
		this.filterMode = filter;
		GlStateManager._bindTexture(colorAttachment);
		GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, filter);
		GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, filter);
		GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP);
		GlStateManager._texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP);
		GlStateManager._bindTexture(0);
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
	}
	
	public enum FboType {
		BASE,
		ARB,
		EXT,
		NONE
	}
}
