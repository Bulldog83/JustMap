package ru.bulldog.justmap.map.icon;

import net.minecraft.client.util.math.MatrixStack;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;

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
		
		IconPos iconPos = new IconPos(mapX + x, mapY + y);
		
		iconPos.x -= size / 2 + offX;
		iconPos.y -= size / 2 + offY;
		
		int mapW = map.getWidth();
		int mapH = map.getHeight();
		if (ClientParams.rotateMap) {
			this.rotatePos(iconPos, mapW, mapH, mapX, mapY, rotation);
		}
		
		if (Minimap.isRound()) {
			int centerX = mapX + mapW / 2;
			int centerY = mapY + mapH / 2;
			Line radius = new Line(centerX, centerY, centerX, mapY);
			Line iconRay = new Line(centerX, centerY, iconPos.x, iconPos.y);
			if (iconRay.lenght() > radius.lenght()) {
				double diff = iconRay.lenght() - radius.lenght();
				double brdX = centerX + mapW;
				double brdY = centerY + mapH;
				if (iconPos.x > brdX || iconPos.y > brdY) {
					diff += size;
				}
				iconRay.subtract(diff);
				iconPos.x = iconRay.second.x;
				iconPos.y = iconRay.second.y;
			}
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
		return waypoint.hidden;
	}
}