package ru.bulldog.justmap.minimap.waypoint;

import java.util.HashMap;
import java.util.Map;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.systems.RenderSystem;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.Drawer;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.util.Identifier;
import net.minecraft.util.JsonHelper;
import net.minecraft.util.math.BlockPos;

public class Waypoint {
	
	public static class Icons {
		public final static int CIRCLE = 1;
		public final static int CROSS = 2;
		public final static int DIAMOND = 3;
		public final static int MOON = 4;
		public final static int SKULL = 5;
		public final static int SQUARE = 6;
		public final static int STAR = 7;
		public final static int TRIANGLE = 8;
		public final static int HOUSE = 9;
		public final static int VILLAGE = 10;
		public final static int PICK = 11;
		public final static int AXE = 12;
		public final static int HOE = 13;
		public final static int SWORD = 14;
		public final static int WHEAT = 15;
		public final static int TRIDENT = 16;
		public final static int SLIME = 17;
	}
	
	private static final Icon[] WAYPOINT_ICONS = new Icon[] {
		null,
		new Icon(1, new Identifier(JustMap.MODID, "textures/icon/circle.png"), 0xFFFF9000, 18, 18),
		new Icon(2, new Identifier(JustMap.MODID, "textures/icon/cross.png"), 0xFFFF0000, 18, 18),
		new Icon(3, new Identifier(JustMap.MODID, "textures/icon/diamond.png"), 0xFFE70CE3, 18, 18),
		new Icon(4, new Identifier(JustMap.MODID, "textures/icon/moon.png"), 0xFFADE4F0, 18, 18),
		new Icon(5, new Identifier(JustMap.MODID, "textures/icon/skull.png"), 0xFFFFFEFA, 18, 18),
		new Icon(6, new Identifier(JustMap.MODID, "textures/icon/square.png"), 0xFF00D0FF, 18, 18),
		new Icon(7, new Identifier(JustMap.MODID, "textures/icon/star.png"), 0xFFFFEE00, 18, 18),
		new Icon(8, new Identifier(JustMap.MODID, "textures/icon/triangle.png"), 0xFF00FF00, 18, 18),
		new Icon(9, new Identifier(JustMap.MODID, "textures/icon/house.png"), 0xFFEBA700, 18, 18),
		new Icon(10, new Identifier(JustMap.MODID, "textures/icon/village.png"), 0xFFFC4A01, 18, 18),
		new Icon(11, new Identifier("textures/item/iron_pickaxe.png"), 0xFFB0B0B0, 16, 16),
		new Icon(12, new Identifier("textures/item/iron_axe.png"), 0xFFB0B0B0, 16, 16),
		new Icon(13, new Identifier("textures/item/iron_hoe.png"), 0xFFB0B0B0, 16, 16),
		new Icon(14, new Identifier("textures/item/iron_sword.png"), 0xFFB0B0B0, 16, 16),
		new Icon(15, new Identifier("textures/item/wheat.png"), 0xFFFFEE91, 16, 16),
		new Icon(16, new Identifier("textures/item/trident.png"), 0xFF54E1B2, 16, 16),
		new Icon(17, new Identifier("textures/item/slime_ball.png"), 0xFF88DB71, 16, 16)
	};
	
	public static final Integer[] WAYPOINT_COLORS = new Integer[] {
		Colors.RED, Colors.GREEN, Colors.BLUE, Colors.DARK_RED,
		Colors.GOLD, Colors.YELLOW, Colors.DARK_GREEN, Colors.CYAN,
		Colors.DARK_AQUA, Colors.DARK_BLUE, Colors.PINK, Colors.MAGENTA,
		Colors.PURPLE
	};
	
	public String name = "";
	public BlockPos pos = new BlockPos(0, 0, 0);
	public int dimension;
	public int color;
	public boolean showAlways;
	public boolean hidden = false;
	public boolean tracking = true;
	public boolean render = true;
	public int showRange = 1000;
	
	private int icon = -1;
	
	public boolean isVisible() {
		return !hidden || showAlways;
	}
	
	public void setIcon(Icon icon, int color) {
		if (icon.key > 0) {
			this.icon = icon.key;
			this.color = icon.color;
		} else {
			this.color = color;
		}
	}
	
	public Icon getIcon() {
		if (this.icon > 0) {
			return getIcon(this.icon);
		}
		
		return Icon.coloredIcon(this.color);
	}
	
