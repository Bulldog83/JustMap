package ru.bulldog.justmap.minimap.icon;

import net.minecraft.entity.Entity;
import ru.bulldog.justmap.client.JustMapClient;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.minimap.EntityModelRenderer;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.MathUtil;

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
		int drawX = mapX + x - size / 2;
		int drawY = mapY + y - size / 2;
		
		int mapSize = JustMapClient.MAP.getMapSize();
		if (ClientParams.rotateMap) {
			int centerX = mapX + mapSize / 2;
			int centerY = mapY + mapSize / 2;
			double l = Math.sqrt(MathUtil.pow2(drawX - centerX) + MathUtil.pow2(drawY - centerY));
			double angle = Math.toRadians(rotation + 180);
			
			drawX = (int) (drawX + Math.cos(angle) * l);
			drawY = (int) (drawY + Math.sin(angle) * l);
		}
		
		if (drawX < mapX || drawX > (mapX + mapSize) ||
			drawY < mapY || drawY > (mapY + mapSize)) return;
		
		EntityHeadIcon icon = null;
		if (ClientParams.showEntityHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(entity, drawX, drawY);
			} else {
				icon = EntityHeadIcon.getIcon(entity);
				if (icon != null) {
					icon.draw(drawX, drawY, size);
				} else {
					DrawHelper.drawOutlineCircle(drawX, drawY, size / 1.75, 0.6, color);
				}
			}
		} else {
			DrawHelper.drawOutlineCircle(drawX, drawY, size / 1.75, 0.6, color);
		}
	}
}
