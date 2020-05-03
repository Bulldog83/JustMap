package ru.bulldog.justmap.client.render;

import com.mojang.blaze3d.systems.RenderSystem;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;
import ru.bulldog.justmap.util.math.MathUtil;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import java.util.List;

@Environment(EnvType.CLIENT)
public class WaypointRenderer {
	private static WaypointRenderer renderer;
	
	private final static Identifier BEAM_TEX = new Identifier("textures/entity/beacon_beam.png");
	
	public static void renderHUD(float delta, float fov) {
		if (ClientParams.waypointsTracking) {
			if (renderer == null) {
				renderer = new WaypointRenderer();
			}
			MinecraftClient client = MinecraftClient.getInstance();
			if (client.world == null || client.player == null || client.currentScreen != null) {
				return;
			}
		
			List<Waypoint> wayPoints = WaypointKeeper.getInstance().getWaypoints(client.world.dimension.getType().getRawId(), true);
			for (Waypoint wp : wayPoints) {
				int dist = (int) MathUtil.getDistance(wp.pos, client.player.getBlockPos(), false);
				if (wp.tracking && dist < wp.showRange) {
					renderer.renderHUD(wp, client, delta, fov, dist);
				}
			}
		}
	}
	
	private void renderHUD(Waypoint waypoint, MinecraftClient client, float delta, float fov, int dist) {
		int wpX = waypoint.pos.getX();
		int wpZ = waypoint.pos.getZ();		
		
		Icon icon = waypoint.getIcon();
		
		int size = icon != null ? icon.getWidth() : 18;
		int screenWidth = client.getWindow().getScaledWidth();
		
		int dx = (int) client.player.getX() - wpX;
		int dy = wpZ - (int) client.player.getZ();
		
		int wpFi = correctAngle((int) (Math.atan2(dx, dy) * (180 / Math.PI)));		
		int pFi = correctAngle((int) (client.player.getYaw(delta) % 360));
		
		int a0 = pFi - (int) fov / 2;
		int a1 = pFi + (int) fov / 2;		
		int ax = correctAngle(2 * pFi - wpFi);
		
		float scale = (MathUtil.clamp(ax, a0, a1) - a0) / fov;
		
		int x = (int) MathUtil.clamp((screenWidth - screenWidth * scale) - size / 2, 0, screenWidth - size);
		int y = ClientParams.positionOffset;
		
		if (icon != null) {
			icon.draw(x, y);
		} else {
			RenderSystem.pushMatrix();
			DrawHelper.drawDiamond(x, y, size, size, waypoint.color);
			RenderSystem.popMatrix();
		}
		DrawHelper.drawBoundedString(new MatrixStack(), client.textRenderer, new LiteralText(dist + "m"), x + size / 2, y + size + 2, 0, screenWidth, Colors.WHITE);
	}
	
	public static void renderWaypoint(MatrixStack matrixStack, MinecraftClient client, Camera camera, float tickDelta) {
		if (ClientParams.waypointsWorldRender) {
			if (renderer == null) {
				renderer = new WaypointRenderer();
			}
			
			long time = client.player.world.getTime();
			
			float tick = (float) Math.floorMod(time, 125L) + tickDelta;
			
			VertexConsumerProvider.Immediate immediate = client.getBufferBuilders().getEntityVertexConsumers();
			
			BlockPos playerPos = client.player.getBlockPos();
			
			List<Waypoint> wayPoints = WaypointKeeper.getInstance().getWaypoints(client.world.dimension.getType().getRawId(), true);			
			for (Waypoint wp : wayPoints) {
				int dist = (int) MathUtil.getDistance(wp.pos, playerPos, false);
				if (wp.render && dist > ClientParams.minRenderDist && dist < ClientParams.maxRenderDist) {
					renderer.renderWaypoint(matrixStack, immediate, wp, client, camera, tick, dist);
				}
			}			
		}
	}
	
