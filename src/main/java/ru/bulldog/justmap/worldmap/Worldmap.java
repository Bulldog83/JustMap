package ru.bulldog.justmap.worldmap;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.TextureManager;
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

public class Worldmap extends MapScreen {

	private static final LiteralText title = new LiteralText("Worldmap");
	
	private int paddingTop;
	private int paddingBottom;
	private int imageHeight;
	
	private NativeImage mapTexture;
	private Identifier textureId;
	
	public Worldmap() {
		super(title);
		
		this.paddingTop = 16;
		this.paddingBottom = 32;
	}

	@Override
	public void init(MinecraftClient minecraftClient, int width, int height) {
		super.init(minecraftClient, width, height);
		
		int dimId = minecraft.player.dimension.getRawId();
		this.info = DIMENSION_INFO.getOrDefault(DimensionType.byRawId(dimId).toString(), null);		
		this.width = width;
		this.height = height;
		
		children.add(new ButtonWidget(width - 80, height - 26, 60, 20, lang("cancel"), (b) -> onClose()));
		
		this.imageHeight = height - (paddingTop + paddingBottom);
		this.mapTexture = new NativeImage(width, imageHeight, false);
		ImageUtil.fillImage(mapTexture, Colors.BLACK);
		
		prepareMapTexture();
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
	
	private void prepareMapTexture() {
		BlockPos currentPos = minecraft.player.getBlockPos();
		
		int x = currentPos.getX() - width / 2;
		int z = currentPos.getZ() - imageHeight / 2;
		
		int regions =  (int) (Math.ceil(width / 512.0) * Math.ceil(imageHeight / 512.0));
		int startX = x / 512;
		int startZ = z / 512;
		int endX = startX + regions;
		int endZ = startZ + regions;
		
		int offsetX = startX * 512 - x;
		int offsetZ = startZ * 512 - z;
		
		int posX = 0;
		for (int chunkX = startX; chunkX < endX; chunkX++) {
			int posY = 0;
			int imgX = (posX * 512) + offsetX;
			for (int chunkZ = startZ; chunkZ < endZ; chunkZ++) {

				ChunkPos pos = new ChunkPos(posX << 5, posY << 5);
				NativeImage image = MapCache.get().getRegion(pos).getImage();
				
				int imgY = (posY * 512) + offsetZ;
				ImageUtil.writeTile(mapTexture, image, imgX, imgY);
				
				posY++;
			}
			
			posX++;
		}
	}
	
	private void bindMapTexture() {
		TextureManager manager = minecraft.getTextureManager();
		if (textureId == null) {
			textureId = manager.registerDynamicTexture(JustMap.MODID + "_map_texture", new NativeImageBackedTexture(mapTexture));
		}
		
		manager.bindTexture(textureId);
	}
	
	private void drawMap() {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		builder.begin(7, VertexFormats.POSITION_TEXTURE);
		
		bindMapTexture();
		
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);		
		builder.vertex(0, paddingTop, 0).texture(0, 0).next();
		builder.vertex(0, paddingTop + imageHeight, 0).texture(0, 1).next();
		builder.vertex(width, paddingTop + imageHeight, 0).texture(1, 1).next();
		builder.vertex(width, paddingTop, 0).texture(1, 0).next();
		
		tessellator.draw();
	}
}
