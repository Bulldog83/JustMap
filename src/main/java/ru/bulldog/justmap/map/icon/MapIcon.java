package ru.bulldog.justmap.map.icon;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.Point;

public abstract class MapIcon<T extends MapIcon<T>> {

	protected Point iconPos;
	protected boolean allowRender = true;
	protected int lastMapX, lastMapY;
	protected double lastX, lastY;
	protected double x, y;
	protected int height;

	public MapIcon() {}

	public void setPosition(double x, double y, int height) {
		this.height = height;
		this.x = x;
		this.y = y;
	}

	public int getHeight() {
		return this.height;
	}

	protected void updatePos(int mapX, int mapY, int mapW, int mapH, int size) {
		if (iconPos == null || x != lastX || y != lastY || mapX != lastMapX || mapY != lastMapY) {
			this.iconPos = new Point(x, y);
			this.iconPos.x -= size / 2;
			this.iconPos.y -= size / 2;
			this.allowRender = true;
			if (Minimap.isRound()) {
				int centerX = mapX + mapW / 2;
				int centerY = mapY + mapH / 2;
				Line radius = new Line(centerX, centerY, centerX, mapY);
				Line rayTL = new Line(centerX, centerY, iconPos.x, iconPos.y);
				Line rayTR = new Line(centerX, centerY, iconPos.x + size, iconPos.y);
				Line rayBL = new Line(centerX, centerY, iconPos.x, iconPos.y + size);
				Line rayBR = new Line(centerX, centerY, iconPos.x + size, iconPos.y + size);
				double diff = MathUtil.max(rayTL.difference(radius),
										   rayTR.difference(radius),
										   rayBL.difference(radius),
										   rayBR.difference(radius));

				if (diff > 0) {
					this.allowRender = false;
				}
			} else if (iconPos.x < mapX || iconPos.x > (mapX + mapW) - size ||
					   iconPos.y < mapY || iconPos.y > (mapY + mapH) - size) {

				this.allowRender = false;
			}
			this.lastMapX = mapX;
			this.lastMapY = mapY;
			this.lastX = x;
			this.lastY = y;
		}
	}

	public abstract void draw(MatrixStack matrixStack, VertexConsumerProvider consumerProvider, int mapX, int mapY, int mapW, int mapH, float rotation);
}
