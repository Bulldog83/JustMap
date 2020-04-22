package ru.bulldog.justmap.map.icon;

import net.minecraft.entity.Entity;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;

public class EntityIcon extends MapIcon<EntityIcon> {
	
	private final Entity entity;
	boolean hostile;
		
	public EntityIcon(IMap map, Entity entity, boolean hostile) {
		super(map);
			
		this.entity = entity;
		this.hostile = hostile;
	}
	
	@Override
	public void draw(int mapX, int mapY, float rotation) {
		if (!Minimap.allowCreatureRadar() && !hostile) { return; }
		if (!Minimap.allowHostileRadar() && hostile) { return; }
		
		int size = ClientParams.showEntityHeads ? ClientParams.entityIconSize : 4;
		int color = (hostile) ? Colors.DARK_RED : Colors.YELLOW;
		
		IconPos pos = new IconPos(mapX + x, mapY + y);
		
		if (ClientParams.rotateMap) {
			rotatePos(pos, map.getWidth(), map.getHeight(), mapX, mapY, rotation);
		}
		
		pos.x -= size / 2;
		pos.y -= size / 2;
		
		if (pos.x < mapX || pos.x > (mapX + map.getWidth()) - size ||
			pos.y < mapY || pos.y > (mapY + map.getHeight()) - size) return;
		
		EntityHeadIcon icon = null;
		if (ClientParams.showEntityHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(entity, pos.x, pos.y);
			} else {
				icon = EntityHeadIcon.getIcon(entity);
				if (icon != null) {
					icon.draw(pos.x, pos.y, size);
				} else {
					DrawHelper.drawOutlineCircle(pos.x, pos.y, size / 3, 0.6, color);
				}
			}
		} else {
			DrawHelper.drawOutlineCircle(pos.x, pos.y, size / 3, 0.6, color);
		}
	}
}
