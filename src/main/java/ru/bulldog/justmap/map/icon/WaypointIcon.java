package ru.bulldog.justmap.map.icon;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.math.Line;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.math.Point;
import ru.bulldog.justmap.util.render.GLC;
import ru.bulldog.justmap.util.render.RenderUtil;

public class WaypointIcon extends MapIcon<WaypointIcon> {

	public final Waypoint waypoint;
	private final IMap map;

	private final int iconSize = 8;
	private float lastRotation;
	private float offX;
	private float offY;

	public WaypointIcon(IMap map, Waypoint waypoint) {
		this.waypoint = waypoint;
		this.map = map;
	}

	public void draw(int size) {
		double x = this.x - size / 2;
		double y = this.y - size / 2;

		Waypoint.Icon icon = waypoint.getIcon();
		if (icon != null) {
			icon.draw(x, y, size);
		}
	}

	public void draw(MatrixStack matrices, VertexConsumerProvider consumerProvider, int mapX, int mapY, int mapW, int mapH, double offX, double offY, double rotation) {
		rotation = MathUtil.correctAngle(rotation + 180);
		this.updatePos(mapX, mapY, mapW, mapH, iconSize, rotation);
		this.applyOffset(offX, offY, rotation);
		this.draw(matrices, consumerProvider, mapX, mapY, mapW, mapH, (float) rotation);
	}

	@Override
	public void draw(MatrixStack matrices, VertexConsumerProvider consumerProvider, int mapX, int mapY, int mapW, int mapH, float rotation) {
		Waypoint.Icon icon = waypoint.getIcon();
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
				RenderSystem.setShaderColor(hmod, hmod, hmod, 1.0F);
			}
			icon.draw(matrices, iconPos.x - offX, iconPos.y - offY, iconSize);
			RenderUtil.texEnvMode(GLC.GL_MODULATE);
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		}
	}

	private void updatePos(int mapX, int mapY, int mapW, int mapH, int size, double rotation) {
		if (iconPos == null || x != lastX || y != lastY || mapX != lastMapX || mapY != lastMapY || rotation != lastRotation) {
			int centerX = mapX + mapW / 2;
			int centerY = mapY + mapH / 2;
			this.iconPos = new Point(x, y);
			if (map.isRotated()) {
				this.correctRotation(centerX, centerY, rotation);
			}
			int halfSize = size / 2;
			this.iconPos.x -= halfSize;
			this.iconPos.y -= halfSize;
			if (Minimap.isRound()) {
				Line radius = new Line(centerX, centerY, centerX, mapY);
				Line rayTL = new Line(centerX, centerY, iconPos.x, iconPos.y);
				Line rayTR = new Line(centerX, centerY, iconPos.x + halfSize, iconPos.y);
				Line rayBL = new Line(centerX, centerY, iconPos.x, iconPos.y + halfSize);
				Line rayBR = new Line(centerX, centerY, iconPos.x + size, iconPos.y + halfSize);
				double diff = MathUtil.max(rayTL.difference(radius),
										   rayTR.difference(radius),
										   rayBL.difference(radius),
										   rayBR.difference(radius));
				if (diff > 0) {
					rayTL.subtract(diff);
				}
				this.iconPos.x = rayTL.second.x;
				this.iconPos.y = rayTL.second.y;
			} else {
				this.iconPos.x = MathUtil.clamp(iconPos.x, mapX, (mapX + mapW) - size);
				this.iconPos.y = MathUtil.clamp(iconPos.y, mapY, (mapY + mapH) - size);
			}
			this.lastRotation = (float) rotation;
			this.lastMapX = mapX;
			this.lastMapY = mapY;
			this.lastX = x;
			this.lastY = y;
		}
	}

	private void correctRotation(int centerX, int centerY, double rotation) {
		double angle = Math.toRadians(-rotation);
		double posX = centerX + (iconPos.x - centerX) * Math.cos(angle) - (iconPos.y - centerY) * Math.sin(angle);
		double posY = centerY + (iconPos.y - centerY) * Math.cos(angle) + (iconPos.x - centerX) * Math.sin(angle);

		this.iconPos.x = posX;
		this.iconPos.y = posY;
	}

	private void applyOffset(double offX, double offY, double rotation) {
		double angle = Math.toRadians(-rotation);
		this.offX = (float) (offX * Math.cos(angle) - offY * Math.sin(angle));
		this.offY = (float) (offY * Math.cos(angle) + offX * Math.sin(angle));
	}

	public boolean isHidden() {
		return this.waypoint.hidden;
	}
}
