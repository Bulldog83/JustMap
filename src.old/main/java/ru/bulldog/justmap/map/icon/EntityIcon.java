package ru.bulldog.justmap.map.icon;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.PoseStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.client.render.EntityModelRenderer;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.RuleUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.render.GLC;
import ru.bulldog.justmap.util.render.RenderUtil;

public class EntityIcon extends MapIcon<EntityIcon> {
	
	private final Entity entity;
	boolean hostile;
		
	public EntityIcon(Entity entity) {
		this.hostile = entity instanceof HostileEntity;
		this.entity = entity;
	}
	
	@Override
	public void draw(PoseStack matrices, VertexConsumerProvider consumerProvider, int mapX, int mapY, int mapW, int mapH, float rotation) {
		if (!RuleUtil.allowCreatureRadar() && !hostile) { return; }
		if (!RuleUtil.allowHostileRadar() && hostile) { return; }
		
		int color;
		if (entity instanceof TameableEntity) {
			TameableEntity tameable = (TameableEntity) entity;
			color = tameable.isTamed() ? Colors.GREEN : Colors.YELLOW;
		} else {
			color = (hostile) ? Colors.DARK_RED : Colors.YELLOW;
		}
		int size = ClientSettings.entityIconSize;
		this.updatePos(mapX, mapY, mapW, mapH, size);
		if (!allowRender) return;
		if (ClientSettings.renderEntityModel) {
			EntityModelRenderer.renderModel(matrices, consumerProvider, entity, iconPos.x, iconPos.y);
		} else if (ClientSettings.showEntityHeads) {
			EntityHeadIcon icon = EntityHeadIcon.getIcon(entity);
			if (icon != null) {
				if (ClientSettings.entityIconsShading) {
					int posY = DataUtil.coordY();
					int hdiff = posY - height;
					float hmod;
					if (hdiff < 0) {
						hmod = MathUtil.clamp(Math.abs(hdiff) / 24F, 0.0F, 0.5F);
						RenderUtil.texEnvMode(GLC.GL_ADD);
					} else {
						hmod = MathUtil.clamp((24 - Math.abs(hdiff)) / 24F, 0.25F, 1.0F);
						RenderUtil.texEnvMode(GLC.GL_MODULATE);
					}
					RenderSystem.color3f(hmod, hmod, hmod);
				}
				double moveX = iconPos.x + size / 2;
				double moveY = iconPos.y + size / 2;
				float scale = MathUtil.clamp(1.0F / ClientSettings.mapScale, 0.5F, 1.5F);
				matrices.push();
				matrices.translate(moveX, moveY, 0.0);
				if (ClientSettings.rotateMap) {
					matrices.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(rotation + 180.0F));
				}
				matrices.scale(scale, scale, 1.0F);
				matrices.translate(-moveX, -moveY, 0.0);
				icon.draw(matrices, iconPos.x, iconPos.y, size);
				matrices.pop();
				RenderUtil.texEnvMode(GLC.GL_MODULATE);
				RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			} else {
				RenderUtil.drawOutlineCircle(iconPos.x, iconPos.y, size / 3, 0.6, color);
			}
		} else {
			RenderUtil.drawOutlineCircle(iconPos.x, iconPos.y, size / 3, 0.6, color);
		}
	}
}
