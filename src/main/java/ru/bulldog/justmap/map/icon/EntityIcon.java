package ru.bulldog.justmap.map.icon;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.TameableEntity;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.RuleUtil;

public class EntityIcon extends MapIcon<EntityIcon> {
	
	private final Entity entity;
	boolean hostile;
		
	public EntityIcon(IMap map, Entity entity, boolean hostile) {
		super(map);
			
		this.entity = entity;
		this.hostile = hostile;
	}
	
	@Override
	public void draw(MatrixStack matrices, VertexConsumerProvider consumerProvider, int mapX, int mapY, double offX, double offY, float rotation) {
		if (!RuleUtil.allowCreatureRadar() && !hostile) { return; }
		if (!RuleUtil.allowHostileRadar() && hostile) { return; }
		
		int color;
		if (entity instanceof TameableEntity) {
			TameableEntity tameable = (TameableEntity) entity;
			color = tameable.isTamed() ? Colors.GREEN : Colors.YELLOW;
		} else {
			color = (hostile) ? Colors.DARK_RED : Colors.YELLOW;
		}
		int size = ClientParams.entityIconSize;
		this.updatePos(size, mapX, mapY, offX, offY, rotation);
		if (!allowRender) return;
		if (ClientParams.renderEntityModel) {
			EntityModelRenderer.renderModel(matrices, consumerProvider, entity, iconPos.x, iconPos.y);
		} else if (ClientParams.showEntityHeads) {
			EntityHeadIcon icon = EntityHeadIcon.getIcon(entity);
			if (icon != null) {					
				icon.draw(matrices, iconPos.x, iconPos.y, size);
			} else {
				RenderUtil.drawOutlineCircle(iconPos.x, iconPos.y, size / 3, 0.6, color);
			}
		} else {
			RenderUtil.drawOutlineCircle(iconPos.x, iconPos.y, size / 3, 0.6, color);
		}
	}
}