	public static Icon getIcon(int id) {
		if (id > 0 && id < WAYPOINT_ICONS.length) {
			return WAYPOINT_ICONS[id];
		}
		
		return null;
	}
	
	public static Icon getColoredIcon(int color) {
		return Icon.coloredIcon(color);
	}
	
	public static int amountIcons() {
		return WAYPOINT_ICONS.length - 1;
	}
	
	public JsonElement toJson() {
		JsonObject waypoint = new JsonObject();
		
		waypoint.addProperty("name", this.name);
		waypoint.addProperty("dimension", this.dimension);
		waypoint.addProperty("show_always", this.showAlways);
		waypoint.addProperty("hidden", this.hidden);
		waypoint.addProperty("show_range", this.showRange);
		waypoint.addProperty("color", Integer.toHexString(this.color).toUpperCase());
		waypoint.addProperty("icon", this.icon);
		
		JsonObject position = new JsonObject();
		position.addProperty("x", pos.getX());
		position.addProperty("y", pos.getY());
		position.addProperty("z", pos.getZ());
		
		waypoint.add("position", position);
		
		return waypoint;
	}
	
	public static Waypoint fromJson(JsonObject jsonObject) {
		Waypoint waypoint = new Waypoint();

		JsonObject position = (JsonObject) jsonObject.get("position");		
		waypoint.pos = new BlockPos(JsonHelper.getInt(position, "x"),
									JsonHelper.getInt(position, "y"),
									JsonHelper.getInt(position, "z"));
		waypoint.dimension = JsonHelper.getInt(jsonObject, "dimension");
		waypoint.icon = JsonHelper.getInt(jsonObject, "icon");
		waypoint.showRange = JsonHelper.getInt(jsonObject, "show_range");
		waypoint.color = ColorUtil.parseHex(JsonHelper.getString(jsonObject, "color"));
		waypoint.hidden = JsonHelper.getBoolean(jsonObject, "hidden");
		waypoint.showAlways = JsonHelper.getBoolean(jsonObject, "show_always");
		
		return waypoint;
	}
	
	public static class Icon extends Sprite {
		
		public final static Identifier DEFAULT_ICON = new Identifier(JustMap.MODID, "textures/icon/default.png");
		
		public final int key;
		public final int color;
		
		private final TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
		
		private final static SpriteAtlasTexture ATLAS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/waypoint_icons.png"));
		
		private final static Map<Integer, Icon> coloredIcons = new HashMap<>();
		
		private Icon(int key, Identifier icon, int color, int w, int h) {
			super(ATLAS, new Sprite.Info(icon, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(icon, w, h));
			this.key = key;
			this.color = color;
		}
		
		private Icon(int key, Identifier icon, NativeImage texture, int color, int w, int h) {
			super(ATLAS, new Sprite.Info(icon, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, texture);
			this.key = key;
			this.color = color;
		}
		
		private static Icon coloredIcon(int color) {
			if(!coloredIcons.containsKey(color)) {
				NativeImage texture = ImageUtil.applyColor(ImageUtil.loadImage(DEFAULT_ICON, 18, 18), color);
				coloredIcons.put(color, new Icon(-1, DEFAULT_ICON, texture, color, 18, 18));
			}
			
			return coloredIcons.get(color);
		}
		
		public void draw(int x, int y, int w, int h) {
			if (this.key > 0) {
				textureManager.bindTexture(this.getTexture());
			} else {
				textureManager.bindTexture(getColoredTexture());
			}			
			RenderSystem.enableAlphaTest();
			RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
			Drawer.blit(x, y, 0, w, h, this);
		}
		
		public void draw(int x, int y, int size) {
			this.draw(x, y, size, size);
		}
		
		public void draw(int x, int y) {
			this.draw(x, y, this.getWidth(), this.getHeight());
		}
		
		private Identifier getColoredTexture() {
			Identifier id = new Identifier(JustMap.MODID, String.format("wp_icon_%d", this.color));
			if (textureManager.getTexture(id) == null) {
				textureManager.registerTexture(id, new NativeImageBackedTexture(this.images[0]));
			}
			return id;
		}
		
		public Identifier getTexture() {
			if (this.key > 0) {
				return this.getId();
			}
			return getColoredTexture();
		}
	}
}
