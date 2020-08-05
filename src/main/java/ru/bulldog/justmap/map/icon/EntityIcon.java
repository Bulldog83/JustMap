package ru.bulldog.justmap.map.icon;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.Point;

public class EntityIcon extends MapIcon<EntityIcon> {
	
	private final Entity entity;
	boolean hostile;
		
	public EntityIcon(IMap map, Entity entity, boolean hostile) {
		super(map);
			
		this.entity = entity;
		this.hostile = hostile;
	}
	
	@Override
	public void draw(MatrixStack matrixStack, int mapX, int mapY, double offX, double offY, float rotation) {
		if (!RuleUtil.allowCreatureRadar() && !hostile) { return; }
		if (!RuleUtil.allowHostileRadar() && hostile) { return; }
		
		int size = ClientParams.showEntityHeads ? ClientParams.entityIconSize : 4;
		
		int color;
		if (entity instanceof TameableEntity) {
			TameableEntity tameable = (TameableEntity) entity;
			color = tameable.isTamed() ? Colors.GREEN : Colors.YELLOW;
		} else {
			color = (hostile) ? Colors.DARK_RED : Colors.YELLOW;
		}
		
		Point iconPos = new Point(mapX + x, mapY + y);
		
		iconPos.x -= size / 2 + offX;
		iconPos.y -= size / 2 + offY;
		
		int mapW = map.getWidth();
		int mapH = map.getHeight();		
		if (Minimap.isRound()) {
			int centerX = mapX + mapW / 2;
			int centerY = mapY + mapH / 2;
			Line radius = new Line(centerX, centerY, centerX, mapY);
			Line rayTL = new Line(centerX, centerY, iconPos.x, iconPos.y);
			Line rayTR = new Line(centerX, centerY, iconPos.x + size, iconPos.y);
			Line rayBL = new Line(centerX, centerY, iconPos.x, iconPos.y + size);
			Line rayBR = new Line(centerX, centerY, iconPos.x + size, iconPos.y + size);
			double diff = MathUtil.max(rayTL.difference(radius),
									   rayTR.difference(radius),
									   rayBL.difference(radius),
									   rayBR.difference(radius));
			
			if (diff > 0) return;
		}
		
		if (iconPos.x < mapX || iconPos.x > (mapX + mapW) - size ||
			iconPos.y < mapY || iconPos.y > (mapY + mapH) - size) return;
		
		if (ClientParams.rotateMap) {
			this.rotatePos(iconPos, mapW, mapH, mapX, mapY, rotation);
		}
		
		if (ClientParams.renderEntityModel) {
			EntityModelRenderer.renderModel(entity, iconPos.x, iconPos.y);
		} else if (ClientParams.showEntityHeads) {
			EntityHeadIcon icon = EntityHeadIcon.getIcon(entity);
			if (icon != null) {					
				icon.draw(matrixStack, iconPos.x, iconPos.y, size);
			} else {
				RenderUtil.drawOutlineCircle(iconPos.x, iconPos.y, size / 3, 0.6, color);
			}
		} else {
			RenderUtil.drawOutlineCircle(iconPos.x, iconPos.y, size / 3, 0.6, color);
		}
	}
}
