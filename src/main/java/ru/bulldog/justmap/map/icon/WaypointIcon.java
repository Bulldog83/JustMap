package ru.bulldog.justmap.map.icon;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.Point;

public class WaypointIcon extends MapIcon<WaypointIcon> {
	
	public final Waypoint waypoint;
	
	public WaypointIcon(IMap map, Waypoint waypoint) {
		super(map);
		this.waypoint = waypoint;
	}

	public void draw(int size) {
		double x = this.x - size / 2;
		double y = this.y - size / 2;
		
		Waypoint.Icon icon = waypoint.getIcon();
		if (icon != null) {
			icon.draw(x, y, size);
		}
	}
	
	@Override
	public void draw(MatrixStack matrices, VertexConsumerProvider consumerProvider, int mapX, int mapY, float rotation) {
		int size = 8;
		this.updatePos(size, mapX, mapY, rotation);
		Waypoint.Icon icon = waypoint.getIcon();
		if (icon != null) {
			matrices.push();
			if (ClientParams.rotateMap) {
				double moveX = iconPos.x + size / 2;
				double moveY = iconPos.y + size / 2;
				matrices.translate(moveX, moveY, 0.0);
				matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(rotation + 180.0F));
				matrices.translate(-moveX, -moveY, 0.0);
			}
			icon.draw(matrices, iconPos.x, iconPos.y, size);
			matrices.pop();
		}
	}
	
	@Override
	protected void updatePos(int size, int mapX, int mapY, float rotation) {
		int mapW = map.getWidth();
		int mapH = map.getHeight();
		int halfSize = size / 2;
		if (iconPos == null || x != lastX || y != lastY || mapX != lastMapX || mapY != lastMapY) {
			this.iconPos = new Point(mapX + x, mapY + y);
			this.iconPos.x -= halfSize;
			this.iconPos.y -= halfSize;
			if (Minimap.isRound()) {
				int centerX = mapX + mapW / 2;
				int centerY = mapY + mapH / 2;
				Line radius = new Line(centerX, centerY, centerX, mapY);
				Line rayTL = new Line(centerX, centerY, iconPos.x, iconPos.y);
				Line rayTR = new Line(centerX, centerY, iconPos.x + halfSize, iconPos.y);
				Line rayBL = new Line(centerX, centerY, iconPos.x, iconPos.y + halfSize);
				Line rayBR = new Line(centerX, centerY, iconPos.x + size, iconPos.y + halfSize);
				double diff = MathUtil.max(rayTL.difference(radius),
										   rayTR.difference(radius),
										   rayBL.difference(radius),
										   rayBR.difference(radius));
				if (diff > 0) {
					rayTL.subtract(diff);
				}
				this.iconPos.x = rayTL.second.x;
				this.iconPos.y = rayTL.second.y;
			} else {
				if (ClientParams.rotateMap) {
					//this.correctRotation(mapW, mapH, mapX, mapY, rotation);
				} else {
					this.iconPos.x = MathUtil.clamp(iconPos.x, mapX, (mapX + mapW) - size);
					this.iconPos.y = MathUtil.clamp(iconPos.y, mapY, (mapY + mapH) - size);
				}
			}
			this.lastMapX = mapX;
			this.lastMapY = mapY;
			this.lastX = x;
			this.lastY = y;
		}
	}
	
	private void correctRotation(int mapW, int mapH, int mapX, int mapY, float rotation) {
		double centerX = mapX + mapW / 2.0;
		double centerY = mapY + mapH / 2.0;
		
		rotation = MathUtil.correctAngle(rotation) + 180;
		
		double angle = Math.toRadians(-rotation);
		int trmX = (int) (centerX + (mapX - centerX) * Math.cos(angle) - (mapY - centerY) * Math.sin(angle));
		int trmY = (int) (centerY + (mapY - centerY) * Math.cos(angle) + (mapX - centerX) * Math.sin(angle));
		int tlmX = (int) (centerX + ((mapX + mapW) - centerX) * Math.cos(angle) - (mapY - centerY) * Math.sin(angle));
		int tlmY = (int) (centerY + (mapY - centerY) * Math.cos(angle) + ((mapX + mapW) - centerX) * Math.sin(angle));
		int brmX = (int) (centerX + (mapX - centerX) * Math.cos(angle) - ((mapY + mapH) - centerY) * Math.sin(angle));
		int brmY = (int) (centerY + ((mapY + mapH) - centerY) * Math.cos(angle) + (mapX - centerX) * Math.sin(angle));
		int blmX = (int) (centerX + ((mapX + mapW) - centerX) * Math.cos(angle) - ((mapY + mapH) - centerY) * Math.sin(angle));
		int blmY = (int) (centerY + ((mapY + mapH) - centerY) * Math.cos(angle) + ((mapX + mapW) - centerX) * Math.sin(angle));
		
		Line iconRay = new Line(centerX, centerY, iconPos.x, iconPos.y);
		Line top = new Line(trmX, trmY, tlmX, tlmY);
		Line bottom = new Line(brmX, brmY, blmX, blmY);
		Line right = new Line(trmX, trmY, brmX, brmY);
		Line left = new Line(tlmX, tlmY, blmX, blmY);
		
		Point cross = MathUtil.getCross(iconRay, top);
		if (cross != null) {
			iconPos = cross;
			return;
		}
		cross = MathUtil.getCross(iconRay, left);
		if (cross != null) {
			iconPos = cross;
			return;
		}
		cross = MathUtil.getCross(iconRay, bottom);
		if (cross != null) {
			iconPos = cross;
			return;
		}
		cross = MathUtil.getCross(iconRay, right);
		if (cross != null) {
			iconPos = cross;
		}
	}
	
	public boolean isHidden() {
		return this.waypoint.hidden;
	}
}