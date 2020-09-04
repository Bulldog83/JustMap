package ru.bulldog.justmap.util.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public final class GLC {
	public final static int GL_DEPTH_BUFFER_BIT = GL11.GL_DEPTH_BUFFER_BIT; //256
	public final static int GL_COLOR_BUFFER_BIT = GL11.GL_COLOR_BUFFER_BIT; //16384
	public final static int GL_COLOR_OR_DEPTH_BUFFER_BIT = 0x4100; //16640, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT
	public final static int GL_TEXTURE_2D = GL11.GL_TEXTURE_2D; //3553
	public final static int GL_RGBA8 = GL11.GL_RGBA8; //32856
	public final static int GL_RGBA = GL11.GL_RGBA; //6408
	public final static int GL_UNSIGNED_BYTE = GL11.GL_UNSIGNED_BYTE; //5121
	public final static int GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER; //36160
	public final static int GL_COLOR_ATTACHMENT0 = GL30.GL_COLOR_ATTACHMENT0; //36064
	public final static int GL_RENDERBUFFER = GL30.GL_RENDERBUFFER; //36161
	public final static int GL_DEPTH_COMPONENT24 = GL14.GL_DEPTH_COMPONENT24; //33190
	public final static int GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT; //36096
	public final static int GL_TEXTURE_MAG_FILTER = GL11.GL_TEXTURE_MAG_FILTER; //10240
	public final static int GL_TEXTURE_MIN_FILTER = GL11.GL_TEXTURE_MIN_FILTER; //10241
	public final static int GL_TEXTURE_WRAP_S = GL11.GL_TEXTURE_WRAP_S; //10242
	public final static int GL_TEXTURE_WRAP_T = GL11.GL_TEXTURE_WRAP_T; //10243
	public final static int GL_CLAMP = GL11.GL_CLAMP; //10496
	public final static int GL_ZERO = GL11.GL_ZERO; //0
	public final static int GL_ONE  = GL11.GL_ONE; //1
	public final static int GL_SRC_COLOR = GL11.GL_SRC_COLOR; //768
	public final static int GL_ONE_MINUS_SRC_COLOR = GL11.GL_ONE_MINUS_SRC_COLOR; //769
	public final static int GL_SRC_ALPHA = GL11.GL_SRC_ALPHA; //770
	public final static int GL_ONE_MINUS_SRC_ALPHA = GL11.GL_ONE_MINUS_SRC_ALPHA; //771
	public final static int GL_DST_ALPHA = GL11.GL_DST_ALPHA; //772
	public final static int GL_ONE_MINUS_DST_ALPHA = GL11.GL_ONE_MINUS_DST_ALPHA; //773
	public final static int GL_NEAREST = GL11.GL_NEAREST; //9728
	public final static int GL_MODELVIEW = GL11.GL_MODELVIEW; //5888
	public final static int GL_PROJECTION = GL11.GL_PROJECTION; //5889
	
	public final static int GL_FRAMEBUFFER_COMPLETE = GL30.GL_FRAMEBUFFER_COMPLETE; //36053
	public final static int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT; //36054
	public final static int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT; //36055
	public final static int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER; //36059
	public final static int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER; //36060
}
