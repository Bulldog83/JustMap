package ru.bulldog.justmap.util.render;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;
import org.lwjgl.opengl.GL14;
import org.lwjgl.opengl.GL30;

public final class GLC {
	public final static int GL_ZERO = GL11.GL_ZERO; //0
	public final static int GL_ONE  = GL11.GL_ONE; //1
	public final static int GL_TRUE  = GL11.GL_TRUE; //1
	public final static int GL_LINES = GL11.GL_LINES; //1
	public final static int GL_TRIANGLES = GL11.GL_TRIANGLES; //4
	public final static int GL_TRIANGLE_FAN = GL11.GL_TRIANGLE_FAN; //6
	public final static int GL_QUADS = GL11.GL_QUADS; //7
	public final static int GL_DEPTH_BUFFER_BIT = GL11.GL_DEPTH_BUFFER_BIT; //256
	public final static int GL_ADD = GL11.GL_ADD; //260
	public final static int GL_SRC_COLOR = GL11.GL_SRC_COLOR; //768
	public final static int GL_ONE_MINUS_SRC_COLOR = GL11.GL_ONE_MINUS_SRC_COLOR; //769
	public final static int GL_SRC_ALPHA = GL11.GL_SRC_ALPHA; //770
	public final static int GL_ONE_MINUS_SRC_ALPHA = GL11.GL_ONE_MINUS_SRC_ALPHA; //771
	public final static int GL_DST_ALPHA = GL11.GL_DST_ALPHA; //772
	public final static int GL_ONE_MINUS_DST_ALPHA = GL11.GL_ONE_MINUS_DST_ALPHA; //773
	public final static int GL_COLOR_BUFFER_BIT = GL11.GL_COLOR_BUFFER_BIT; //16384
	public final static int GL_COLOR_OR_DEPTH_BUFFER_BIT = 0x4100; //16640, GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT
	public final static int GL_TEXTURE_2D = GL11.GL_TEXTURE_2D; //3553
	public final static int GL_RGBA = GL11.GL_RGBA; //6408
	public final static int GL_RGBA8 = GL11.GL_RGBA8; //32856
	public final static int GL_UNSIGNED_BYTE = GL11.GL_UNSIGNED_BYTE; //5121
	public final static int GL_TEXTURE_MAG_FILTER = GL11.GL_TEXTURE_MAG_FILTER; //10240
	public final static int GL_TEXTURE_MIN_FILTER = GL11.GL_TEXTURE_MIN_FILTER; //10241
	public final static int GL_TEXTURE_WRAP_S = GL11.GL_TEXTURE_WRAP_S; //10242
	public final static int GL_TEXTURE_WRAP_T = GL11.GL_TEXTURE_WRAP_T; //10243
	public final static int GL_CLAMP = GL11.GL_CLAMP; //10496
	public final static int GL_NEAREST = GL11.GL_NEAREST; //9728
	public final static int GL_LINEAR = GL11.GL_LINEAR; //9729
	public final static int GL_LINEAR_MIPMAP_NEAREST = GL11.GL_LINEAR_MIPMAP_NEAREST; //9985
	public final static int GL_LINEAR_MIPMAP_LINEAR = GL11.GL_LINEAR_MIPMAP_LINEAR; //9987
	public final static int GL_MODELVIEW = GL11.GL_MODELVIEW; //5888
	public final static int GL_PROJECTION = GL11.GL_PROJECTION; //5889
	public final static int GL_SCISSOR_TEST = GL11.GL_SCISSOR_TEST; //3089
	public final static int GL_UNPACK_ROW_LENGTH = GL11.GL_UNPACK_ROW_LENGTH; //3314
	public final static int GL_UNPACK_SKIP_ROWS = GL11.GL_UNPACK_SKIP_ROWS; //3315
	public final static int GL_UNPACK_SKIP_PIXELS = GL11.GL_UNPACK_SKIP_PIXELS; //3316
	public final static int GL_TEXTURE_ENV = GL11.GL_TEXTURE_ENV; //8960
	public final static int GL_TEXTURE_ENV_MODE = GL11.GL_TEXTURE_ENV_MODE; //8704
	public final static int GL_MODULATE = GL11.GL_MODULATE; //8448
	public final static int GL_UNSIGNED_INT_8_8_8_8 = GL12.GL_UNSIGNED_INT_8_8_8_8; //32821
	public final static int GL_CLAMP_TO_EDGE = GL12.GL_CLAMP_TO_EDGE; //33071
	public final static int GL_GENERATE_MIPMAP = GL14.GL_GENERATE_MIPMAP; //33169
	public final static int GL_DEPTH_COMPONENT24 = GL14.GL_DEPTH_COMPONENT24; //33190
	public final static int GL_COLOR_ATTACHMENT = GL30.GL_COLOR_ATTACHMENT0; //36064
	public final static int GL_DEPTH_ATTACHMENT = GL30.GL_DEPTH_ATTACHMENT; //36096
	public final static int GL_FRAMEBUFFER = GL30.GL_FRAMEBUFFER; //36160
	public final static int GL_RENDERBUFFER = GL30.GL_RENDERBUFFER; //36161

	public final static int GL_FRAMEBUFFER_COMPLETE = GL30.GL_FRAMEBUFFER_COMPLETE; //36053
	public final static int GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT = GL30.GL_FRAMEBUFFER_INCOMPLETE_ATTACHMENT; //36054
	public final static int GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT = GL30.GL_FRAMEBUFFER_INCOMPLETE_MISSING_ATTACHMENT; //36055
	public final static int GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER = GL30.GL_FRAMEBUFFER_INCOMPLETE_DRAW_BUFFER; //36059
	public final static int GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER = GL30.GL_FRAMEBUFFER_INCOMPLETE_READ_BUFFER; //36060
}
