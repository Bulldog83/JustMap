package ru.bulldog.justmap.client.render;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.Vec3f;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.ChunkGrid;
import ru.bulldog.justmap.map.data.MapRegion;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.render.GLC;
import ru.bulldog.justmap.util.render.RenderUtil;

public class FastMiniMapRenderer extends AbstractMiniMapRenderer {

	public FastMiniMapRenderer(Minimap map) {
		super(map);
	}

	protected void render(MatrixStack matrices, double scale) {
		Framebuffer minecraftFramebuffer = minecraft.getFramebuffer();
		int fbuffH = minecraftFramebuffer.viewportHeight;
		int scissX = (int) (mapX * scale);
		int scissY = (int) (fbuffH - (mapY + mapHeight) * scale);
		int scissW = (int) (mapWidth * scale);
		int scissH = (int) (mapHeight * scale);
		RenderUtil.enableScissor();
		RenderUtil.applyScissor(scissX, scissY, scissW, scissH);
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		if (Minimap.isRound()) {
			RenderSystem.colorMask(false, false, false, true);
			RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
			RenderSystem.clear(GLC.GL_COLOR_BUFFER_BIT, false);
			RenderSystem.colorMask(true, true, true, true);
			RenderUtil.bindTexture(roundMask);
			RenderUtil.startDraw();
			RenderUtil.addQuad(mapX, mapY, mapWidth, mapHeight);
			RenderUtil.endDraw();
			RenderSystem.blendFunc(GLC.GL_DST_ALPHA, GLC.GL_ONE_MINUS_DST_ALPHA);
		}
		matrices.push();
		if (mapRotation) {
			float moveX = mapX + mapWidth / 2.0F;
			float moveY = mapY + mapHeight / 2.0F;
			matrices.translate(moveX, moveY, 0.0);
			matrices.multiply(Vec3f.POSITIVE_Z.getDegreesQuaternion(-rotation + 180));
			matrices.translate(-moveX, -moveY, 0.0);
		}
		matrices.translate(-offX, -offY, 0.0);

		this.drawMap(matrices);
		if (ClientSettings.showGrid) {
			this.drawGrid();
		}

		VertexConsumerProvider.Immediate consumerProvider = minecraft.getBufferBuilders().getEntityVertexConsumers();
		List<MapIcon<?>> drawableEntities = minimap.getDrawableIcons(lastX, lastZ, centerX, centerY, delta);
		for (MapIcon<?> icon : drawableEntities) {
			icon.draw(matrices, consumerProvider, mapX, mapY, mapWidth, mapHeight, rotation);
		}
		consumerProvider.draw();

		matrices.pop();

		List<WaypointIcon> drawableWaypoints = minimap.getWaypoints(playerPos, centerX, centerY);
		for (WaypointIcon icon : drawableWaypoints) {
			icon.draw(matrices, consumerProvider, mapX, mapY, mapWidth, mapHeight, offX, offY, rotation);
		}
		consumerProvider.draw();
		RenderUtil.disableScissor();
	}

	private void drawMap(MatrixStack matrices) {
		int cornerX = lastX - scaledW / 2;
		int cornerZ = lastZ - scaledH / 2;

		int picX = 0;
		while (picX < scaledW) {
			int texW = 512;
			if (picX + texW > scaledW) texW = scaledW - picX;

			int picY = 0;
			int cX = cornerX + picX;
			while (picY < scaledH) {
				int texH = 512;
				if (picY + texH > scaledH) texH = scaledH - picY;

				int cZ = cornerZ + picY;
				MapRegion region = worldMapper.getMapRegion(minimap, cX, cZ);

				int texX = cX - (region.getPos().x << 9);
				int texY = cZ - (region.getPos().z << 9);
				if (texX + texW >= 512) texW = 512 - texX;
				if (texY + texH >= 512) texH = 512 - texY;

				double scX = (double) picX / mapScale;
				double scY = (double) picY / mapScale;
				double scW = (double) texW / mapScale;
				double scH = (double) texH / mapScale;

				region.drawLayer(matrices, minimap.getLayer(), minimap.getLevel(), imgX + scX, imgY + scY, scW, scH, texX, texY, texW, texH);

				picY += texH > 0 ? texH : 512;
			}

			picX += texW > 0 ? texW : 512;
		}
	}

	private void drawGrid() {
		if (paramsUpdated) {
			if (chunkGrid == null) {
				this.chunkGrid = new ChunkGrid(lastX, lastZ, imgX, imgY, imgW, imgH, mapScale);
			} else {
				this.chunkGrid.updateRange(imgX, imgY, imgW, imgH, mapScale);
				this.chunkGrid.updateGrid();
			}
			this.paramsUpdated = false;
		}
		if (playerMoved) {
			this.chunkGrid.updateCenter(lastX, lastZ);
			this.chunkGrid.updateGrid();
			this.playerMoved = false;
		}
		this.chunkGrid.draw();
	}
}
