package ru.bulldog.justmap.client.render;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.data.WorldManager;
import ru.bulldog.justmap.map.waypoint.Waypoint;
import ru.bulldog.justmap.map.waypoint.WaypointKeeper;
import ru.bulldog.justmap.map.waypoint.Waypoint.Icon;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.math.MathUtil;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Matrix3f;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.util.math.Vec3d;

import java.util.List;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;

@Environment(EnvType.CLIENT)
public class WaypointRenderer {
	private static WaypointRenderer renderer;
	
	private final static Identifier BEAM_TEX = new Identifier("textures/entity/beacon_beam.png");
	private final static Tessellator tessellator = Tessellator.getInstance();
	private final static BufferBuilder builder = tessellator.getBuffer();
	
	private static void initBuffer() {
		builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_COLOR_TEXTURE_OVERLAY_LIGHT_NORMAL);
	}
	
	public static void renderHUD(float delta, float fov) {
		if (!ClientParams.showWaypoints || !ClientParams.waypointsTracking) return;
		if (renderer == null) {
			renderer = new WaypointRenderer();
		}
		MinecraftClient minecraft = DataUtil.getMinecraft();
		if (minecraft.world == null || minecraft.player == null || minecraft.currentScreen != null) {
			return;
		}
	
		List<Waypoint> wayPoints = WaypointKeeper.getInstance().getWaypoints(WorldManager.getWorldKey(), true);
		for (Waypoint wp : wayPoints) {
			int dist = (int) MathUtil.getDistance(wp.pos, minecraft.player.getBlockPos(), false);
			if (wp.tracking && dist <= wp.showRange) {
				renderer.renderHUD(wp, minecraft, delta, fov, dist);
			}
		}
	}
	
	private void renderHUD(Waypoint waypoint, MinecraftClient client, float delta, float fov, int dist) {
		int wpX = waypoint.pos.getX();
		int wpZ = waypoint.pos.getZ();
		
		Icon icon = waypoint.getIcon();
		
		int size = icon != null ? icon.getWidth() : 18;
		int screenWidth = client.getWindow().getScaledWidth();
		
		double dx = client.player.getX() - wpX;
		double dy = wpZ - client.player.getZ();		
		double wfi = correctAngle((float) (Math.atan2(dx, dy) * (180 / Math.PI)));		
		double pfi = correctAngle(client.player.getYaw(delta) % 360);		
		double a0 = pfi - fov / 2;
		double a1 = pfi + fov / 2;		
		double ax = correctAngle((float) (2 * pfi - wfi));		
		double scale = (MathUtil.clamp(ax, a0, a1) - a0) / fov;
		
		int x = (int) Math.round(MathUtil.clamp((screenWidth - screenWidth * scale) - size / 2, 0, screenWidth - size));
		int y = ClientParams.positionOffset;
		
		if (icon != null) {
			icon.draw(x, y);
		} else {
			RenderUtil.drawDiamond(x, y, size, size, waypoint.color);
		}
		RenderUtil.drawBoundedString((int) dist + "m", x + size / 2, y + size + 2, 0, screenWidth, Colors.WHITE);
	}
	
	public static void renderWaypoints(MatrixStack matrixStack, MinecraftClient client, Camera camera, float tickDelta) {
		if (!ClientParams.showWaypoints || !ClientParams.waypointsWorldRender) return;
		if (renderer == null) {
			renderer = new WaypointRenderer();
		}
		
		long time = client.player.world.getTime();
		float tick = (float) Math.floorMod(time, 125L) + tickDelta;
		
		BlockPos playerPos = client.player.getBlockPos();
		
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		RenderSystem.enableAlphaTest();
		RenderSystem.enableDepthTest();
		RenderSystem.enableTexture();
		RenderSystem.depthMask(false);
		
		List<Waypoint> wayPoints = WaypointKeeper.getInstance().getWaypoints(WorldManager.getWorldKey(), true);
		for (Waypoint wp : wayPoints) {
			int dist = (int) MathUtil.getDistance(wp.pos, playerPos, false);
			if (wp.render && dist > ClientParams.minRenderDist && dist < ClientParams.maxRenderDist) {
				renderer.renderWaypoint(matrixStack, wp, client, camera, tick, dist);
			}
		}
		
		RenderSystem.depthMask(true);
	}
	
	private void renderWaypoint(MatrixStack matrixStack, Waypoint waypoint, MinecraftClient client, Camera camera, float tick, int dist) {
		int wpX = waypoint.pos.getX();
		int wpY = waypoint.pos.getY();
		int wpZ = waypoint.pos.getZ();
		
		Vec3d vec3d = camera.getPos();   	
		
		double camX = vec3d.getX();
		double camY = vec3d.getY();
		double camZ = vec3d.getZ();
		
		float[] colors = ColorUtil.toFloatArray(waypoint.color);
		float alpha = MathUtil.clamp(0.125F * ((float) dist / 10), 0.11F, 0.275F);
		
		matrixStack.push();
		matrixStack.translate((double) wpX - camX, (double) wpY - camY, (double) wpZ - camZ);
		matrixStack.translate(0.5, 0.5, 0.5);
		if (ClientParams.renderLightBeam) {
			this.renderLightBeam(matrixStack, tick, -wpY, 1024 - wpY, colors, alpha, 0.15F, 0.2F);
		}
		if (ClientParams.renderMarkers) {
			matrixStack.push();
			matrixStack.translate(0.0, 1.0, 0.0);
			if (ClientParams.renderAnimation) {
				double swing = 0.25 * Math.sin((tick * 2.25 - 45.0) / 15.0);
				matrixStack.translate(0.0, swing, 0.0);
			}
			matrixStack.multiply(camera.getRotation());
   	 		matrixStack.multiply(Vector3f.POSITIVE_Y.getDegreesQuaternion(180.0F));
   	 		matrixStack.multiply(Vector3f.POSITIVE_Z.getDegreesQuaternion(-90.0F));
   	 		
   	 		alpha = MathUtil.clamp(alpha * 3, 0.0F, 1.0F);
   	 		
   	 		initBuffer();
   	 		waypoint.getIcon().bindTexture();
   	 		this.renderIcon(matrixStack, builder, colors, alpha, waypoint.getIcon().getWidth());
   	 		tessellator.draw();
   	 		
   	 		matrixStack.pop();
		}
		matrixStack.pop();
	}
	
	private void renderIcon(MatrixStack matrixStack, VertexConsumer vertexConsumer, float[] colors, float alpha, int size) {
		MatrixStack.Entry entry = matrixStack.peek();
		Matrix4f matrix4f = entry.getModel();
		Matrix3f matrix3f = entry.getNormal();

		this.addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], alpha, -0.5F, -0.5F, 0.0F, 0.0F, 0.0F);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], alpha, -0.5F, 0.5F, 0.0F, 0.0F, 1.0F);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], alpha, 0.5F, 0.5F, 0.0F, 1.0F, 1.0F);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, colors[0], colors[1], colors[2], alpha, 0.5F, -0.5F, 0.0F, 1.0F, 0.0F);
	}
	
	private void renderLightBeam(MatrixStack matrixStack, float tick, int i, int j, float[] colors, float alpha, float h, float k) {
		int m = i + j;
		
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
		
		initBuffer();
		
		RenderUtil.bindTexture(BEAM_TEX);
		this.renderBeam(matrixStack, builder, red, green, blue, alpha, i, m, 0.0F, h, h, 0.0F, aj, 0.0F, 0.0F, aa, 0.0F, 1.0F, aq, ap);
		matrixStack.pop();
		
		af = -k;
		float ag = -k;
		ai = -k;
		aj = -k;
		ap = -1.0F + p;
		aq = (float) j + ap;
		this.renderBeam(matrixStack, builder, red, green, blue, alpha, i, m, af, ag, k, ai, aj, k, k, k, 0.0F, 1.0F, aq, ap);
		
		tessellator.draw();
	}

	private void renderBeam(MatrixStack matrixStack, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s, float t, float u, float v, float w) {
		MatrixStack.Entry entry = matrixStack.peek();
		Matrix4f matrix4f = entry.getModel();
		Matrix3f matrix3f = entry.getNormal();		
		this.renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, l, m, n, o, t, u, v, w);
		this.renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, r, s, p, q, t, u, v, w);
		this.renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, n, o, r, s, t, u, v, w);
		this.renderBeam(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, k, p, q, l, m, t, u, v, w);
	}

	private void renderBeam(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, int j, int k, float l, float m, float n, float o, float p, float q, float r, float s) {
		this.addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, k, l, m, q, r);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, l, m, q, s);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, j, n, o, p, s);
		this.addVertex(matrix4f, matrix3f, vertexConsumer, red, green, blue, alpha, k, n, o, p, r);
	}

	private void addVertex(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer vertexConsumer, float red, float green, float blue, float alpha, float y, float x, float l, float m, float n) {
		vertexConsumer.vertex(matrix4f, x, y, l).color(red, green, blue, alpha).texture(m, n).overlay(OverlayTexture.DEFAULT_UV).light(Colors.LIGHT).normal(matrix3f, 0.0F, 1.0F, 0.0F).next();
	}
	 
	private double correctAngle(float angle) {
		return angle < 0 ? angle += 360.0D : angle >= 360.0D ? angle -= 360.0D : angle;
	}
}
