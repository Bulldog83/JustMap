package ru.bulldog.justmap.client.render;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.pipeline.TextureTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.map.ChunkGrid;
import ru.bulldog.justmap.map.data.RegionData;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.render.ExtendedFramebuffer;
import ru.bulldog.justmap.util.render.GLC;
import ru.bulldog.justmap.util.render.RenderUtil;

import java.util.List;

public class BufferedRenderer extends MapRenderer {
	
	private RenderTarget primaryFramebuffer;
	private RenderTarget secondaryFramebuffer;
	private boolean triedFBO = false;
	private boolean loadedFBO = false;
	
	public BufferedRenderer(Minimap map) {
		super(map);
	}
	
	public void loadFrameBuffers() {
		if (!ExtendedFramebuffer.canUseFramebuffer()) {
			JustMap.LOGGER.warning("FBO not supported! Using fast minimap render.");
		} else {
			double scale = minecraft.getWindow().getGuiScale();
			int scaledW = (int) (imgW * scale);
			int scaledH = (int) (imgH * scale);
			this.primaryFramebuffer = new TextureTarget(scaledW, scaledH, false, Minecraft.ON_OSX);
			this.secondaryFramebuffer = new TextureTarget(scaledW, scaledH, false, Minecraft.ON_OSX);
			this.loadedFBO = (primaryFramebuffer.frameBufferId != -1 && secondaryFramebuffer.frameBufferId != -1);
		}
		this.triedFBO = true;
	}

	@Override
	protected void render(PoseStack matrices, double scale) {
		MultiBufferSource.BufferSource consumerProvider = minecraft.renderBuffers().bufferSource();
		
		int scaledW = (int) (imgW * scale);
		int scaledH = (int) (imgH * scale);
		boolean isMac = Minecraft.ON_OSX;
		if (paramsUpdated) {
			resize(scaledW, scaledH, isMac);
			if (ClientSettings.showGrid) {
				if (chunkGrid == null) {
					chunkGrid = new ChunkGrid(lastX, lastZ, 0, 0, imgW, imgH, mapScale);
				} else {
					chunkGrid.updateRange(0, 0, imgW, imgH, mapScale);
					chunkGrid.updateGrid();
				}
			}
			paramsUpdated = false;
		}

		matrices.pushPose();
		primaryFramebuffer.bindWrite(true);
		RenderSystem.clear(GLC.GL_COLOR_OR_DEPTH_BUFFER_BIT, isMac);
		RenderSystem.enableTexture();
		RenderSystem.backupProjectionMatrix();
		Matrix4f orthographic = Matrix4f.orthographic(0.0F, scaledW, scaledH, 0, 1000, 3000);
		RenderSystem.setProjectionMatrix(orthographic);
		matrices.pushPose();
		matrices.setIdentity();
		RenderSystem.applyModelViewMatrix();
		matrices.pushPose();
		matrices.setIdentity();
		matrices.translate(0.0F, 0.0F, -2000.0F);
		matrices.scale((float) scale, (float) scale, 1.0F);
		RenderSystem.enableBlend();
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		drawMap();
		if (ClientSettings.showGrid) {
			drawGrid();
		}
		if (!mapRotation) {
			drawEntities(matrices, consumerProvider);
		}
		primaryFramebuffer.unbindWrite();
		matrices.popPose();
		
		secondaryFramebuffer.bindWrite(false);
		RenderSystem.clear(GLC.GL_COLOR_OR_DEPTH_BUFFER_BIT, isMac);
		matrices.pushPose();
		if (mapRotation) {
			float shiftX = scaledW / 2.0F;
			float shiftY = scaledH / 2.0F;
			matrices.translate(shiftX, shiftY, 0.0);
			matrices.mulPose(Vector3f.ZP.rotation(180.0F - rotation));
			matrices.translate(-shiftX, -shiftY, 0.0);
		}
		matrices.translate(-offX * scale, -offY * scale, 0.0);
		primaryFramebuffer.bindRead();
		RenderUtil.startDraw();
		BufferBuilder buffer = RenderUtil.getBuffer();
		buffer.vertex(0.0, scaledH, 0.0).uv(0.0F, 0.0F).endVertex();
		buffer.vertex(scaledW, scaledH, 0.0).uv(1.0F, 0.0F).endVertex();
		buffer.vertex(scaledW, 0.0, 0.0).uv(1.0F, 1.0F).endVertex();
		buffer.vertex(0.0, 0.0, 0.0).uv(0.0F, 1.0F).endVertex();
		RenderUtil.endDraw();
		if (mapRotation) {
			matrices.pushPose();
			matrices.scale((float) scale, (float) scale, 1.0F);
			drawEntities(matrices, consumerProvider);
			matrices.popPose();
		}
		matrices.popPose();
		secondaryFramebuffer.unbindWrite();
		RenderSystem.restoreProjectionMatrix();
		matrices.popPose();
		RenderSystem.applyModelViewMatrix();
		matrices.popPose();
		
		RenderTarget minecraftFramebuffer = minecraft.getMainRenderTarget();
		int fbuffW = minecraftFramebuffer.viewWidth;
		int fbuffH = minecraftFramebuffer.viewHeight;
		int scissX = (int) (mapX * scale);
		int scissY = (int) (fbuffH - (mapY + mapHeight) * scale);
		int scissW = (int) (mapWidth * scale);
		int scissH = (int) (mapHeight * scale);
		RenderUtil.enableScissor();
		RenderUtil.applyScissor(scissX, scissY, scissW, scissH);
		minecraftFramebuffer.bindWrite(false);
		RenderSystem.viewport(0, 0, fbuffW, fbuffH);
		if (Minimap.isRound()) {
			RenderSystem.enableBlend();
			RenderSystem.colorMask(false, false, false, true);
			RenderSystem.clearColor(0.0F, 0.0F, 0.0F, 0.0F);
			RenderSystem.clear(GLC.GL_COLOR_BUFFER_BIT, false);
			RenderSystem.colorMask(true, true, true, true);
			RenderUtil.bindTexture(roundMask);
			RenderUtil.drawQuad(mapX, mapY, mapWidth, mapHeight);
			RenderSystem.blendFunc(GLC.GL_DST_ALPHA, GLC.GL_ONE_MINUS_DST_ALPHA);
		}
		secondaryFramebuffer.bindRead();
		RenderUtil.startDraw();
		buffer = RenderUtil.getBuffer();
		buffer.vertex(imgX, imgY + imgH, 0.0).uv(0.0F, 0.0F).endVertex();
		buffer.vertex(imgX + imgW, imgY + imgH, 0.0).uv(1.0F, 0.0F).endVertex();
		buffer.vertex(imgX + imgW, imgY, 0.0).uv(1.0F, 1.0F).endVertex();
		buffer.vertex(imgX, imgY, 0.0).uv(0.0F, 1.0F).endVertex();
		RenderUtil.endDraw();
		List<WaypointIcon> drawableWaypoints = minimap.getWaypoints(playerPos, centerX, centerY);
		for (WaypointIcon icon : drawableWaypoints) {
			icon.draw(matrices, consumerProvider, mapX, mapY, mapWidth, mapHeight, offX, offY, rotation);
		}
		consumerProvider.endBatch();
		RenderUtil.disableScissor();
	}
	
