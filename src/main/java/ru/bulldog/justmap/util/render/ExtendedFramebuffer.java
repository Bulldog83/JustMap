package ru.bulldog.justmap.util.render;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.platform.TextureUtil;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL30;

public class ExtendedFramebuffer extends Framebuffer {
	private int colorAttachment;
	private int depthAttachment;
	private FboType fboType;
	
	public ExtendedFramebuffer(boolean useDepthIn) {
		super(useDepthIn);
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
		RenderSystem.enableDepthTest();
		if (fbo >= 0) {
			this.delete();
		}
		this.initFbo(width, height, isMac);
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
	}
	
	@Override
	public void initFbo(int width, int height, boolean isMac) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.viewportWidth = width;
		this.viewportHeight = height;
		this.textureWidth = width;
		this.textureHeight = height;
		this.fbo = this.genFrameBuffers();
		this.colorAttachment = TextureUtil.generateTextureId();
		if (useDepthAttachment) {
			this.depthAttachment = this.genRenderbuffers();
			RenderSystem.bindTexture(depthAttachment);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, GLC.GL_NEAREST);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, GLC.GL_NEAREST);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP);
			RenderSystem.texParameter(GLC.GL_TEXTURE_2D, 34892, 0);
			GlStateManager._texImage2D(GLC.GL_TEXTURE_2D, 0, 6402, textureWidth, textureHeight, 0, 6402, 5126, null);
		}
		this.setTexFilter(GLC.GL_NEAREST);
		RenderSystem.bindTexture(colorAttachment);
		GlStateManager._texImage2D(GLC.GL_TEXTURE_2D, 0, GLC.GL_RGBA8, textureWidth, textureHeight, 0, GLC.GL_RGBA, GLC.GL_UNSIGNED_BYTE, null);
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, fbo);
		this.framebufferTexture2D(GLC.GL_FRAMEBUFFER, GLC.GL_COLOR_ATTACHMENT, GLC.GL_TEXTURE_2D, colorAttachment, 0);
		if (useDepthAttachment) {
			this.bindRenderbuffer(GLC.GL_RENDERBUFFER, depthAttachment);
			this.renderbufferStorage(GLC.GL_RENDERBUFFER, GLC.GL_DEPTH_COMPONENT24, textureWidth, textureHeight);
			this.framebufferRenderbuffer(GLC.GL_FRAMEBUFFER, GLC.GL_DEPTH_ATTACHMENT, GLC.GL_RENDERBUFFER, depthAttachment);
		}
		try {
			this.checkFramebufferStatus();
			this.clear(isMac);
		} catch (Exception ex) {
			// ignore
		}
		this.endRead();
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
		this.endRead();
		this.endWrite();
		if (depthAttachment > -1) {
			this.deleteRenderbuffers(depthAttachment);
			this.depthAttachment = -1;
		}
		if (colorAttachment > -1) {
			TextureUtil.releaseTextureId(colorAttachment);
			this.colorAttachment = -1;
		}
		if (fbo > -1) {
			this.bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
			this.deleteFramebuffers(fbo);
			this.fbo = -1;
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
	public void checkFramebufferStatus() {
		int status = this.checkFramebufferStatus(GLC.GL_FRAMEBUFFER);
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
	
	public void renderbufferStorage(int target, int internalFormat, int width, int height) {
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
	public void beginWrite(boolean setViewport) {
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, fbo);
		if (setViewport) {
			RenderSystem.viewport(0, 0, viewportWidth, viewportHeight);
		}
	}
	
	@Override
	public void endWrite() {
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
	}

	// yarn mapping missing for beginRead
	@Override
	public void method_35610() {
		RenderSystem.bindTexture(colorAttachment);
	}
	
	@Override
	public void endRead() {
		RenderSystem.bindTexture(0);
	}
	
	@Override
	public void setTexFilter(int filter) {
		RenderSystem.assertThread(RenderSystem::isOnRenderThreadOrInit);
		this.texFilter = filter;
		RenderSystem.bindTexture(colorAttachment);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MIN_FILTER, filter);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_MAG_FILTER, filter);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_S, GLC.GL_CLAMP);
		RenderSystem.texParameter(GLC.GL_TEXTURE_2D, GLC.GL_TEXTURE_WRAP_T, GLC.GL_CLAMP);
		RenderSystem.bindTexture(0);
		this.bindFramebuffer(GLC.GL_FRAMEBUFFER, 0);
	}
	
	public enum FboType {
		BASE,
		ARB,
		EXT,
		NONE
    }
}
