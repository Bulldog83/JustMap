package ru.bulldog.justmap.worldmap;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.text.LiteralText;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.dimension.DimensionType;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.MapScreen;
import ru.bulldog.justmap.minimap.data.MapCache;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.math.MathUtil;

public class Worldmap extends MapScreen {

	private static final LiteralText TITLE = new LiteralText("Worldmap");
	
	private static Worldmap worldmap;
	
	public static Worldmap getScreen() {
		if (worldmap == null) {
			worldmap = new Worldmap();
		}
		
		return worldmap;
	}
	
	private int paddingTop;
	private int paddingBottom;
	private int imageWidth;
	private int imageHeight;
	private int scaledWidth;
	private int scaledHeight;
	private float imageScale = 1.0F;
	
	private BlockPos centerPos;
	private NativeImage mapImage;
	private NativeImageBackedTexture mapTexture;
	private Identifier textureId;
	
	private Worldmap() {
		super(TITLE);
		
		this.paddingTop = 16;
		this.paddingBottom = 32;
	}

	@Override
	public void init() {		
		if (centerPos == null) {
			centerPos = minecraft.player.getBlockPos();
		}
		
		int dimId = minecraft.player.dimension.getRawId();
		this.info = DIMENSION_INFO.getOrDefault(DimensionType.byRawId(dimId).toString(), null);
		
		children.add(new ButtonWidget(width - 80, height - 26, 60, 20, lang("cancel"), (b) -> onClose()));
	}
	
	@Override
	public void renderBackground() {
		fill(x, 0, x + width, height, 0xFF444444);
		drawBorders(paddingTop, paddingBottom);
	}
	
	@Override
	public void renderForeground() {
		drawMap();
	}
	
	private void prepareTexture() {
		this.imageWidth = width;		
		this.imageHeight = height - (paddingTop + paddingBottom);
		this.scaledWidth = (int) (imageWidth * imageScale);
		this.scaledHeight = (int) (imageHeight * imageScale);
		
		if (mapImage == null || mapImage.getWidth() != scaledWidth || mapImage.getHeight() != scaledHeight) {
			this.mapImage = new NativeImage(scaledWidth, scaledHeight, false);
			ImageUtil.fillImage(mapImage, Colors.BLACK);
			
			updateMapTexture();
			
			if (mapTexture != null) mapTexture.close();
			mapTexture = new NativeImageBackedTexture(mapImage);
			textureId = minecraft.getTextureManager().registerDynamicTexture(JustMap.MODID + "_worldmap_texture", mapTexture);
		}
	}
	
	private void updateMapTexture() {
		int centerX = centerPos.getX() >> 4;
		int centerZ = centerPos.getZ() >> 4;		
		int chunksX = (int) Math.ceil((scaledWidth / 16.0) / 2);
		int chunksZ = (int) Math.ceil((scaledHeight / 16.0) / 2);
		int startX = centerX - chunksX;
		int startZ = centerZ - chunksZ;
		int stopX = centerX + chunksX;
		int stopZ = centerZ + chunksZ;
		
		MapCache mapData = MapCache.get();
		
		int picX = 0, picY = 0;
		for (int posX = startX; posX < stopX; posX++) {
			picY = 0;
			for (int posZ = startZ; posZ < stopZ; posZ++) {
				ChunkPos pos = new ChunkPos(posX, posZ);
				NativeImage chunkImage = mapData.getRegion(pos).getChunkImage(pos);
				ImageUtil.writeTile(mapImage, chunkImage, picX, picY);
				
				chunkImage.close();
				picY += 16;
			}
			
			picX += 16;
		}
		
		if (mapTexture != null) {
			mapTexture.upload();
		}
	}
	
	private void bindMapTexture() {
		prepareTexture();
		minecraft.getTextureManager().bindTexture(textureId);
	}
	
	private void drawMap() {
		bindMapTexture();
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		
		builder.begin(7, VertexFormats.POSITION_TEXTURE);			
		builder.vertex(0, paddingTop, 0).texture(0, 0).next();
		builder.vertex(0, paddingTop + imageHeight, 0).texture(0, 1).next();
		builder.vertex(imageWidth, paddingTop + imageHeight, 0).texture(1, 1).next();
		builder.vertex(imageWidth, paddingTop, 0).texture(1, 0).next();
		
		tessellator.draw();
	}
	
	public void setCenterByPlayer() {
		this.centerPos = minecraft.player.getBlockPos();
  		updateMapTexture();
	}
	
	private void changeScale(float value) {
		this.imageScale = MathUtil.clamp(this.imageScale + value, 0.5F, 3.0F);		
		prepareTexture();
	}
	
	@Override
	public boolean keyPressed(int i, int j, int k) {
		switch(i) {
			case GLFW.GLFW_KEY_W:
			case GLFW.GLFW_KEY_UP:
				this.centerPos = centerPos.add(0, 0, -16);
		  		updateMapTexture();
		  		return true;
		  	case GLFW.GLFW_KEY_S:
		  	case GLFW.GLFW_KEY_DOWN:
		  		this.centerPos = centerPos.add(0, 0, 16);
		  		updateMapTexture();
		  		return true;
		  	case GLFW.GLFW_KEY_A:
		  	case GLFW.GLFW_KEY_LEFT:
		  		this.centerPos = centerPos.add(-16, 0, 0);
		  		updateMapTexture();
		  		return true;
		  	case GLFW.GLFW_KEY_D:
		  	case GLFW.GLFW_KEY_RIGHT:
		  		this.centerPos = centerPos.add(16, 0, 0);
		  		updateMapTexture();
		  		return true;
		  	case GLFW.GLFW_KEY_MINUS:
		  	case GLFW.GLFW_KEY_KP_SUBTRACT:
		  		changeScale(0.25F);
		  		return true;
		  	case GLFW.GLFW_KEY_EQUAL:
		  	case GLFW.GLFW_KEY_KP_ADD:
		  		changeScale(-0.25F);
		  		return true;
		  	case GLFW.GLFW_KEY_X:
		  		setCenterByPlayer();
		  		return true;
		  	case GLFW.GLFW_KEY_M:
		  		this.onClose();
		  		return true;
		  	default:
		  		return super.keyPressed(i, j, k);
		}
	}
	
	@Override
	public boolean mouseDragged(double d, double e, int i, double f, double g) {
		int x = centerPos.getX();
		int y = centerPos.getY();
		int z = centerPos.getZ();
		
		x -= Math.ceil(f);
		z -= Math.ceil(g);
		
		this.centerPos = new BlockPos(x, y, z);
		updateMapTexture();
		
		return super.mouseDragged(d, e, i, f, g);
	}
	
	@Override
	public boolean mouseScrolled(double d, double e, double f) {
		changeScale(f > 0 ? -0.25F : 0.25F);		
		return super.mouseScrolled(d, e, f);
	}
}
