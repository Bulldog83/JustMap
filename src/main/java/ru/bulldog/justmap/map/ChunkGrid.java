package ru.bulldog.justmap.map;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.GameRenderer;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.render.RenderUtil;

import java.util.ArrayList;
import java.util.List;

public class ChunkGrid {
	
	private final static int color = Colors.GRID;	
	private final List<GridLine> lines;
	
	private float scale;
	private int rangeX;
	private int rangeY;
	private int rangeW;
	private int rangeH;
	private int x, z;
	
	public ChunkGrid(int x, int z, int rangeX, int rangeY, int rangeW, int rangeH, float scale) {
		this.lines = new ArrayList<>();
		updateRange(rangeX, rangeY, rangeW, rangeH, scale);
		updateCenter(x, z);
		updateGrid();
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
		clear();
		
		double centerX = rangeX + rangeW / 2.0;
		double centerY = rangeY + rangeH / 2.0;
		int startX = ((int) MathUtil.worldPos(rangeX, x, centerX, scale) >> 4) << 4;
		int startZ = ((int) MathUtil.worldPos(rangeY, z, centerY, scale) >> 4) << 4;
		int right = rangeX + rangeW;
		int bottom = rangeY + rangeH;
		
		double xp = MathUtil.screenPos(startX, x, centerX, scale);
		while (xp < right) {
			if (xp > rangeX) {
				lines.add(new GridLine(xp, rangeY, xp, bottom));
			}
			startX += 16;
			xp = MathUtil.screenPos(startX, x, centerX, scale);
		}
		double yp = MathUtil.screenPos(startZ, z, centerY, scale);
		while(yp < bottom) {
			if (yp > rangeY) {
				lines.add(new GridLine(rangeX, yp, right, yp));
			}
			startZ += 16;
			yp = MathUtil.screenPos(startZ, z, centerY, scale);
		}
	}
	
	public void draw() {
		float a = (float) (color >> 24 & 255) / 255.0F;
		float r = (float) (color >> 16 & 255) / 255.0F;
		float g = (float) (color >> 8 & 255) / 255.0F;
		float b = (float) (color & 255) / 255.0F;
		
		RenderSystem.disableTexture();
		RenderSystem.setShaderColor(r, g, b, a);
		RenderSystem.setShader(GameRenderer::getPositionShader);
		RenderUtil.startDraw(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION);
		BufferBuilder buffer = RenderUtil.getBuffer();
		lines.forEach(line -> line.draw(buffer));
		RenderUtil.endDraw();
		RenderSystem.enableTexture();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
	}	
	
	private static class GridLine extends Line {
		private GridLine(double sx, double sy, double ex, double ey) {
			super(sx, sy, ex, ey);
		}
		
		private void draw(VertexConsumer builder) {
			builder.vertex(first.x, first.y, 0).endVertex();
			builder.vertex(second.x, second.y, 0).endVertex();
		}
	}
	
	public void clear() {
		lines.clear();
	}
}