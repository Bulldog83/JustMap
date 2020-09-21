package ru.bulldog.justmap.client.render;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

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

public class BufferedRenderer extends MapRenderer {
	
	private Framebuffer primaryFramebuffer;
	private Framebuffer secondaryFramebuffer;
	private boolean triedFBO = false;
	private boolean loadedFBO = false;
	
	public BufferedRenderer(Minimap map) {
		super(map);
	}
	
	public void loadFrameBuffers() {
		if (!ExtendedFramebuffer.canUseFramebuffer()) {
			JustMap.LOGGER.warning("FBO not supported! Using fast minimap render.");
		} else {
			double scale = minecraft.getWindow().getScaleFactor();
			int scaledW = (int) (imgW * scale);
			int scaledH = (int) (imgH * scale);
			this.primaryFramebuffer = new ExtendedFramebuffer(scaledW, scaledH, false);
			this.secondaryFramebuffer = new ExtendedFramebuffer(scaledW, scaledH, false);
			this.loadedFBO = (this.primaryFramebuffer.fbo != -1 && this.secondaryFramebuffer.fbo != -1);
		}
		this.triedFBO = true;
	}

	@Override
	protected void render(MatrixStack matrices, double scale) {
		VertexConsumerProvider.Immediate consumerProvider = minecraft.getBufferBuilders().getEntityVertexConsumers();
		
		int scaledW = (int) (imgW * scale);
		int scaledH = (int) (imgH * scale);
		boolean isMac = MinecraftClient.IS_SYSTEM_MAC;
		if (paramsUpdated) {
			this.resize(scaledW, scaledH, isMac);
			if (ClientSettings.showGrid) {
				if (chunkGrid == null) {
					this.chunkGrid = new ChunkGrid(lastX, lastZ, 0, 0, imgW, imgH, mapScale);
				} else {
					this.chunkGrid.updateRange(0, 0, imgW, imgH, mapScale);
					this.chunkGrid.updateGrid();
				}
			}
			this.paramsUpdated = false;
		}

		RenderSystem.pushMatrix();
		this.primaryFramebuffer.beginWrite(true);
		RenderSystem.clear(GLC.GL_COLOR_OR_DEPTH_BUFFER_BIT, isMac);
		RenderSystem.enableTexture();
		RenderSystem.matrixMode(GLC.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.ortho(0.0, scaledW, scaledH, 0.0, 1000.0, 3000.0);
		RenderSystem.matrixMode(GLC.GL_MODELVIEW);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
		RenderSystem.scaled(scale, scale, 1.0);
		RenderSystem.enableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawMap();
		if (ClientSettings.showGrid) {
			this.drawGrid();
		}
		if (!mapRotation) {
			this.drawEntities(matrices, consumerProvider);
		}
		this.primaryFramebuffer.endWrite();
		RenderSystem.popMatrix();
		
		this.secondaryFramebuffer.beginWrite(false);
		RenderSystem.clear(GLC.GL_COLOR_OR_DEPTH_BUFFER_BIT, isMac);
		RenderSystem.pushMatrix();
		if (mapRotation) {
			float shiftX = scaledW / 2.0F;
			float shiftY = scaledH / 2.0F;
			RenderSystem.translatef(shiftX, shiftY, 0.0F);
			RenderSystem.rotatef(180.0F - rotation, 0.0F, 0.0F, 1.0F);
			RenderSystem.translatef(-shiftX, -shiftY, 0.0F);
		}
		RenderSystem.translated(-offX * scale, -offY * scale, 0.0);
		this.primaryFramebuffer.beginRead();
		RenderUtil.startDraw();
		BufferBuilder buffer = RenderUtil.getBuffer();
		buffer.vertex(0.0, scaledH, 0.0).texture(0.0F, 0.0F).next();
		buffer.vertex(scaledW, scaledH, 0.0).texture(1.0F, 0.0F).next();
		buffer.vertex(scaledW, 0.0, 0.0).texture(1.0F, 1.0F).next();
		buffer.vertex(0.0, 0.0, 0.0).texture(0.0F, 1.0F).next();
		RenderUtil.endDraw();
		if (mapRotation) {
			RenderSystem.pushMatrix();
			RenderSystem.scaled(scale, scale, 1.0);
			this.drawEntities(matrices, consumerProvider);
			RenderSystem.popMatrix();
		}
		RenderSystem.popMatrix();
		this.secondaryFramebuffer.endWrite();
		RenderSystem.matrixMode(GLC.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GLC.GL_MODELVIEW);
		RenderSystem.popMatrix();
		
		Framebuffer minecraftFramebuffer = minecraft.getFramebuffer();
		int fbuffW = minecraftFramebuffer.viewportWidth;
		int fbuffH = minecraftFramebuffer.viewportHeight;
		int scissX = (int) (mapX * scale);
		int scissY = (int) (fbuffH - (mapY + mapHeight) * scale);
		int scissW = (int) (mapWidth * scale);
		int scissH = (int) (mapHeight * scale);
		RenderUtil.enableScissor();
		RenderUtil.applyScissor(scissX, scissY, scissW, scissH);
		minecraftFramebuffer.beginWrite(false);
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
		this.secondaryFramebuffer.beginRead();
		RenderUtil.startDraw();
		buffer = RenderUtil.getBuffer();
		buffer.vertex(imgX, imgY + imgH, 0.0).texture(0.0F, 0.0F).next();
		buffer.vertex(imgX + imgW, imgY + imgH, 0.0).texture(1.0F, 0.0F).next();
		buffer.vertex(imgX + imgW, imgY, 0.0).texture(1.0F, 1.0F).next();
		buffer.vertex(imgX, imgY, 0.0).texture(0.0F, 1.0F).next();
		RenderUtil.endDraw();
		List<WaypointIcon> drawableWaypoints = minimap.getWaypoints(playerPos, centerX, centerY);
		for (WaypointIcon icon : drawableWaypoints) {
			icon.draw(matrices, consumerProvider, mapX, mapY, mapWidth, mapHeight, offX, offY, rotation);
		}
		consumerProvider.draw();
		RenderUtil.disableScissor();
	}
	
	private void drawMap() {
		int cornerX = lastX - scaledW / 2;
		int cornerZ = lastZ - scaledH / 2;
		
		BlockPos.Mutable currentPos = new BlockPos.Mutable();
		
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
			this.chunkGrid.updateCenter(lastX, lastZ);
			this.chunkGrid.updateGrid();
			this.playerMoved = false;
		}
		this.chunkGrid.draw();
	}
	
	private void drawEntities(MatrixStack matrices, VertexConsumerProvider.Immediate consumerProvider) {
		float halfW = imgW / 2.0F;
		float halfH = imgH / 2.0F;
		int iconX = (int) (imgW - mapWidth);
		int iconY = (int) (imgH - mapHeight);
		List<MapIcon<?>> drawableEntities = minimap.getDrawableIcons(lastX, lastZ, halfW, halfH, delta);
		for (MapIcon<?> icon : drawableEntities) {
			icon.draw(matrices, consumerProvider, iconX, iconY, mapWidth, mapHeight, rotation);
			consumerProvider.draw();
		}
	}
	
	public void resize(int width, int height, boolean isMac) {
		this.primaryFramebuffer.resize(width, height, isMac);
		this.secondaryFramebuffer.resize(width, height, isMac);
	}
	
	public void deleteFramebuffers() {
		this.primaryFramebuffer.delete();
		this.secondaryFramebuffer.delete();
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
