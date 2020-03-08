package ru.bulldog.justmap.minimap.icon;

import net.minecraft.entity.Entity;

import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.EntityModelRenderer;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;

public class EntityIcon extends MapIcon<EntityIcon> {
	
	private final Entity entity;
	boolean hostile;
		
	public EntityIcon(Minimap map, Entity entity, boolean hostile) {
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
		
		int mapSize = JustMapClient.MAP.getMapSize();
		if (ClientParams.rotateMap) {
			rotatePos(pos, mapSize, mapX, mapY, rotation);
		}
		
		pos.x -= size / 2;
		pos.y -= size / 2;
		
		if (pos.x < mapX + size || pos.x > (mapX + mapSize) - size ||
			pos.y < mapY + size || pos.y > (mapY + mapSize) - size) return;
		
		EntityHeadIcon icon = null;
		if (ClientParams.showEntityHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(entity, pos.x, pos.y);
			} else {
				icon = EntityHeadIcon.getIcon(entity);
				if (icon != null) {
					icon.draw(pos.x, pos.y, size);
				} else {
					DrawHelper.drawOutlineCircle(pos.x, pos.y, size / 2, 0.6, color);
				}
			}
		} else {
			DrawHelper.drawOutlineCircle(pos.x, pos.y, size / 2, 0.6, color);
		}
	}
}
