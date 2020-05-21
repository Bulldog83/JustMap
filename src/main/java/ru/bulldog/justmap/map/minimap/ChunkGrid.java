package ru.bulldog.justmap.map.minimap;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;

import ru.bulldog.justmap.client.config.ClientParams;

public class ChunkGrid {
	
	private final static Tessellator tessellator = Tessellator.getInstance();
	private final static BufferBuilder builder = tessellator.getBuffer();	
	private final static int color = 0xAA333333;	
	
	private final List<GridLine> lines;
	
	private int mapX, mapY, mapW, mapH;
	
	public ChunkGrid(int mapX, int mapY, int mapW, int mapH) {
		this.lines = new ArrayList<>();
		this.mapX = mapX;
		this.mapY = mapY;
		this.mapW = mapW;
		this.mapH = mapH;
	}
	
	public void update(int posX, int posZ) {
		this.lines.clear();
		
		float scale = ClientParams.mapScale;
		
		double xOff = (((posX >> 4) << 4) - posX) / scale;
		double yOff = (((posZ >> 4) << 4) - posZ) / scale;
		
		int top = mapY - 8;
		int left = mapX - 8;
		int right = mapX + mapW + 8;
		int bottom = mapY + mapH + 8;
		
		int step = (int) (16 / scale);
		
		GridLine line;
		for (double cH = yOff; cH <= mapH; cH += step) {
			double yp = mapY + cH;
			if (yp < top) continue;
			if (yp > bottom) break;
			
			line = new GridLine(left, yp, right, yp);
			this.lines.add(line);
		}	
		for (double v = xOff; v <= mapW; v += step) {
			double xp = mapX + v;
			if (xp < left) continue;
			if (xp > right) break;
			
			line = new GridLine(xp, top, xp, bottom);
			this.lines.add(line);
		}
	}
	
	public void draw() {
		float a = (color >> 24 & 255) / 255.0F;
		float r = (color >> 16 & 255) / 255.0F;
		float g = (color >> 8 & 255) / 255.0F;
		float b = (color & 255) / 255.0F;
		
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);		
		
		builder.begin(GL11.GL_LINES, VertexFormats.POSITION);
		lines.forEach((line) -> {
			line.draw(builder);
		});		
		tessellator.draw();
		
		RenderSystem.enableTexture();
	}	
	
	private class GridLine {
		double x1, x2, y1, y2;
		
		GridLine(double x1, double y1, double x2, double y2) {
			this.x1 = x1;
			this.y1 = y1;
			this.x2 = x2;
			this.y2 = y2;
		}
		
		private void draw(VertexConsumer builder) {
			builder.vertex(x1, y1, 0.0).next();
			builder.vertex(x2, y2, 0.0).next();
		}
	}
	
}