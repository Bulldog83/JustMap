package ru.bulldog.justmap.map.icon;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
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
	public void draw(int mapX, int mapY, double offX, double offY, float rotation) {
		if (!Minimap.allowCreatureRadar() && !hostile) { return; }
		if (!Minimap.allowHostileRadar() && hostile) { return; }
		
		int size = ClientParams.showEntityHeads ? ClientParams.entityIconSize : 4;
		int color = (hostile) ? Colors.DARK_RED : Colors.YELLOW;
		
		IconPos pos = new IconPos(mapX + x, mapY + y);
		
		pos.x -= size / 2 + offX;
		pos.y -= size / 2 + offY;
		
		if (pos.x < mapX || pos.x > (mapX + map.getWidth()) - size ||
			pos.y < mapY || pos.y > (mapY + map.getHeight()) - size) return;
		
		EntityHeadIcon icon = null;
		if (ClientParams.showEntityHeads) {
			if (ClientParams.renderEntityModel) {
				EntityModelRenderer.renderModel(entity, pos.x, pos.y);
			} else {
				icon = EntityHeadIcon.getIcon(entity);
				if (icon != null) {
					MatrixStack matrix = new MatrixStack();					
					matrix.push();
					if (ClientParams.rotateMap) {
						double moveX = pos.x + size / 2;
						double moveY = pos.y + size / 2;
						matrix.translate(moveX, moveY, 0.0);
						matrix.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(rotation + 180));
						matrix.translate(-moveX, -moveY, 0.0);
					}					
					icon.draw(matrix, pos.x, pos.y, size);
					matrix.pop();
				} else {
					DrawHelper.drawOutlineCircle(pos.x, pos.y, size / 3, 0.6, color);
				}
			}
		} else {
			DrawHelper.drawOutlineCircle(pos.x, pos.y, size / 3, 0.6, color);
		}
	}
}
