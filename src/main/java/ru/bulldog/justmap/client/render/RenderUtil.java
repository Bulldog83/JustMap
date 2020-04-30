package ru.bulldog.justmap.client.render;

import java.nio.ByteBuffer;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.ARBFramebufferObject;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.client.JustMapClient;

public class RenderUtil {

	private static final boolean fboEnabledArb = GL.getCapabilities().GL_ARB_framebuffer_object;
	private static final boolean fboEnabledBase = GL.getCapabilities().OpenGL30;
	
	private static int previousFBOID = 0;
	private static int previousRBOID = 0;
	
	public static int fboID = 0;
	public static int rboID = 0;
	public static int fboTextureID = 0;
	public static boolean hasAlphaBits = GL11.glGetInteger(GL11.GL_ALPHA_BITS) > 0;

	public static void setupFrameBuffer() {
		if (fboEnabledBase) {
			setupFrameBufferBASE();
		} else if (fboEnabledArb) {
			setupFrameBufferARB();
		} else {
			setupFrameBufferEXT();
		}
	}

	private static void setupFrameBufferBASE() {
		previousFBOID = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		fboID = GL30.glGenFramebuffers();
		fboTextureID = GL11.glGenTextures();
		
		int width = JustMapClient.MAP.getWidth();
		int height = JustMapClient.MAP.getHeight();
		
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);
		
		RenderSystem.bindTexture(fboTextureID);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, width, height, 0, GL11.GL_RGBA, GL11.GL_BYTE, byteBuffer);
		
		GL30.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, fboTextureID, GL11.GL_ZERO);

		rboID = GL30.glGenRenderbuffers();
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboID);
		GL30.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
		GL30.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rboID);
		GL30.glBindRenderbuffer(GL30.GL_RENDERBUFFER, 0);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFBOID);
	}

	private static void setupFrameBufferARB() {
		previousFBOID = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		fboID = ARBFramebufferObject.glGenFramebuffers();
		fboTextureID = GL11.glGenTextures();
		
		int width = JustMapClient.MAP.getWidth();
		int height = JustMapClient.MAP.getHeight();
		
		ARBFramebufferObject.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
		ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);

		RenderSystem.bindTexture(fboTextureID);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, GL11.GL_ZERO, GL11.GL_RGBA, width, height, GL11.GL_ZERO, GL11.GL_RGBA, GL11.GL_BYTE, byteBuffer);
		
		ARBFramebufferObject.glFramebufferTexture2D(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, fboTextureID, GL11.GL_ZERO);

		rboID = ARBFramebufferObject.glGenRenderbuffers();
		ARBFramebufferObject.glBindRenderbuffer(GL30.GL_RENDERBUFFER, rboID);
		ARBFramebufferObject.glRenderbufferStorage(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
		ARBFramebufferObject.glFramebufferRenderbuffer(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rboID);
		ARBFramebufferObject.glBindRenderbuffer(GL30.GL_RENDERBUFFER, GL11.GL_ZERO);
		ARBFramebufferObject.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFBOID);
	}

	private static void setupFrameBufferEXT() {
		previousFBOID = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		fboID = EXTFramebufferObject.glGenFramebuffersEXT();
		fboTextureID = GL11.glGenTextures();
		
		int width = JustMapClient.MAP.getWidth();
		int height = JustMapClient.MAP.getHeight();
		
		EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, fboID);
		ByteBuffer byteBuffer = BufferUtils.createByteBuffer(4 * width * height);

		RenderSystem.bindTexture(fboTextureID);
		
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexImage2D(GL11.GL_TEXTURE_2D, GL11.GL_ZERO, GL11.GL_RGBA, width, height, GL11.GL_ZERO, GL11.GL_RGBA, GL11.GL_BYTE, byteBuffer);
		
		EXTFramebufferObject.glFramebufferTexture2DEXT(GL30.GL_FRAMEBUFFER, GL30.GL_COLOR_ATTACHMENT0, GL11.GL_TEXTURE_2D, fboTextureID, GL11.GL_ZERO);

		rboID = EXTFramebufferObject.glGenRenderbuffersEXT();
		EXTFramebufferObject.glBindRenderbufferEXT(GL30.GL_RENDERBUFFER, rboID);
		EXTFramebufferObject.glRenderbufferStorageEXT(GL30.GL_RENDERBUFFER, GL14.GL_DEPTH_COMPONENT24, width, height);
		EXTFramebufferObject.glFramebufferRenderbufferEXT(GL30.GL_FRAMEBUFFER, GL30.GL_DEPTH_ATTACHMENT, GL30.GL_RENDERBUFFER, rboID);
		EXTFramebufferObject.glBindRenderbufferEXT(GL30.GL_RENDERBUFFER, GL11.GL_ZERO);
		EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, previousFBOID);
	}

	public static void bindFrameBuffer() {
		if (fboEnabledBase) {
			bindFrameBufferBASE();
		} else if (fboEnabledArb) {
			bindFrameBufferARB();
		} else {
			bindFrameBufferEXT();
		}
	}

	private static void bindFrameBufferBASE() {
		previousFBOID = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
	}

	private static void bindFrameBufferARB() {
		previousFBOID = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		ARBFramebufferObject.glBindFramebuffer(GL30.GL_FRAMEBUFFER, fboID);
	}

	private static void bindFrameBufferEXT() {
		previousFBOID = GL11.glGetInteger(GL30.GL_FRAMEBUFFER_BINDING);
		EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, fboID);
	}

	public static void unbindFrameBuffer() {
		if (fboEnabledBase) {
			unbindFrameBufferBASE();
		} else if (fboEnabledArb) {
			unbindFrameBufferARB();
		} else {
			unbindFrameBufferEXT();
		}
	}

	private static void unbindFrameBufferBASE() {
		GL30.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFBOID);
	}

	private static void unbindFrameBufferARB() {
		ARBFramebufferObject.glBindFramebuffer(GL30.GL_FRAMEBUFFER, previousFBOID);
	}

	private static void unbindFrameBufferEXT() {
		EXTFramebufferObject.glBindFramebufferEXT(GL30.GL_FRAMEBUFFER, previousFBOID);
	}
}
