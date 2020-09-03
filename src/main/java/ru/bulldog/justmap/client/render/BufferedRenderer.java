package ru.bulldog.justmap.client.render;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gl.Framebuffer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.map.ChunkGrid;
import ru.bulldog.justmap.map.data.RegionData;
import ru.bulldog.justmap.map.icon.MapIcon;
import ru.bulldog.justmap.map.icon.WaypointIcon;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.DataUtil;
import ru.bulldog.justmap.util.render.ExtendedFramebuffer;
import ru.bulldog.justmap.util.render.GLC;
import ru.bulldog.justmap.util.render.RenderUtil;

public class BufferedRenderer extends MapRenderer {
	
	private ExtendedFramebuffer scalingFramebuffer;
	private ExtendedFramebuffer rotationFramebuffer;
	private boolean triedFBO = false;
	private boolean loadedFBO = false;
	
	public BufferedRenderer(Minimap map) {
		super(map);
	}
	
	public void loadFrameBuffer(int width, int height) {
		if (!ExtendedFramebuffer.canUseFramebuffer()) {
			JustMap.LOGGER.warning("FBO not supported! Using fast minimap render.");
		} else {
			this.scalingFramebuffer = new ExtendedFramebuffer(width, height, false);
			this.rotationFramebuffer = new ExtendedFramebuffer(width, height, false);
			this.loadedFBO = (this.scalingFramebuffer.fbo != -1 && this.rotationFramebuffer.fbo != -1);
		}
		this.triedFBO = true;
	}

	@Override
	protected void render(MatrixStack matrices) {
		if (paramsUpdated) {
			if (this.isFBOLoaded()) {
				this.resize(imgW, imgH);
			}
			if (ClientParams.showGrid) {
				if (chunkGrid == null) {
					this.chunkGrid = new ChunkGrid(lastX, lastZ, 0, 0, imgW, imgH, mapScale);
				} else {
					this.chunkGrid.updateRange(0, 0, imgW, imgH, mapScale);
					this.chunkGrid.updateGrid();
				}
			}
			this.paramsUpdated = false;
		}
		
		boolean isMac = MinecraftClient.IS_SYSTEM_MAC;
		
		RenderSystem.pushMatrix();
		this.scalingFramebuffer.beginWrite(true);
		RenderSystem.clear(GLC.GL_COLOR_OR_DEPTH_BUFFER_BIT, isMac);
		RenderSystem.enableTexture();
		RenderSystem.matrixMode(GLC.GL_PROJECTION);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.ortho(0.0, imgW, imgH, 0.0, 1000.0, 3000.0);
		RenderSystem.matrixMode(GLC.GL_MODELVIEW);
		RenderSystem.pushMatrix();
		RenderSystem.loadIdentity();
		RenderSystem.translatef(0.0F, 0.0F, -2000.0F);
		RenderSystem.enableBlend();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		this.drawMap();
		if (ClientParams.showGrid) {
			this.drawGrid();
		}
		this.scalingFramebuffer.endWrite();
		RenderSystem.popMatrix();
		RenderSystem.pushMatrix();
		this.rotationFramebuffer.beginWrite(false);
		RenderSystem.clear(GLC.GL_COLOR_OR_DEPTH_BUFFER_BIT, isMac);
		float moveX = imgW / 2.0F;
		float moveY = imgH / 2.0F;
		if (mapRotation) {
			RenderSystem.translatef(moveX, moveY, 0.0F);
			RenderSystem.rotatef(rotation + 180, 0.0F, 0.0F, 1.0F);
			RenderSystem.translatef(-moveX, -moveY, 0.0F);
		}
		RenderSystem.pushMatrix();
		RenderSystem.translatef(-offX, -offY, 0.0F);
		
		this.scalingFramebuffer.beginRead();
		RenderUtil.drawQuad(0.0, 0.0, imgW, imgH);
		
		VertexConsumerProvider.Immediate consumerProvider = minecraft.getBufferBuilders().getEntityVertexConsumers();
		List<MapIcon<?>> drawableEntities = minimap.getDrawableIcons(lastX, lastZ, moveX, moveY, delta);
		for (MapIcon<?> icon : drawableEntities) {
			icon.draw(matrices, consumerProvider, 0, 0, rotation);
		}
		consumerProvider.draw();
		
		RenderSystem.popMatrix();
		
		List<WaypointIcon> drawableWaypoints = minimap.getWaypoints(playerPos, (int) moveX, (int) moveY);
		for (WaypointIcon icon : drawableWaypoints) {
			icon.draw(matrices, consumerProvider, 0, 0, offX, offY, rotation);
		}
		consumerProvider.draw();
		
		this.rotationFramebuffer.endWrite();
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GLC.GL_PROJECTION);
		RenderSystem.popMatrix();
		RenderSystem.matrixMode(GLC.GL_MODELVIEW);
		RenderSystem.popMatrix();
		
		Framebuffer minecraftFramebuffer = DataUtil.getMinecraft().getFramebuffer();
		minecraftFramebuffer.beginWrite(false);
		RenderSystem.viewport(0, 0, minecraftFramebuffer.viewportWidth, minecraftFramebuffer.viewportHeight);
		RenderSystem.pushMatrix();
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
		this.rotationFramebuffer.beginRead();
		RenderUtil.drawQuad(mapX, mapY, mapWidth, mapHeight);
		RenderSystem.popMatrix();
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
	
	public void resize(int width, int height) {
		boolean isMac = MinecraftClient.IS_SYSTEM_MAC;
		this.scalingFramebuffer.resize(width, height, isMac);
		this.rotationFramebuffer.resize(width, height, isMac);
	}
	
	public void deleteFramebuffers() {
		this.scalingFramebuffer.delete();
		this.rotationFramebuffer.delete();
		this.setLoadedFBO(false);
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
