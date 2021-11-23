package ru.bulldog.justmap.client.screen;

import java.util.HashMap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.registry.RegistryKey;
import net.minecraft.world.World;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.LangUtil;
import ru.bulldog.justmap.util.render.RenderUtil;

public abstract class AbstractMapScreen extends Screen {
	public static final Identifier DEFAULT_TEXTURE = new Identifier("textures/block/dirt.png");
	public static final HashMap<String, Pair<String, Identifier>> DIMENSION_INFO = new HashMap<String, Pair<String, Identifier>>() {
		private static final long serialVersionUID = 1L;
		{
			put("minecraft:overworld", new Pair<>(JustMap.MODID + ".dim.overworld", new Identifier("textures/block/stone.png")));
			put("minecraft:the_nether", new Pair<>(JustMap.MODID + ".dim.nether", new Identifier("textures/block/netherrack.png")));
			put("minecraft:the_end", new Pair<>(JustMap.MODID + ".dim.the_end", new Identifier("textures/block/end_stone.png")));
		}
	};
	
	protected final Screen parent;	
	protected Pair<String, Identifier> info;
	protected final LangUtil langUtil;
	protected int x, y, center;
	protected int paddingTop;
	protected int paddingBottom;
	
	protected AbstractMapScreen(Text title) {
		this(title, null);
	}
	
	public AbstractMapScreen(Text title, Screen parent) {
		super(title);
		this.parent = parent;
		this.langUtil = new LangUtil(LangUtil.GUI_ELEMENT);
	}
	
	@Override
	protected void init() {
		RegistryKey<World> dimKey = client.world.getRegistryKey();
		this.info = DIMENSION_INFO.getOrDefault(dimKey.getValue().toString(), null);
	}
	
	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderSystem.disableDepthTest();
		this.renderBackground(matrices);
		this.renderForeground(matrices);
		for (Element e : children()) {
			if (e instanceof Drawable) {
				((Drawable) e).render(matrices, mouseX, mouseY, delta);
			}
		}
		RenderSystem.enableDepthTest();
	}
	
	public void renderBackground(MatrixStack matrixStack) {
		fill(matrixStack, 0, 0, width, height, 0x88444444);		
		this.drawBorders();
	}
	
	public void renderForeground(MatrixStack matrixStack) {}
	
	@Override
	public void onClose() {
		this.client.setScreen(parent);
	}
	
	public void renderTexture(int x, int y, int width, int height, float u, float v, Identifier id) {
		RenderUtil.bindTexture(id);
		RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
		RenderUtil.startDraw();
		RenderUtil.addQuad(x, y, width, height, 0.0F, 0.0F, u, v);
		RenderUtil.endDraw();
	}
	
	public void renderTextureModal(int x, int y, int width, int height, int textureWidth, int textureHeight, Identifier id) {
		this.renderTexture(x, y, width, height, (float) width / textureWidth, (float) height / textureHeight, id);
	}

	public void renderTextureRepeating(int x, int y, int width, int height, int textureHeight, int textureWidth, Identifier id) {
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
		Identifier id = info != null ? info.getSecond() : DEFAULT_TEXTURE;
		this.renderTextureRepeating(0, 0, width, top, 16, 16, id);
		this.renderTextureRepeating(0, height - bottom, width, bottom, 16, 16, id);		
	}
	
	public Text lang(String key) {
		return langUtil.getText(key);
	}	
}
