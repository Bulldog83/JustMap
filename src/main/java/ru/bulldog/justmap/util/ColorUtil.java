package ru.bulldog.justmap.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import net.fabricmc.fabric.impl.client.indigo.renderer.helper.ColorHelper;
import net.fabricmc.fabric.impl.client.rendering.fluid.FluidRenderHandlerRegistryImpl;

import net.minecraft.block.AttachedStemBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.FernBlock;
import net.minecraft.block.FluidBlock;
import net.minecraft.block.LilyPadBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.block.TallPlantBlock;
import net.minecraft.block.VineBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.color.world.BiomeColors;
import net.minecraft.client.render.block.BlockModels;
import net.minecraft.client.render.model.BakedQuad;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.fluid.FluidState;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import net.minecraft.world.chunk.WorldChunk;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.mixins.client.BakedSpriteAccessor;
import ru.bulldog.justmap.util.math.MathUtil;

public class ColorUtil {
	
	private static MinecraftClient minecraft = MinecraftClient.getInstance();	
	private static BlockModels blockModels = minecraft.getBlockRenderManager().getModels();	
	private static FluidRenderHandlerRegistryImpl fluidRenderHandlerRegistry = FluidRenderHandlerRegistryImpl.INSTANCE;	
	private static Map<BlockState, Integer> colorCache = new HashMap<>();	
	private static float[] floatBuffer = new float[3];
	
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
		int cmax = (r > g) ? r : g;
		if (b > cmax) cmax = b;
		int cmin = (r < g) ? r : g;
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
		return 0xFF000000 | (r << 16) | (g << 8) | (b << 0);
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
	
	private static int getStateColor(BlockState state) {
		if (colorCache.containsKey(state)) {
			return colorCache.get(state);
		}		
		return extractColor(state);
	}
	
	private static int extractColor(BlockState state) {
		List<BakedQuad> quads = blockModels.getModel(state).getQuads(state, Direction.UP, new Random());
		
		Identifier blockSprite;
		if (quads.size() > 0) {
			blockSprite = ((BakedSpriteAccessor) quads.get(0)).getSprite().getId();
		} else {
			blockSprite = blockModels.getSprite(state).getId();
		}
		Identifier texture = new Identifier(blockSprite.getNamespace(), String.format("textures/%s.png", blockSprite.getPath()));
		NativeImage image = ImageUtil.loadImage(texture, 16, 16);
		
		long r = 0, g = 0, b = 0;
					
		int pixels = 0;
		for (int i = 0; i < image.getWidth(); i++) {
			for (int j = 0; j < image.getHeight(); j++) {
				int col = image.getPixelColor(i, j);
				if ((int) (col >> 24 & 255) > 0) {
					b += (col >> 16) & 255;
					g += (col >> 8) & 255;
					r += col & 255;
					pixels++;
				}
			}
		}
		image.close();
		
		if (pixels > 0) {
			int color = ((int) (r / pixels)) << 16 | ((int) (g / pixels)) << 8 | (int) (b / pixels);
			colorCache.put(state, color);
			
			return color;
		}
		
		return -1;
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
	
	private static int fluidColor(World world, BlockState state, BlockPos pos, int defColor) {
		FluidState fluidState = state.getBlock().getFluidState(state);
		int fcolor = fluidRenderHandlerRegistry.get(fluidState.getFluid()).getFluidColor(world, pos, fluidState);
		return fcolor != -1 ? fcolor : defColor;
	}
	
	public static int blockColor(WorldChunk worldChunk, BlockPos pos) {
		World world = worldChunk.getWorld();
		BlockPos overPos = new BlockPos(pos.getX(), pos.getY() + 1, pos.getZ());
		BlockState overState = worldChunk.getBlockState(overPos);
		BlockState blockState = worldChunk.getBlockState(pos);
		
		boolean waterTint = ClientSettings.alternateColorRender && ClientSettings.waterTint;
		boolean skipWater = !(ClientSettings.hideWater || waterTint);
		if (!ClientSettings.hideWater && ClientSettings.hidePlants && StateUtil.isSeaweed(overState)) {
			if (waterTint) {
				int color = blockColor(world, blockState, pos);
				return applyTint(color, BiomeColors.getWaterColor(world, pos));
			}
			return blockColor(world, Blocks.WATER.getDefaultState(), pos);			
		} else if (!StateUtil.isAir(blockState) && StateUtil.checkState(overState, skipWater, !ClientSettings.hidePlants)) {			
			int color = blockColor(world, blockState, pos);
			if (ClientSettings.hideWater) return color;
			if (waterTint && (StateUtil.isWater(overState) || StateUtil.isWaterlogged(blockState))) {
				return applyTint(color, BiomeColors.getWaterColor(world, pos));
			}
			return color;
		}
	
		return -1;
	}
	
	public static int blockColor(World world, BlockState state, BlockPos pos) {
		int materialColor = state.getTopMaterialColor(world, pos).color;
		if (ClientSettings.alternateColorRender) {
			int blockColor = minecraft.getBlockColors().getColor(state, world, pos, Colors.LIGHT);
			int textureColor = getStateColor(state);
			
			Block block = state.getBlock();
			if (block instanceof VineBlock) {
				blockColor = proccessColor(blockColor, textureColor, BiomeColors.getFoliageColor(world, pos));
			} else if (block instanceof FernBlock || block instanceof TallPlantBlock) {				
				blockColor = proccessColor(blockColor, textureColor, BiomeColors.getGrassColor(world, pos));
			} else if (block instanceof LilyPadBlock || block instanceof StemBlock || block instanceof AttachedStemBlock) {
				blockColor = proccessColor(blockColor, textureColor, materialColor);
			} else if (block instanceof FluidBlock) {
				if (StateUtil.isWater(state)) {
					blockColor = proccessColor(blockColor, textureColor, BiomeColors.getWaterColor(world, pos));
				} else {
					blockColor = fluidColor(world, state, pos, textureColor);
				}
			} else if (blockColor != -1){
				blockColor = ColorHelper.multiplyColor(textureColor, blockColor);
			} else {
				blockColor = textureColor != -1 ? textureColor : materialColor;
			}
			
			return blockColor;
		}
		
		return materialColor;
	}
}