	private void renderWaypoint(MatrixStack matrixStack, VertexConsumerProvider immediate, Waypoint waypoint, MinecraftClient client, Camera camera, float tick, int dist) {
		int wpX = waypoint.pos.getX();
		int wpY = waypoint.pos.getY();
		int wpZ = waypoint.pos.getZ();
		
		Vec3d vec3d = camera.getPos();   	
		
		double camX = vec3d.getX();
		double camY = vec3d.getY();
		double camZ = vec3d.getZ();
		
		float[] colors = ColorUtil.toFloatArray(waypoint.color);
		float alpha = MathUtil.clamp(0.125F * ((float) dist / 10), 0.025F, 0.275F);
		
		matrixStack.push();
		matrixStack.translate((double) wpX - camX, (double) wpY - camY, (double) wpZ - camZ);
		matrixStack.translate(0.5D, 1.5D, 0.5D);
		if (ClientParams.renderLightBeam) {
			renderLightBeam(matrixStack, immediate, waypoint, BEAM_TEX, tick, -wpY, 512 - wpY, colors, alpha, 0.15F, 0.2F);
		}
		
		if (ClientParams.renderAnimation) {
			double swing = 0.25 * Math.sin((tick * 2.25 - 45.0) / 15.0);		
			matrixStack.translate(0.0D, swing, 0.0D);
		}
		
		if (ClientParams.renderMarkers) {
			matrixStack.multiply(camera.getRotation());
   	 		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
   	 		renderIcon(matrixStack, immediate.getBuffer(RenderLayer.getEntityTranslucent(waypoint.getIcon().getTexture())), colors, alpha, waypoint.getIcon().getWidth());
		}
		matrixStack.pop();
	}
	
	private void renderLightBeam(MatrixStack matrixStack, VertexConsumerProvider vertexConsumerProvider, Waypoint waypoint, Identifier identifier, float tick, int i, int j, float[] colors, float alpha, float h, float k) {
		int m = i + j;
		matrixStack.push();
		float o = j < 0 ? tick : -tick;
		float p = MathHelper.fractionalPart(o * 0.2F - (float) MathHelper.floor(o * 0.1F));
		float red = colors[0];
		float green = colors[1];
		float blue = colors[2];
		matrixStack.push();
		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(tick * 2.25F - 45.0F));
		float af = 0.0F;
		float ai = 0.0F;
		float aj = -h;
		float aa = -h;
		float ap = -1.0F + p;
		float aq = (float) j * (0.5F / h) + ap;
		renderBeam(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getBeaconBeam(identifier, true)), red, green, blue, alpha, i, m, 0.0F, h, h, 0.0F, aj, 0.0F, 0.0F, aa, 0.0F, 1.0F, aq, ap);
		matrixStack.pop();		
		af = -k;
		float ag = -k;
		ai = -k;
		aj = -k;
		ap = -1.0F + p;
		aq = (float) j + ap;
		renderBeam(matrixStack, vertexConsumerProvider.getBuffer(RenderLayer.getBeaconBeam(identifier, true)), red, green, blue, alpha, i, m, af, ag, k, ai, aj, k, k, k, 0.0F, 1.0F, aq, ap);
		matrixStack.pop();
	 }
	
	 private void renderIcon(MatrixStack matrixStack, VertexConsumer vertexConsumer, float[] colors, float alpha, int size) {
		 MatrixStack.Entry entry = matrixStack.peek();
		 Matrix4f matrix4f = entry.getModel();
		 Matrix3f matrix3f = entry.getNormal();
		 
		 float h = (size % 4 * 16) / 16.0F;
		 float l = (size / 4 * 16) / 16.0F;
		 float k = (size % 4 * 16 + 16) / 16.0F;
		 float m = (size / 4 * 16 + 16) / 16.0F;		 
		 
		 float a = MathUtil.clamp(alpha * 5, 0.0F, 1.0F);
		 
		 matrixStack.push();		
		 
		 addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], a, -0.5F, -0.5F, 0, l, k);
		 addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], a, -0.5F, 0.5F, 0, m, k);
		 addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], a, 0.5F, 0.5F, 0, m, h);
		 addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], a, 0.5F, -0.5F, 0, l, h);
		 
		 matrixStack.pop();
	 }

	 private void renderBeam(MatrixStack matrixStack, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u, float v, float w) {
		MatrixStack.Entry entry = matrixStack.peek();
		Matrix4f matrix4f = entry.getModel();
		Matrix3f matrix3f = entry.getNormal();		
		renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, l, m, n, o, t, u, v, w);
		renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, r, s, p, q, t, u, v, w);
		renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, n, o, r, s, t, u, v, w);
		renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, p, q, l, m, t, u, v, w);
	 }

	 private void renderBeam(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s) {
		addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, k, l, m, q, r);
		addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, l, m, q, s);
		addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, n, o, p, s);
		addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, k, n, o, p, r);
	 }

	 private void addVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, float y, float x, float l, float m, float n) {
		vertexConsumer.vertex(matrix4f, x, y, l).color(red, green, blue, alpha).texture(m, n).overlay(OverlayTexture.DEFAULT_UV).light(15728880).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
	 }
	 
	 private int correctAngle(int fi) {
		 return fi < -180 ? fi += 360 : fi > 180 ? fi -= 360 : fi;
	 }
}
