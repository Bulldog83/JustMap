package ru.bulldog.justmap.map.waypoint;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.map.data.WorldKey;
import ru.bulldog.justmap.util.Dimension;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.PosUtil;
import ru.bulldog.justmap.util.colors.ColorUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.math.RandomUtil;
import ru.bulldog.justmap.util.render.Image;

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
		new Icon(1, new ResourceLocation(JustMap.MODID, "textures/icon/circle.png"), 0xFFFF9000, 18, 18),
		new Icon(2, new ResourceLocation(JustMap.MODID, "textures/icon/cross.png"), 0xFFFF0000, 18, 18),
		new Icon(3, new ResourceLocation(JustMap.MODID, "textures/icon/diamond.png"), 0xFFE70CE3, 18, 18),
		new Icon(4, new ResourceLocation(JustMap.MODID, "textures/icon/moon.png"), 0xFFADE4F0, 18, 18),
		new Icon(5, new ResourceLocation(JustMap.MODID, "textures/icon/skull.png"), 0xFFFFFEFA, 18, 18),
		new Icon(6, new ResourceLocation(JustMap.MODID, "textures/icon/square.png"), 0xFF00D0FF, 18, 18),
		new Icon(7, new ResourceLocation(JustMap.MODID, "textures/icon/star.png"), 0xFFFFEE00, 18, 18),
		new Icon(8, new ResourceLocation(JustMap.MODID, "textures/icon/triangle.png"), 0xFF00FF00, 18, 18),
		new Icon(9, new ResourceLocation(JustMap.MODID, "textures/icon/house.png"), 0xFFEBA700, 18, 18),
		new Icon(10, new ResourceLocation(JustMap.MODID, "textures/icon/village.png"), 0xFFFC4A01, 18, 18),
		new Icon(11, new ResourceLocation("textures/item/iron_pickaxe.png"), 0xFFB0B0B0, 16, 16),
		new Icon(12, new ResourceLocation("textures/item/iron_axe.png"), 0xFFB0B0B0, 16, 16),
		new Icon(13, new ResourceLocation("textures/item/iron_hoe.png"), 0xFFB0B0B0, 16, 16),
		new Icon(14, new ResourceLocation("textures/item/iron_sword.png"), 0xFFB0B0B0, 16, 16),
		new Icon(15, new ResourceLocation("textures/item/wheat.png"), 0xFFFFEE91, 16, 16),
		new Icon(16, new ResourceLocation("textures/item/trident.png"), 0xFF54E1B2, 16, 16),
		new Icon(17, new ResourceLocation("textures/item/slime_ball.png"), 0xFF88DB71, 16, 16)
	};
	
	public static final Integer[] WAYPOINT_COLORS = new Integer[] {
		Colors.RED, Colors.GREEN, Colors.BLUE, Colors.DARK_RED,
		Colors.GOLD, Colors.YELLOW, Colors.DARK_GREEN, Colors.CYAN,
		Colors.DARK_AQUA, Colors.DARK_BLUE, Colors.PINK, Colors.MAGENTA,
		Colors.PURPLE
	};
	
	public String name = "";
	public BlockPos pos = new BlockPos(0, 0, 0);
	public WorldKey world;
	public int color;
	public boolean showAlways;
	public boolean hidden = false;
	public boolean tracking = true;
	public boolean render = true;
	public int showRange = 1000;
	
	private int icon = -1;
	
	public static void createOnDeath(WorldKey world, BlockPos pos) {
		Waypoint waypoint = new Waypoint();
		waypoint.world = world;
		waypoint.name = "Player Death";
		waypoint.pos = pos;
		waypoint.setIcon(Waypoint.getIcon(Icons.CROSS), Colors.RED);
		
		JustMap.LOGGER.info("Created Death waypoint at " + waypoint.pos.toShortString());
		
		WaypointKeeper.getInstance().addNew(waypoint);
		WaypointKeeper.getInstance().saveWaypoints();
	}
	
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
		waypoint.addProperty("show_always", this.showAlways);
		waypoint.addProperty("hidden", this.hidden);
		waypoint.addProperty("tracking", this.tracking);
		waypoint.addProperty("render", this.render);
		waypoint.addProperty("show_range", this.showRange);
		waypoint.addProperty("color", Integer.toHexString(this.color).toUpperCase());
		waypoint.addProperty("icon", this.icon);
		waypoint.add("world", this.world.toJson());
		waypoint.add("position", PosUtil.toJson(pos));
		
		return waypoint;
	}
	
	public static Waypoint fromJson(JsonObject jsonObject) {
		Waypoint waypoint = new Waypoint();

		JsonObject position = GsonHelper.getAsJsonObject(jsonObject, "position", new JsonObject());		
		waypoint.pos = PosUtil.fromJson(position);
		waypoint.name = GsonHelper.getAsString(jsonObject, "name", "Waypoint");
		waypoint.showAlways = GsonHelper.getAsBoolean(jsonObject, "show_always", false);
		waypoint.hidden = GsonHelper.getAsBoolean(jsonObject, "hidden", false);
		waypoint.tracking = GsonHelper.getAsBoolean(jsonObject, "tracking", true);
		waypoint.render = GsonHelper.getAsBoolean(jsonObject, "render", true);
		waypoint.showRange = GsonHelper.getAsInt(jsonObject, "show_range", 1000);
		waypoint.color = ColorUtil.parseHex(GsonHelper.getAsString(jsonObject, "color",
											Integer.toHexString(RandomUtil.getElement(WAYPOINT_COLORS))));
		waypoint.icon = GsonHelper.getAsInt(jsonObject, "icon", -1);
		
		if (jsonObject.has("dimension")) {
			try {
				waypoint.world = new WorldKey(Dimension.fromId(GsonHelper.getAsInt(jsonObject, "dimension", 0)));
			} catch (Exception ex) {
				ResourceLocation dimension = new ResourceLocation(GsonHelper.getAsString(jsonObject, "dimension", "unknown"));
				waypoint.world = new WorldKey(dimension);
			}
		} else {
			waypoint.world = WorldKey.fromJson(GsonHelper.getAsJsonObject(jsonObject, "world", new JsonObject()));
		}
		
		return waypoint;
	}
	
	public static class Icon extends Image {
		
		public final static ResourceLocation DEFAULT_ICON = new ResourceLocation(JustMap.MODID, "textures/icon/default.png");
		private final static NativeImage DEFAULT_TEXTURE = ImageUtil.loadImage(DEFAULT_ICON, 18, 18);
		
		public final int key;
		public final int color;
		
		private final static Map<Integer, Icon> coloredIcons = new HashMap<>();
		
		private Icon(int key, ResourceLocation icon, int color, int w, int h) {
			super(icon, ImageUtil.loadImage(icon, w, h));
			this.key = key;
			this.color = color;
		}
		
		private Icon(int key, ResourceLocation icon, NativeImage texture, int color) {
			super(icon, texture);
			this.key = key;
			this.color = color;
		}
		
		private static Icon coloredIcon(int color) {
			if(coloredIcons.containsKey(color)) {
				return coloredIcons.get(color);
			}
			
			NativeImage texture = new NativeImage(18, 18, false);
			
			texture.copyFrom(DEFAULT_TEXTURE);
			ImageUtil.applyColor(texture, color);
			
			Icon icon = new Icon(-1, DEFAULT_ICON, texture, color);
			coloredIcons.put(color, icon);
			
			return icon;
		}
		
		@Override
		public void bindTexture() {
			RenderSystem.setShaderTexture(0, getTexture());
		}
		
		@Override
		public void draw(PoseStack matrices, double x, double y, int w, int h) {
			this.bindTexture();
			this.draw(matrices, x, y, (float) w, (float) h);
		}
		
		private ResourceLocation getColoredTexture() {
			ResourceLocation id = new ResourceLocation(JustMap.MODID, String.format("wp_icon_%d", this.color));
			if (textureManager.getTexture(id) == null) {
				textureManager.register(id, new DynamicTexture(this.image));
			}
			return id;
		}
		
		public ResourceLocation getTexture() {
			if (this.key > 0) {
				return this.getId();
			}
			return this.getColoredTexture();
		}
	}
}
