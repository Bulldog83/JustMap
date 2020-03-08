package ru.bulldog.justmap.minimap;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.Window;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.math.Line;

public class ChunkGrid {
	
	private final int color = 0x66333333;
	
	private final List<GridLine> lines;
	
	public ChunkGrid(int x, int y, int mx, int my, int mw, int mh) {
		lines = new ArrayList<>();
		
		float scale = ClientParams.mapScale;
		
		int xOff = (int) ((((x / 16) * 16) - x) / scale);
		int yOff = (int) ((((y / 16) * 16) - y) / scale);
		
		int sw = mx + mw;
		int sh = my + mh;
		
		GridLine line;
		int step = (int) (16 / scale);
		for (int cH = yOff; cH < mh; cH += step) {
			int yp = my + cH;
			if (yp < my || yp > my + mh) {
				continue;
			}
			
			line = new GridLine(mx, yp, sw, yp);
			lines.add(line);
		}	
		for (int v = xOff; v < mw; v += step) {
			int xp = mx + v;
			if (xp < mx || xp >= mx + mw) {
				continue;
			}
			
			line = new GridLine(xp, my, xp, sh);
			lines.add(line);
		}
	}
	
	public void draw(int mx, int my, int mw, int mh, float rotation) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		
		int cx = mx + mw / 2;
		int cy = my + mh / 2;
		
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		
		float angle = -rotation + 180;
		
		Window window = MinecraftClient.getInstance().getWindow();
		
		int winW = window.getFramebufferWidth();
		int winH = window.getFramebufferHeight();
		
		RenderSystem.viewport(winW - mx, my, mw, mh);
		
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		
//		System.out.println("=======");
//		System.out.println("" + mx);
//		System.out.println("" + my);
		
		RenderSystem.matrixMode(GL11.GL_PROJECTION);
		RenderSystem.pushMatrix();
		//RenderSystem.loadIdentity();
		//RenderSystem.ortho(mx, mw, mh, my, -1.0, 1.0);
		RenderSystem.translatef(cx, cy, 0);
		RenderSystem.rotatef(angle, 0, 0, 1.0F);
		RenderSystem.translatef(-cx, -cy, 0);
		
		builder.begin(GL11.GL_LINES, VertexFormats.POSITION);
		lines.forEach((line) -> {
			line.draw(builder);
		});
		
		tessellator.draw();
		
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GL11.GL_MODELVIEW);
		RenderSystem.enableTexture();
		
		RenderSystem.viewport(0, 0, winW, winH);
	}
	
	public void draw() {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);		
		
		builder.begin(GL11.GL_LINES, VertexFormats.POSITION);
		lines.forEach((line) -> {
			line.draw(builder);
		});
		
		tessellator.draw();
		
		RenderSystem.enableTexture();
	}	
	
	private class GridLine extends Line {
		private GridLine(int sx, int sy, int ex, int ey) {
			super(sx, sy, ex, ey);
		}
		
		private void draw(VertexConsumer builder) {
			builder.vertex(first.x, first.y, 0).next();
			builder.vertex(second.x, second.y, 0).next();
		}
	}
	
}
