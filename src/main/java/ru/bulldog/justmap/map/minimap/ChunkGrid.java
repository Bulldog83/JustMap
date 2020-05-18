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
import ru.bulldog.justmap.util.math.Line;

public class ChunkGrid {
	
	private final static Tessellator tessellator = Tessellator.getInstance();
	private final static BufferBuilder builder = tessellator.getBuffer();	
	private final static int color = 0xAA333333;	
	
	private final List<GridLine> lines;
	
	public ChunkGrid(int x, int y, int mapX, int mapY, int mapW, int mapH) {
		this.lines = new ArrayList<>();
		
		float scale = ClientParams.mapScale;
		
		int xOff = (int) ((((x >> 4) << 4) - x) / scale);
		int yOff = (int) ((((y >> 4) << 4) - y) / scale);
		
		int right = mapX + mapW;
		int bottom = mapY + mapH;
		
		GridLine line;
		int step = (int) (16 / scale);
		for (int cH = yOff; cH < mapH; cH += step) {
			int yp = mapY + cH;
			if (yp < mapY || yp > mapY + mapH) {
				continue;
			}
			
			line = new GridLine(mapX, yp, right, yp);
			this.lines.add(line);
		}	
		for (int v = xOff; v < mapW; v += step) {
			int xp = mapX + v;
			if (xp < mapX || xp >= mapX + mapW) {
				continue;
			}
			
			line = new GridLine(xp, mapY, xp, bottom);
			this.lines.add(line);
		}
	}
	
	public void draw() {
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