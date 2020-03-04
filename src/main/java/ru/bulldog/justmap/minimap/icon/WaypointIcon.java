package ru.bulldog.justmap.minimap.icon;

import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.minimap.waypoint.Waypoint;
import ru.bulldog.justmap.util.DrawHelper;

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