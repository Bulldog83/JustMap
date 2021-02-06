package ru.bulldog.justmap.util.colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.Minecraft;

import net.minecraft.client.renderer.BiomeColors;
import net.minecraft.client.renderer.block.BlockModelShaper;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.material.FluidState;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.StateUtil;
import ru.bulldog.justmap.util.math.MathUtil;

@Environment(EnvType.CLIENT)
public class ColorUtil {
	
	private static Minecraft minecraft = Minecraft.getInstance();
	private static BlockModelShaper blockModels = minecraft.getBlockRenderer().getBlockModelShaper();
	private static FluidRenderHandlerRegistryImpl fluidRenderHandlerRegistry = FluidRenderHandlerRegistryImpl.INSTANCE;
	private static float[] floatBuffer = new float[3];
	private static ColorProviders colorProvider = ColorProviders.INSTANCE;
	private static Colors colorPalette = Colors.INSTANCE;
	
	public static int[] toIntArray(int color) {
		return new int[] {
			(color >> 24) & 255,
			(color >> 16) & 255,
			(color >> 8) & 255,
			 color & 255
		};
	}
	
	public static float[] toFloatArray(int color) {
		floatBuffer[0] = ((color >> 16 & 255) / 255.0F);
		floatBuffer[1] = ((color >> 8 & 255) / 255.0F);
		floatBuffer[2] = ((color & 255) / 255.0F);
		
		return floatBuffer;
	}
	
	public static float[] RGBtoHSB(int r, int g, int b, float[] hsbvals) {
		float hue, saturation, brightness;
		if (hsbvals == null) {
			hsbvals = floatBuffer;
		}
		int cmax = Math.max(r, g);
		if (b > cmax) cmax = b;
		int cmin = Math.min(r, g);
		if (b < cmin) cmin = b;

		brightness = ((float) cmax) / 255.0F;
		if (cmax != 0)
			saturation = ((float) (cmax - cmin)) / ((float) cmax);
		else
			saturation = 0;
		if (saturation == 0)
			hue = 0;
		else {
			float redc = ((float) (cmax - r)) / ((float) (cmax - cmin));
			float greenc = ((float) (cmax - g)) / ((float) (cmax - cmin));
			float bluec = ((float) (cmax - b)) / ((float) (cmax - cmin));
			if (r == cmax)
				hue = bluec - greenc;
			else if (g == cmax)
				hue = 2.0F + redc - bluec;
			else
				hue = 4.0F + greenc - redc;
			hue = hue / 6.0F;
			if (hue < 0)
				hue = hue + 1.0F;
		}
		hsbvals[0] = hue;
		hsbvals[1] = saturation;
		hsbvals[2] = brightness;
		return hsbvals;
	}
	
	public static int HSBtoRGB(float hue, float saturation, float brightness) {
		int r = 0, g = 0, b = 0;
		if (saturation == 0) {
			r = g = b = (int) (brightness * 255.0F + 0.5F);
		} else {
			float h = (hue - (float)Math.floor(hue)) * 6.0F;
			float f = h - (float)java.lang.Math.floor(h);
			float p = brightness * (1.0F - saturation);
			float q = brightness * (1.0F - saturation * f);
			float t = brightness * (1.0F - (saturation * (1.0F - f)));
			switch ((int) h) {
			case 0:
				r = (int) (brightness * 255.0F + 0.5F);
				g = (int) (t * 255.0F + 0.5F);
				b = (int) (p * 255.0F + 0.5F);
				break;
			case 1:
				r = (int) (q * 255.0F + 0.5F);
				g = (int) (brightness * 255.0F + 0.5F);
				b = (int) (p * 255.0F + 0.5F);
				break;
			case 2:
				r = (int) (p * 255.0F + 0.5F);
				g = (int) (brightness * 255.0F + 0.5F);
				b = (int) (t * 255.0F + 0.5F);
				break;
			case 3:
				r = (int) (p * 255.0F + 0.5F);
				g = (int) (q * 255.0F + 0.5F);
				b = (int) (brightness * 255.0F + 0.5F);
				break;
			case 4:
				r = (int) (t * 255.0F + 0.5F);
				g = (int) (p * 255.0F + 0.5F);
				b = (int) (brightness * 255.0F + 0.5F);
				break;
			case 5:
				r = (int) (brightness * 255.0F + 0.5F);
				g = (int) (p * 255.0F + 0.5F);
				b = (int) (q * 255.0F + 0.5F);
				break;
			}
		}
		return 0xFF000000 | (r << 16) | (g << 8) | b;
	}
	
