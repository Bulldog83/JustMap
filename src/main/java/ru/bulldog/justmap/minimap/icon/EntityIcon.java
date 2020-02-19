package ru.bulldog.justmap.minimap.icon;

import net.minecraft.entity.Entity;
import ru.bulldog.justmap.config.Params;
import ru.bulldog.justmap.minimap.EntityModelRenderer;
import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.Drawer;

public class EntityIcon extends MapIcon<EntityIcon> {
	
	private final Entity entity;
	boolean hostile;
		
	public EntityIcon(Minimap map, Entity entity, boolean hostile) {
		super(map);
			
		this.entity = entity;
		this.hostile = hostile;
	}
	
	@Override
	public void draw(int mapX, int mapY) {
		if (!Minimap.allowCreatureRadar() && !hostile) { return; }
		if (!Minimap.allowHostileRadar() && hostile) { return; }
		
		int size = Params.showEntityHeads ? Params.entityIconSize : 4;
		int col = (hostile) ? Colors.ORANGE : Colors.YELLOW;
		
		int drawX = mapX + x - size / 2;
		int drawY = mapY + y - size / 2;
		
		EntityHeadIcon icon = null;
		if (Params.showEntityHeads) {
			if (Params.renderEntityModel) {
				EntityModelRenderer.renderModel(entity, drawX, drawY);
			} else {
				icon = EntityHeadIcon.getIcon(entity);
				if (icon != null) {
					icon.draw(drawX, drawY, size);
				} else {
					Drawer.fill(drawX, drawY, drawX + size, drawY + size, col);
				}
			}
		} else {
			Drawer.fill(drawX, drawY, drawX + size, drawY + size, col);
		}
	}
}
