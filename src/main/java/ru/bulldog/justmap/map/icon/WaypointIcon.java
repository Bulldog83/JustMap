package ru.bulldog.justmap.map.icon;

import net.minecraft.client.util.math.MatrixStack;

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
	public void draw(MatrixStack matrixStack, int mapX, int mapY, double offX, double offY, float rotation) {
		int size = 8;
		int halfSize = size / 2;
		
		Point iconPos = new Point(mapX + x, mapY + y);
		
		iconPos.x -= halfSize + offX;
		iconPos.y -= halfSize + offY;
		
		int mapW = map.getWidth();
		int mapH = map.getHeight();
		if (ClientParams.rotateMap) {
			this.rotatePos(iconPos, mapW, mapH, mapX, mapY, rotation);
		}
		
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
			iconPos.x = rayTL.second.x;
			iconPos.y = rayTL.second.y;
		}
		
		iconPos.x = MathUtil.clamp(iconPos.x, mapX, (mapX + mapW) - size);
		iconPos.y = MathUtil.clamp(iconPos.y, mapY, (mapY + mapH) - size);
		
		MatrixStack matrix = new MatrixStack();
		Waypoint.Icon icon = waypoint.getIcon();
		if (icon != null) {
			icon.draw(matrix, iconPos.x, iconPos.y, size);
		}
	}
	
	public boolean isHidden() {
		return this.waypoint.hidden;
	}
}