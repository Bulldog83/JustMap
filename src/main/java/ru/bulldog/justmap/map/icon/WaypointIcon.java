package ru.bulldog.justmap.map.icon;

import net.minecraft.client.util.math.MatrixStack;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.waypoint.Waypoint;
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
		
		IconPos pos = new IconPos(mapX + x, mapY + y);
		
		if (ClientParams.rotateMap) {
			this.rotatePos(pos, map.getWidth(), map.getHeight(), mapX, mapY, rotation);
		}
		
		pos.x -= size / 2 + offX;
		pos.y -= size / 2 + offY;
		
		pos.x = MathUtil.clamp(pos.x, mapX, (mapX + map.getWidth()) - size);
		pos.y = MathUtil.clamp(pos.y, mapY, (mapY + map.getHeight()) - size);
		
		MatrixStack matrix = new MatrixStack();
		Waypoint.Icon icon = waypoint.getIcon();
		if (icon != null) {
			icon.draw(matrix, pos.x, pos.y, size);
		}
	}
	
	public boolean isHidden() {
		return waypoint.hidden;
	}
}