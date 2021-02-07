package ru.bulldog.justmap.client.screen;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.PoseStack;
import net.minecraft.text.Component;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.registry.ResourceKey;
import net.minecraft.world.Level;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.LangUtil;
import ru.bulldog.justmap.util.render.RenderUtil;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

public class MapScreen extends Screen {
	public static final ResourceLocation DEFAULT_TEXTURE = new ResourceLocation("textures/block/dirt.png");
	public static final HashMap<String, Pair<String, ResourceLocation>> DIMENSION_INFO = new HashMap<String, Pair<String, ResourceLocation>>() {
		private static final long serialVersionUID = 1L;
		{
			put("minecraft:overworld", new Pair<>(JustMap.MODID + ".dim.overworld", new ResourceLocation("textures/block/stone.png")));
			put("minecraft:the_nether", new Pair<>(JustMap.MODID + ".dim.nether", new ResourceLocation("textures/block/netherrack.png")));
			put("minecraft:the_end", new Pair<>(JustMap.MODID + ".dim.the_end", new ResourceLocation("textures/block/end_stone.png")));
		}
	};
	
	protected final Screen parent;	
	protected Pair<String, ResourceLocation> info;
	protected LangUtil langUtil;
	protected int x, y, center;
	protected int paddingTop;
	protected int paddingBottom;
	
	protected MapScreen(Component title) {
		this(title, null);
	}
	
	public MapScreen(Component title, Screen parent) {
		super(title);
		this.parent = parent;
		this.langUtil = new LangUtil(LangUtil.GUI_ELEMENT);
	}
	
	@Override
	protected void init() {
		ResourceKey<Level> dimKey = client.world.dimension();
		this.info = DIMENSION_INFO.getOrDefault(dimKey.getValue().toString(), null);
	}
	
	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.disableDepthTest();
		this.renderBackground(matrices);
		this.renderForeground(matrices);
		for (Element e : children) {
			if (e instanceof Drawable) {
				((Drawable) e).render(matrices, mouseX, mouseY, delta);
			}
		}
		RenderSystem.enableDepthTest();
	}
	
	public void renderBackground(PoseStack matrixStack) {
		fill(matrixStack, 0, 0, width, height, 0x88444444);		
		this.drawBorders();
	}
	
	public void renderForeground(PoseStack matrixStack) {}
	
	@Override
	public void onClose() {
		this.client.openScreen(parent);
	}
	
	public void renderTexture(int x, int y, int width, int height, float u, float v, ResourceLocation id) {
		RenderUtil.bindTexture(id);
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		RenderUtil.startDraw();
		RenderUtil.addQuad(x, y, width, height, 0.0F, 0.0F, u, v);
		RenderUtil.endDraw();
	}
	
	public void renderTextureModal(int x, int y, int width, int height, int textureWidth, int textureHeight, ResourceLocation id) {
		this.renderTexture(x, y, width, height, (float) width / textureWidth, (float) height / textureHeight, id);
	}
	
	public void renderTextureRepeating(int x, int y, int width, int height, int textureHeight, int textureWidth, String id) {
		this.renderTextureRepeating(x, y, width, height, textureHeight, textureWidth, new ResourceLocation(id));
	}
	
	public void renderTextureRepeating(int x, int y, int width, int height, int textureHeight, int textureWidth, ResourceLocation id) {
		for (int xp = 0; xp < width; xp += textureWidth) {
			int w = (xp + textureWidth < width) ? textureWidth : width - xp;
			for (int yp = 0; yp < height; yp += textureHeight) {
				int h = (yp + textureHeight < height) ? textureHeight : height - yp;
				this.renderTextureModal(x + xp, y + yp, w, h, textureWidth, textureHeight, id);
			}
		}
	}
	
	protected void drawBorders() {
		this.drawBorders(32, 32);
	}
	
	protected void drawBorders(int top, int bottom) {
		ResourceLocation id = info != null ? info.getSecond() : DEFAULT_TEXTURE;
		this.renderTextureRepeating(0, 0, width, top, 16, 16, id);
		this.renderTextureRepeating(0, height - bottom, width, bottom, 16, 16, id);		
	}
	
	public Component lang(String key) {
		return langUtil.getText(key);
	}	
}