	private void drawMap() {
		int cornerX = lastX - scaledW / 2;
		int cornerZ = lastZ - scaledH / 2;
		
		BlockPos.MutableBlockPos currentPos = new BlockPos.MutableBlockPos();
		
		int picX = 0;
		while(picX < scaledW) {
			int texW = 512;
			if (picX + texW > scaledW) texW = scaledW - picX;
			
			int picY = 0;
			int cX = cornerX + picX;
			while (picY < scaledH) {
				int texH = 512;
				if (picY + texH > scaledH) texH = scaledH - picY;
				
				int cZ = cornerZ + picY;
				RegionData region = worldData.getRegion(minimap, currentPos.set(cX, 0, cZ));
				region.swapLayer(minimap.getLayer(), minimap.getLevel());
				
				int texX = cX - (region.getX() << 9);
				int texY = cZ - (region.getZ() << 9);
				if (texX + texW >= 512) texW = 512 - texX;
				if (texY + texH >= 512) texH = 512 - texY;
				
				double scX = (double) picX / mapScale;
				double scY = (double) picY / mapScale;
				double scW = (double) texW / mapScale;
				double scH = (double) texH / mapScale;
				
				region.draw(scX, scY, scW, scH, texX, texY, texW, texH);
				
				picY += texH > 0 ? texH : 512;
			}
			
			picX += texW > 0 ? texW : 512;
		}
	}
	
	private void drawGrid() {
		if (playerMoved) {
			chunkGrid.updateCenter(lastX, lastZ);
			chunkGrid.updateGrid();
			playerMoved = false;
		}
		chunkGrid.draw();
	}
	
	private void drawEntities(PoseStack matrices, MultiBufferSource.BufferSource consumerProvider) {
		float halfW = imgW / 2.0F;
		float halfH = imgH / 2.0F;
		int iconX = imgW - mapWidth;
		int iconY = imgH - mapHeight;
		List<MapIcon<?>> drawableEntities = minimap.getDrawableIcons(lastX, lastZ, halfW, halfH, delta);
		for (MapIcon<?> icon : drawableEntities) {
			icon.draw(matrices, consumerProvider, iconX, iconY, mapWidth, mapHeight, rotation);
			consumerProvider.endBatch();
		}
	}
	
	public void resize(int width, int height, boolean isMac) {
		this.primaryFramebuffer.resize(width, height, isMac);
		this.secondaryFramebuffer.resize(width, height, isMac);
	}
	
	public void deleteFramebuffers() {
		this.primaryFramebuffer.destroyBuffers();
		this.secondaryFramebuffer.destroyBuffers();
		this.setLoadedFBO(false);
		this.triedFBO = false;
	}
	
	public boolean isFBOLoaded() {
		return this.loadedFBO;
	}
	
	public void setLoadedFBO(boolean loadedFBO) {
		this.loadedFBO = loadedFBO;
	}
	
	public boolean isFBOTried() {
		return this.triedFBO;
	}
}
