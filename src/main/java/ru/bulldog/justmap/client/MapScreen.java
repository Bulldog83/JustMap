package ru.bulldog.justmap.client;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.render.BufferBuilder;
import net.minecraft.client.render.Tessellator;
import net.minecraft.client.render.VertexFormats;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.world.dimension.DimensionType;

import java.util.HashMap;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.datafixers.util.Pair;

public class MapScreen extends Screen {
	public static final Identifier DEFAULT_IDENTIFIER = new Identifier("textures/block/dirt.png");
	public static final HashMap<String, Pair<String, Identifier>> DIMENSION_INFO = new HashMap<String, Pair<String, Identifier>>() {
		private static final long serialVersionUID = 1L;
		{
			put("minecraft:overworld", new Pair<>("justmap.dim.overworld", new Identifier("textures/block/stone.png")));
			put("minecraft:the_nether", new Pair<>("justmap.dim.nether", new Identifier("textures/block/netherrack.png")));
			put("minecraft:the_end", new Pair<>("justmap.dim.the_end", new Identifier("textures/block/end_stone.png")));
		}
	};
	
	public final Screen parent;
	
	protected Pair<String, Identifier> info;
	
	protected int x, y, center;
	protected int paddingTop;
	protected int paddingBottom;
	
	protected MapScreen(Text title) {
		this(title, null);
	}
	
	public MapScreen(Text title, Screen parent) {
		super(title);
		this.parent = parent;
	}
	
	@Override
	protected void init() {
		int dimension = minecraft.player.dimension.getRawId();
		this.info = DIMENSION_INFO.getOrDefault(DimensionType.byRawId(dimension).toString(), null);
	}
	
	@Override
	public void render(int int_1, int int_2, float float_1) {
		renderBackground();
		for (Element e : children) {
			if (e instanceof Drawable) {
				((Drawable) e).render(int_1, int_2, float_1);
			}
		}
		renderForeground();
	}
	
	public void renderBackground() {
		fill(0, 0, width, height, 0x88444444);		
		drawBorders();
	}
	
	public void renderForeground() {}
	
	@Override
	public void onClose() {
		client.openScreen(parent);
	}
	
	public void renderTexture(int x, int y, int width, int height, String id) {
		renderTexture(x, y, width, height, new Identifier(id));
	}
	
	public void renderTexture(int x, int y, int width, int height, Identifier id) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		client.getTextureManager().bindTexture(id);
		RenderSystem.color4f(1, 1, 1, 1);
		builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
		
		builder.vertex(x, y + height, 0).texture(0, 1).color(255, 255, 255, 255).next();
		builder.vertex(x + width, y + height, 0).texture(1, 1).color(255, 255, 255, 255).next();
		builder.vertex(x + width, y, 0).texture(1, 0).color(255, 255, 255, 255).next();
		builder.vertex(x, y, 0).texture(0, 0).color(255, 255, 255, 255).next();
		
		tessellator.draw();
	}
	
	public void renderTexture(int x, int y, int width, int height, float u, float v, int r, int g, int b, int a, Identifier id) {
		Tessellator tessellator = Tessellator.getInstance();
		BufferBuilder builder = tessellator.getBuffer();
		client.getTextureManager().bindTexture(id);
		RenderSystem.color4f(1, 1, 1, 1);
		builder.begin(GL11.GL_QUADS, VertexFormats.POSITION_TEXTURE_COLOR);
	
		builder.vertex(x, y + height, 0).texture(0f, v).color(r, g, b, a).next();
		builder.vertex(x + width, y + height, 0).texture(u, v).color(r, g, b, a).next();
		builder.vertex(x + width, y, 0).texture(u, 0f).color(r, g, b, a).next();
		builder.vertex(x, y, 0).texture(0f, 0f).color(r, g, b, a).next();
	
		tessellator.draw();
	}
	
	public void renderTextureModal(int x, int y, int width, int height, int textureWidth, int textureHeight, Identifier id) {
		renderTexture(x, y, width, height, (float) width / (float) textureWidth, (float) height / (float) textureHeight, 255, 255, 255, 255, id);
	}
	
	public void renderTextureRepeating(int x, int y, int width, int height, int textureHeight, int textureWidth, String id) {
		renderTextureRepeating(x, y, width, height, textureHeight, textureWidth, new Identifier(id));
	}
	
	public void renderTextureRepeating(int x, int y, int width, int height, int textureHeight, int textureWidth, Identifier id) {
		for (int xp = 0; xp < width; xp += textureWidth) {
			int w = (xp + textureWidth < width) ? textureWidth : width - xp;
			for (int yp = 0; yp < height; yp += textureHeight) {
				int h = (yp + textureHeight < height) ? textureHeight : height - yp;
				renderTextureModal(x + xp, y + yp, w, h, textureWidth, textureHeight, id);
			}
		}
	}
	
	protected void drawBorders() {
		drawBorders(32, 32);
	}
	
	protected void drawBorders(int top, int bottom) {
		Identifier id = info.getSecond();
		if (id == null) {
			id = DEFAULT_IDENTIFIER;
		}

		renderTextureRepeating(0, 0, width, top, 16, 16, id);
		renderTextureRepeating(0, height - bottom, width, bottom, 16, 16, id);		
	}
	
	public String lang(String key) {
		return I18n.translate("justmap.gui." + key);
	}	
}