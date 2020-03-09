package ru.bulldog.justmap.minimap.icon;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.minimap.waypoint.Waypoint;
import ru.bulldog.justmap.util.math.MathUtil;

public class WaypointIcon extends MapIcon<WaypointIcon> {
	private Waypoint waypoint;
	public WaypointIcon(Minimap map, Waypoint waypoint) {
		super(map);
		this.waypoint = waypoint;
	}

	@Override
	public void draw(int mapX, int mapY, float rotation) {
		int size = 8;
		
		IconPos pos = new IconPos(mapX + x, mapY + y);
		
		int mapSize = JustMapClient.MAP.getMapSize();
		if (ClientParams.rotateMap) {
			rotatePos(pos, mapSize, mapX, mapY, rotation);
		}
		
		pos.x -= size / 2;
		pos.y -= size / 2;
		
		pos.x = MathUtil.clamp(pos.x, mapX, mapX + mapSize - size);
		pos.y = MathUtil.clamp(pos.y, mapY, mapY + mapSize - size);
		
		Waypoint.Icon icon = waypoint.getIcon();
		if (icon != null) {
			icon.draw(pos.x, pos.y, size);
		}			
	}
	
	public boolean isHidden() {
		return waypoint.hidden;
	}
}