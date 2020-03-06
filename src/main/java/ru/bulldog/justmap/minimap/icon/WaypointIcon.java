package ru.bulldog.justmap.minimap.icon;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.minimap.waypoint.Waypoint;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.MathUtil;

public class WaypointIcon extends MapIcon<WaypointIcon> {
	private Waypoint waypoint;
	public WaypointIcon(Minimap map, Waypoint waypoint) {
		super(map);
		this.waypoint = waypoint;
	}

	@Override
	public void draw(int mapX, int mapY, float rotation) {
		int size = 8;
		int col = waypoint.color;
		
		int drawX = mapX + x - size / 2;
		int drawY = mapY + y - size / 2;
		
		int mapSize = JustMapClient.MAP.getMapSize();
		if (ClientParams.rotateMap) {
			int centerX = mapX + mapSize / 2;
			int centerY = mapY + mapSize / 2;
			
			rotation = MathUtil.correctAngle(rotation);
			
			double l = Math.sqrt(MathUtil.pow2(drawX - centerX) + MathUtil.pow2(drawY - centerY));			
			double angle = Math.toRadians(rotation);
			double angle2 = Math.atan2(centerY - drawY, centerX - drawX) - angle;
			
			drawX = (int) (centerX + Math.cos(angle2) * l);
			drawY = (int) (centerY + Math.sin(angle2) * l);
		}
		
		if (drawX < mapX || drawX > (mapX + mapSize) ||
			drawY < mapY || drawY > (mapY + mapSize)) return;

		Waypoint.Icon icon = waypoint.getIcon();
		if (icon != null) {
			icon.draw(drawX, drawY, size);
		} else {
			DrawHelper.drawDiamond(drawX, drawY, size, size, col);
		}			
	}
	
	public boolean isHidden() {
		return waypoint.hidden;
	}
}