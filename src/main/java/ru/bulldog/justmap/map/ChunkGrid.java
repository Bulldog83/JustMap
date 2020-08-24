package ru.bulldog.justmap.map;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;

import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;

public class ChunkGrid {
	
	private final static int color = Colors.GRID;	
	private final List<GridLine> lines;
	
	private int x, z;
	private float scale;
	private int rangeX;
	private int rangeY;
	private int rangeW;
	private int rangeH;
	
	public ChunkGrid(int x, int z, int rangeX, int rangeY, int rangeW, int rangeH, float scale) {
		this.lines = new ArrayList<>();
		this.updateRange(rangeX, rangeY, rangeW, rangeH, scale);
		this.updateCenter(x, z);
		this.updateGrid();
	}
	
	public void updateCenter(int x, int z) {
		this.x = x;
		this.z = z;
	}
	
	public void updateRange(int rangeX, int rangeY, int rangeW, int rangeH, float scale) {
		this.rangeX = rangeX;
		this.rangeY = rangeY;
		this.rangeW = rangeW;
		this.rangeH = rangeH;
		this.scale = scale;
	}
	
	public void updateGrid() {
		this.clear();
		
		int centerX = rangeX + rangeW / 2;
		int centerY = rangeY + rangeH / 2;
		int startX = ((int) MathUtil.worldPos(rangeX, x, centerX, scale) >> 4) << 4;
		int startZ = ((int) MathUtil.worldPos(rangeY, z, centerY, scale) >> 4) << 4;
		int right = rangeX + rangeW;
		int bottom = rangeY + rangeH;
		
		int xp = (int) MathUtil.screenPos(startX, x, centerX, scale);
		while (xp < right) {
			if (xp > rangeX) {
				this.lines.add(new GridLine(xp, rangeY, xp, bottom));
			}
			startX += 16;
			xp = (int) MathUtil.screenPos(startX, x, centerX, scale);
		}
		int yp = (int) MathUtil.screenPos(startZ, z, centerY, scale);
		while(yp < bottom) {
			if (yp > rangeY) {
				this.lines.add(new GridLine(rangeX, yp, right, yp));
			}
			startZ += 16;
			yp = (int) MathUtil.screenPos(startZ, z, centerY, scale);
		}
	}
	
	public void draw() {
		float a = (float) (color >> 24 & 255) / 255.0F;
		float r = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float b = (float) (color & 255) / 255.0F;
		
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		RenderUtil.startDraw(GL11.GL_LINES, VertexFormats.POSITION);
		BufferBuilder buffer = RenderUtil.getBuffer();
		lines.forEach(line -> line.draw(buffer));
		RenderUtil.endDraw();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderSystem.enableTexture();
	}	
	
	private class GridLine extends Line {
		private GridLine(double sx, double sy, double ex, double ey) {
			super(sx, sy, ex, ey);
		}
		
		private void draw(VertexConsumer builder) {
			builder.vertex(first.x, first.y, 0).next();
			builder.vertex(second.x, second.y, 0).next();
		}
	}
	
	public void clear() {
		this.lines.clear();
	}
}