	public static int parseHex(String hexColor) {
		int len = hexColor.length();
		if (len < 6 || len > 8 || len % 2 > 0) {
			return -1;
		}
		
		int color, shift;
		if(len == 6) {
			color = 0xFF000000; shift = 16;
		} else {
			color = 0; shift = 24;
		}
		
		try {
			String[] splited = hexColor.split("(?<=\\G.{2})");
			for (String digit : splited) {
				color |= Integer.valueOf(digit, 16) << shift;
				shift -= 8;
			}
		} catch(NumberFormatException ex) {
			JustMap.LOGGER.catching(ex);
			return -1;
		}
		
		return color;
	}
	
	public static int toABGR(int color) {
		int r = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int b = color & 255;
		return 0xFF000000 | b << 16 | g << 8 | r;
	}
	
	public static int ABGRtoARGB(int color) {
		int a = (color >> 24) & 255;
		int b = (color >> 16) & 255;
		int g = (color >> 8) & 255;
		int r = color & 255;
		return a << 24 | r << 16 | g << 8 | b;
	}
	
	public static int colorBrigtness(int color, float val) {
		RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, floatBuffer);
		floatBuffer[2] += val / 10.0F;
		floatBuffer[2] = MathUtil.clamp(floatBuffer[2], 0.0F, 1.0F);
		return HSBtoRGB(floatBuffer[0], floatBuffer[1], floatBuffer[2]);
	}
	
	public static int applyTint(int color, int tint) {
		return colorBrigtness(ColorHelper.multiplyColor(color, tint), 1.5F);
	}
	
	private static int extractColor(BlockState state) {
		List<BakedQuad> quads = blockModels.getBlockModel(state).getQuads(state, Direction.UP, new Random());
		
		ResourceLocation blockSprite;
		if (quads.size() > 0) {
			blockSprite = ((BakedSpriteAccessor) quads.get(0)).getSprite().getId();
		} else {
			blockSprite = blockModels.getSprite(state).getId();
		}
		
		int color = colorPalette.getTextureColor(state, blockSprite);
		if (color != 0x0) return color;
		
		ResourceLocation texture = new ResourceLocation(blockSprite.getNamespace(), String.format("textures/%s.png", blockSprite.getPath()));
		NativeImage image = ImageUtil.loadImage(texture, 16, 16);
		
		int height = state.getBlock() instanceof FlowerBlock ? image.getHeight() / 2 : image.getHeight();
		
		List<Integer> colors = new ArrayList<>();
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < height; j++) {
				int col = image.getPixelRGBA(i, j);
				if (((col >> 24) & 255) > 0) {
					colors.add(ABGRtoARGB(col));
				}
			}
		}
		image.close();
		
		if (colors.size() == 0) return -1;
		
		ColorExtractor extractor = new ColorExtractor(colors);
		color = extractor.analize();
		colorPalette.addTextureColor(state, blockSprite, color);
		
		return color;
	}
	
	public static int proccessColor(int color, int heightDiff, float topoLevel) {
		RGBtoHSB((color >> 16) & 255, (color >> 8) & 255, color & 255, floatBuffer);
		floatBuffer[1] += ClientSettings.mapSaturation / 100.0F;
		floatBuffer[1] = MathUtil.clamp(floatBuffer[1], 0.0F, 1.0F);
		floatBuffer[2] += ClientSettings.mapBrightness / 100.0F;
		floatBuffer[2] = MathUtil.clamp(floatBuffer[2], 0.0F, 1.0F);
		if (ClientSettings.showTerrain && heightDiff != 0) {
			floatBuffer[2] += heightDiff / 10.0F;
			floatBuffer[2] = MathUtil.clamp(floatBuffer[2], 0.0F, 1.0F);
		}
		if (ClientSettings.showTopography && topoLevel != 0) {
			floatBuffer[2] += MathUtil.clamp(topoLevel, -0.75F, 0.1F);
			floatBuffer[2] = MathUtil.clamp(floatBuffer[2], 0.0F, 1.0F);
		}
		return HSBtoRGB(floatBuffer[0], floatBuffer[1], floatBuffer[2]);
	}
	
	private static int proccessColor(int blockColor, int textureColor, int defaultColor) {
		blockColor = blockColor == -1 ? defaultColor : blockColor;		
		if (blockColor != -1) {
			return ColorHelper.multiplyColor(textureColor, blockColor);
		}
		
		return textureColor;
	}
	
	public static int blockColor(LevelChunk worldChunk, BlockPos pos) {
		Level world = worldChunk.getLevel();
		BlockPos overPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
		BlockState overState = worldChunk.getBlockState(overPos);
		BlockState blockState = worldChunk.getBlockState(pos);
		
		boolean waterTint = ClientSettings.alternateColorRender && ClientSettings.waterTint;
		boolean skipWater = !(ClientSettings.hideWater || waterTint);
		if (!ClientSettings.hideWater && ClientSettings.hidePlants && StateUtil.isSeaweed(overState)) {
			if (waterTint) {
				int color = blockColor(world, blockState, pos);
				return applyTint(color, BiomeColors.getAverageWaterColor(world, pos));
			}
			return blockColor(world, Blocks.WATER.defaultBlockState(), pos);
		} else if (!StateUtil.isAir(blockState) && StateUtil.checkState(overState, skipWater, !ClientSettings.hidePlants)) {			
			int color = blockColor(world, blockState, pos);
			if (ClientSettings.hideWater) return color;
			if (waterTint && (StateUtil.isWater(overState) || StateUtil.isWaterlogged(blockState))) {
				return applyTint(color, BiomeColors.getAverageWaterColor(world, pos));
			}
			return color;
		}
	
		return -1;
	}
	
	public static int blockColor(Level world, BlockState state, BlockPos pos) {
		int materialColor = state.getMapColor(world, pos).col;
		if (ClientSettings.alternateColorRender) {
			int blockColor = colorPalette.getBlockColor(state);
			if (blockColor != 0x0) {
				return blockColor;
			}
			
			blockColor = colorProvider.getColor(state, world, pos);
			if (blockColor == -1) {
				blockColor = minecraft.getBlockColors().getColor(state, world, pos, Colors.LIGHT);
			}
			int textureColor = extractColor(state);
			
			Block block = state.getBlock();
			if (block instanceof VineBlock) {
				blockColor = proccessColor(blockColor, textureColor, colorProvider.getFoliageColor(world, pos));
			} else if (block instanceof TallGrassBlock || block instanceof DoublePlantBlock || block instanceof SugarCaneBlock) {
				blockColor = proccessColor(blockColor, textureColor, colorProvider.getGrassColor(world, pos));
			} else if (block instanceof WaterlilyBlock || block instanceof StemBlock || block instanceof AttachedStemBlock) {
				blockColor = proccessColor(blockColor, textureColor, materialColor);
				colorPalette.addBlockColor(state, blockColor);
			} else if (block instanceof LiquidBlock) {
				if (StateUtil.isWater(state)) {
					blockColor = proccessColor(blockColor, textureColor, colorProvider.getWaterColor(world, pos));
				} else {
					blockColor = fluidColor(world, state, pos, textureColor);
					colorPalette.addFluidColor(state, blockColor);
				}
			} else if (blockColor != -1){
				blockColor = ColorHelper.multiplyColor(textureColor, blockColor);
				if (block.equals(Blocks.BIRCH_LEAVES) || block.equals(Blocks.SPRUCE_LEAVES)) {
					colorPalette.addBlockColor(state, blockColor);
				} else if (!(block instanceof LeavesBlock) && !(block instanceof GrassBlock)) {
					colorPalette.addBlockColor(state, blockColor);
				}
			} else {
				blockColor = textureColor != -1 ? textureColor : materialColor;
				colorPalette.addBlockColor(state, blockColor);
			}
			
			return blockColor;
		}
		
		return materialColor;
	}
	
	private static int fluidColor(Level world, BlockState state, BlockPos pos, int defColor) {
		int color = colorPalette.getFluidColor(state);
		if (color == 0x0) {
			FluidState fluidState = state.getBlock().getFluidState(state);
			color = fluidRenderHandlerRegistry.get(fluidState.getFluid()).getFluidColor(world, pos, fluidState);
		}
		return color == -1 ? defColor : color;
	}
}