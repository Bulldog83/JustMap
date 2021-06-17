package ru.bulldog.justmap.client.render;

import com.mojang.blaze3d.platform.GlStateManager;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.data.WorldManager;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.util.colors.ColorUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.MathUtil;
import ru.bulldog.justmap.util.render.RenderUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;

@Environment(EnvType.CLIENT)
public class WaypointRenderer {
	private static final WaypointRenderer renderer = new WaypointRenderer();
	private final static ResourceLocation BEAM_TEX = new ResourceLocation("textures/entity/beacon_beam.png");
	private final static Minecraft minecraft = Minecraft.getInstance();
	
	public static void renderHUD(float delta, float fov) {
		if (!ClientSettings.showWaypoints || !ClientSettings.waypointsTracking) return;
		if (minecraft.level == null || minecraft.player == null || minecraft.screen != null) {
			return;
		}
	
		List<Waypoint> wayPoints = WaypointKeeper.getInstance().getWaypoints(WorldManager.getWorldKey(), true);
		for (Waypoint wp : wayPoints) {
			int dist = (int) MathUtil.getDistance(wp.pos, minecraft.player.blockPosition(), false);
			if (wp.tracking && dist <= wp.showRange) {
				renderer.renderHUD(wp, delta, fov, dist);
			}
		}
	}
	
	private void renderHUD(Waypoint waypoint, float delta, float fov, int dist) {
		int wpX = waypoint.pos.getX();
		int wpZ = waypoint.pos.getZ();
		
		Icon icon = waypoint.getIcon();
		
		int size = icon != null ? icon.getWidth() : 18;
		int screenWidth = minecraft.getWindow().getGuiScaledWidth();
		
		double dx = minecraft.player.getX() - wpX;
		double dy = wpZ - WaypointRenderer.minecraft.player.getZ();
		double wfi = correctAngle((float) (Math.atan2(dx, dy) * (180 / Math.PI)));		
		double pfi = correctAngle(WaypointRenderer.minecraft.player.getViewYRot(delta) % 360);
		double a0 = pfi - fov / 2;
		double a1 = pfi + fov / 2;		
		double ax = correctAngle((float) (2 * pfi - wfi));		
		double scale = (MathUtil.clamp(ax, a0, a1) - a0) / fov;
		
		int x = (int) Math.round(MathUtil.clamp((screenWidth - screenWidth * scale) - size / 2, 0, screenWidth - size));
		int y = ClientSettings.positionOffset;
		
		if (icon != null) {
			icon.draw(x, y);
		} else {
			RenderUtil.drawDiamond(x, y, size, size, waypoint.color);
		}
		RenderUtil.drawBoundedString((int) dist + "m", x + size / 2, y + size + 2, 0, screenWidth, Colors.WHITE);
	}
	
	public static void renderWaypoints(PoseStack matrixStack, Camera camera, float tickDelta) {
		if (minecraft == null) return;
		if (!ClientSettings.showWaypoints || !ClientSettings.waypointsWorldRender) return;
		
		long time = minecraft.player.level.getGameTime();
		float tick = (float) Math.floorMod(time, 125L) + tickDelta;
		
		BlockPos playerPos = minecraft.player.blockPosition();
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableCull();
		RenderSystem.enableDepthTest();
		RenderSystem.enableTexture();
		RenderSystem.depthMask(false);
		
		MultiBufferSource.BufferSource consumerProvider = minecraft.renderBuffers().bufferSource();
		List<Waypoint> wayPoints = WaypointKeeper.getInstance().getWaypoints(WorldManager.getWorldKey(), true);
		for (Waypoint wp : wayPoints) {
			int dist = (int) MathUtil.getDistance(wp.pos, playerPos, false);
			if (wp.render && dist >= ClientSettings.minRenderDist && dist <= wp.showRange) {
				renderer.renderWaypoint(matrixStack, consumerProvider, wp, camera, tick, dist);
			}
		}
		consumerProvider.endBatch();
		
		RenderSystem.depthMask(true);
	}
	
