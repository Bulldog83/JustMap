package ru.bulldog.justmap.map;

import java.util.ArrayList;
import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.util.math.BlockPos;

import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;

public class ChunkGrid {
	
	private final int color = Colors.GRID;	
	private final List<GridLine> lines;
	
	public ChunkGrid(BlockPos center, int mx, int my, int mw, int mh, float scale) {
		this.lines = new ArrayList<>();
		
		int centerX = mx + mw / 2;
		int centerY = mw + mh / 2;
		int cornerX = ((int) MathUtil.worldPos(mx, center.getX(), centerX, scale) >> 4) << 4;
		int cornerZ = ((int) MathUtil.worldPos(my, center.getZ(), centerY, scale) >> 4) << 4;
		int sw = mx + mw;
		int sh = my + mh;
		
		int step = 16;
		int xp = (int) MathUtil.screenPos(cornerX, center.getX(), centerX, scale);
		while (xp < sw) {
			if (xp > mx && xp < sw) {
				lines.add(new GridLine(xp, my, xp, sh));
			}
			cornerX += step;
			xp = (int) MathUtil.screenPos(cornerX, center.getX(), centerX, scale);
		}

		int yp = (int) MathUtil.screenPos(cornerZ, center.getZ(), centerY, scale);
		while(yp < sh) {
			if (yp > my && yp < sh) {
				lines.add(new GridLine(mx, yp, sw, yp));
			}
			cornerZ += step;
			yp = (int) MathUtil.screenPos(cornerZ, center.getZ(), centerY, scale);
		}
	}
	
	public void draw() {
		float a = (float)(color >> 24 & 255) / 255.0F;
		float r = (float)(color >> 16 & 255) / 255.0F;
		float g = (float)(color >> 8 & 255) / 255.0F;
		float b = (float)(color & 255) / 255.0F;
		
		RenderSystem.disableTexture();
		RenderSystem.color4f(r, g, b, a);
		RenderUtil.startDraw(GL11.GL_LINES, VertexFormats.POSITION);
		BufferBuilder buffer = RenderUtil.getBuffer();
		lines.forEach((line) -> {
			line.draw(buffer);
		});
		RenderUtil.endDraw();
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