	private void renderWaypoint(PoseStack matrixStack, MultiBufferSource consumerProvider, Waypoint waypoint, Camera camera, float tick, int dist) {
		int wpX = waypoint.pos.getX();
		int wpY = waypoint.pos.getY();
		int wpZ = waypoint.pos.getZ();
		
		Vec3 vec3d = camera.getPosition();   	
		
		double camX = vec3d.x();
		double camY = vec3d.y();
		double camZ = vec3d.z();
		
		float[] colors = ColorUtil.toFloatArray(waypoint.color);
		float alpha = MathUtil.clamp(0.125F * ((float) dist / 10), 0.11F, 0.275F);
		
		matrixStack.pushPose();
		matrixStack.translate((double) wpX - camX, (double) wpY - camY, (double) wpZ - camZ);
		matrixStack.translate(0.5, 0.5, 0.5);
		if (ClientSettings.renderLightBeam) {
			VertexConsumer vertexConsumer = consumerProvider.getBuffer(RenderType.beaconBeam(BEAM_TEX, true));
			this.renderLightBeam(matrixStack, vertexConsumer, tick, -wpY, 1024 - wpY, colors, alpha);
		}
		if (ClientSettings.renderMarkers) {
			matrixStack.pushPose();
			matrixStack.translate(0.0, 1.0, 0.0);
			if (ClientSettings.renderAnimation) {
				double swing = 0.25 * Math.sin((tick * 2.25 - 45.0) / 15.0);
				matrixStack.translate(0.0, swing, 0.0);
			}
			matrixStack.mulPose(camera.rotation());
   	 		matrixStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
   	 		matrixStack.mulPose(Vector3f.ZP.rotationDegrees(-90.0F));
   	 		
   	 		alpha = MathUtil.clamp(alpha * 3, 0.0F, 1.0F);
   	 		
   	 		ResourceLocation texture = waypoint.getIcon().getTexture();
   	 		VertexConsumer vertexConsumer = consumerProvider.getBuffer(RenderType.beaconBeam(texture, true));
   	 		renderIcon(matrixStack, vertexConsumer, colors, alpha);
   	 		matrixStack.popPose();
		}
		matrixStack.popPose();
	}
	
	private void renderIcon(PoseStack matrixStack, VertexConsumer vertexConsumer, float[] colors, float alpha) {
		PoseStack.Pose entry = matrixStack.last();
		Matrix4f matrix4f = entry.pose();
		Matrix3f matrix3f = entry.normal();

		this.addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], alpha, -0.5F, -0.5F, 0.0F, 0.0F, 0.0F);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], alpha, -0.5F, 0.5F, 0.0F, 0.0F, 1.0F);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], alpha, 0.5F, 0.5F, 0.0F, 1.0F, 1.0F);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], alpha, 0.5F, -0.5F, 0.0F, 1.0F, 0.0F);
	}
	
	private void renderLightBeam(PoseStack matrixStack, VertexConsumer vertexConsumer, float tick, int i, int j, float[] colors, float alpha) {
		int m = i + j;
		
		float o = j < 0 ? tick : -tick;
		float p = Mth.frac(o * 0.2F - (float) Mth.floor(o * 0.1F));
		float red = colors[0];
		float green = colors[1];
		float blue = colors[2];
		
		matrixStack.pushPose();
		matrixStack.mulPose(Vector3f.YP.rotationDegrees(tick * 2.25F - 45.0F));
		float aj = -(float) 0.15;
		float aa = -(float) 0.15;
		float ap = -1.0F + p;
		float aq = (float) j * (0.5F / (float) 0.15) + ap;
		
		this.renderBeam(matrixStack, vertexConsumer, red, green, blue, alpha, i, m, 0.0F, (float) 0.15, (float) 0.15, 0.0F, aj, 0.0F, 0.0F, aa, aq, ap);
		matrixStack.popPose();

		float af = -(float) 0.2;
		float ag = -(float) 0.2;
		float ai = -(float) 0.2;
		aj = -(float) 0.2;
		ap = -1.0F + p;
		aq = (float) j + ap;
		this.renderBeam(matrixStack, vertexConsumer, red, green, blue, alpha, i, m, af, ag, (float) 0.2, ai, aj, (float) 0.2, (float) 0.2, (float) 0.2, aq, ap);
	}

	private void renderBeam(PoseStack matrixStack, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s, float v, float w) {
		PoseStack.Pose entry = matrixStack.last();
		Matrix4f matrix4f = entry.pose();
		Matrix3f matrix3f = entry.normal();		
		this.renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, l, m, n, o, v, w);
		this.renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, r, s, p, q, v, w);
		this.renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, n, o, r, s, v, w);
		this.renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, p, q, l, m, v, w);
	}

	private void renderBeam(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int j, int k, float l, float m, float n, float o, float r, float s) {
		this.addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, k, l, m, 1.0F, r);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, l, m, 1.0F, s);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, n, o, 0.0F, s);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, k, n, o, 0.0F, r);
	}

	private void addVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, float y, float x, float l, float m, float n) {
		vertexConsumer.vertex(matrix4f, x, y, l).color(red, green, blue, alpha).uv(m, n).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(Colors.LIGHT).normal(matrix3f, 0.0F, 1.0F, 0.0F).endVertex();
	}
	 
	private double correctAngle(float angle) {
		return angle < 0 ? angle + 360.0D : angle >= 360.0D ? angle - 360.0D : angle;
	}
}
