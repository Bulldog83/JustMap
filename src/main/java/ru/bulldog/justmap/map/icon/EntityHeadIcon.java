package ru.bulldog.justmap.map.icon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.SpriteAtlas;
import ru.bulldog.justmap.util.ColorUtil;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;
import ru.bulldog.justmap.util.math.Line.Point;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.Identifier;

public class EntityHeadIcon extends AbstractIcon {
	
	private final static Map<Identifier, EntityHeadIcon> ICONS = new HashMap<>();
	
	public static EntityHeadIcon getIcon(Entity entity) {
		Identifier id = EntityType.getId(entity.getType());
		if (ICONS.containsKey(id)) {
			return ICONS.get(id);
		} else {
			Identifier iconId = iconId(id);
			if (ImageUtil.imageExists(iconId)) {
				return registerIcon(entity, id, iconId);
			}
		}
		
		return null;
	}
	
	private final Identifier id;
	private Identifier outlineId;
	private int color = Colors.LIGHT_GRAY;
	private boolean solid;
	
	private EntityHeadIcon(Identifier id, Identifier texture, int w, int h) {
		super(SpriteAtlas.ENTITY_HEAD_ICONS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	
		this.solid = this.isSolid();
		this.id = id;
	}

	@Override
	public void draw(double x, double y, int w, int h) {
		MatrixStack matrix = new MatrixStack();
		this.draw(matrix, x, y, (float) w, (float) h);
	}
	
	@Override
	public void draw(MatrixStack matrix, double x, double y, int w, int h) {
		if (ClientParams.showIconsOutline) {
			double thickness = ClientParams.entityOutlineSize;
			if (solid) {
				RenderUtil.fill(matrix, x - thickness / 2, y - thickness / 2, w + thickness, h + thickness, this.color);
			} else {
				this.bindOutline();
				RenderUtil.draw(x - thickness / 2, y - thickness / 2, (float) (w + thickness), (float) (h + thickness));
			}
		}
		textureManager.bindTexture(this.getId());		
		this.draw(matrix, x, y, (float) w, (float) h);
	}
	
	private void bindOutline() {
		if (outlineId == null) {
			NativeImageBackedTexture outTexture = new NativeImageBackedTexture(this.generateOutline());
			this.outlineId = textureManager.registerDynamicTexture(String.format("%s_%s_outline", this.id.getNamespace(), this.id.getPath()), outTexture);
		}
		textureManager.bindTexture(outlineId);
	}
	
	private boolean isSolid() {
		NativeImage icon = this.images[0];
		
		int width = icon.getWidth();
		int height = icon.getHeight();
		
		boolean solid = true;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int alpha = (icon.getPixelColor(i, j) >> 24) & 255;
				solid = alpha > 0;
				if (!solid) break;
			}
		}
		
		return solid;
	}
	
	private NativeImage generateOutline() {
		NativeImage icon = this.images[0];
		
		int width = icon.getWidth();
		int height = icon.getHeight();
		
		NativeImage outline = new NativeImage(width + 4, height + 4, false);
		ImageUtil.fillImage(outline, Colors.TRANSPARENT);
		
		int outWidth = outline.getWidth();
		int outHeight = outline.getHeight();
		
		int outlineColor = ColorUtil.toABGR(this.color);
		
		List<Point> outlinePixels = new ArrayList<>();
		for (int x = 0; x < width; x++) {
			int left = x - 1;
			int right = x + 1;
			for (int y = 0; y < height; y++) {
				int alpha = (icon.getPixelColor(x, y) >> 24) & 255;
				if (alpha == 0) continue;
				
				outlinePixels.add(new Point(x + 2, y + 2));
				
				int top = y - 1;
				int bottom = y + 1;					
				if (top >= 0) {
					alpha = (icon.getPixelColor(x, top) >> 24) & 255;
					if (alpha == 0) {
						Point pixel = new Point(x + 2, y);
						if (!outlinePixels.contains(pixel)) {
							outlinePixels.add(pixel);
							outlinePixels.add(new Point(x + 2, y + 1));
						}
					}
					if (left >= 0) {
						alpha = (icon.getPixelColor(left, top) >> 24) & 255;
						if (alpha == 0) {
							Point pixel = new Point(x, y);
							if (!outlinePixels.contains(pixel)) {
								outlinePixels.add(pixel);
								outlinePixels.add(new Point(x, y + 1));
								outlinePixels.add(new Point(x + 1, y));
								outlinePixels.add(new Point(x + 1, y + 1));
							}
						}
					}
					if (right < width) {
						alpha = (icon.getPixelColor(right, top) >> 24) & 255;
						if (alpha == 0) {
							Point pixel = new Point(right + 2, y);
							if (!outlinePixels.contains(pixel)) {
								outlinePixels.add(pixel);
								outlinePixels.add(new Point(right + 2, y + 1));
								outlinePixels.add(new Point(right + 3, y));
								outlinePixels.add(new Point(right + 3, y + 1));
							}
						}
					}
				} else if (y == 0){
					Point pixel = new Point(x + 2, 0);
					if (!outlinePixels.contains(pixel)) {
						outlinePixels.add(pixel);
						outlinePixels.add(new Point(x + 2, 1));
					}
				}
				if (bottom < height) {
					alpha = (icon.getPixelColor(x, bottom) >> 24) & 255;
					if (alpha == 0) {
						Point pixel = new Point(x + 2, bottom + 1);
						if (!outlinePixels.contains(pixel)) {
							outlinePixels.add(pixel);
							outlinePixels.add(new Point(x + 2, bottom + 2));
						}
					}
					if (left >= 0) {
						alpha = (icon.getPixelColor(left, bottom) >> 24) & 255;
						if (alpha == 0) {
							Point pixel = new Point(x, bottom + 2);
							if (!outlinePixels.contains(pixel)) {
								outlinePixels.add(pixel);
								outlinePixels.add(new Point(x, bottom + 3));
								outlinePixels.add(new Point(x + 1, bottom + 2));
								outlinePixels.add(new Point(x + 1, bottom + 3));
							}
						}
					}
					if (right < width) {
						alpha = (icon.getPixelColor(right, bottom) >> 24) & 255;
						if (alpha == 0) {
							Point pixel = new Point(right + 2, bottom + 2);
							if (!outlinePixels.contains(pixel)) {
								outlinePixels.add(pixel);
								outlinePixels.add(new Point(right + 2, bottom + 3));
								outlinePixels.add(new Point(right + 3, bottom + 2));
								outlinePixels.add(new Point(right + 3, bottom + 3));
							}
						}
					}
				} else if (y == height - 1) {
					Point pixel = new Point(x + 2, outHeight - 1);
					if (!outlinePixels.contains(pixel)) {
						outlinePixels.add(pixel);
						outlinePixels.add(new Point(x + 2, outHeight - 2));
					}
				}
				if (left >= 0) {
					alpha = (icon.getPixelColor(left, y) >> 24) & 255;
					if (alpha == 0) {
						Point pixel = new Point(x, y + 2);
						if (!outlinePixels.contains(pixel)) {
							outlinePixels.add(pixel);
							outlinePixels.add(new Point(x + 1, y + 2));
						}
					}
				} else if (x == 0) {
					Point pixel = new Point(0, y + 2);
					if (!outlinePixels.contains(pixel)) {
						outlinePixels.add(pixel);
						outlinePixels.add(new Point(1, y + 2));
					}
				}
				if (right < width) {
					alpha = (icon.getPixelColor(right, y) >> 24) & 255;
					if (alpha == 0) {
						Point pixel = new Point(right + 1, y + 2);
						if (!outlinePixels.contains(pixel)) {
							outlinePixels.add(pixel);
							outlinePixels.add(new Point(right + 2, y + 2));
						}
					}
				} else if (x == width - 1) {
					Point pixel = new Point(outWidth - 1, y + 2);
					if (!outlinePixels.contains(pixel)) {
						outlinePixels.add(pixel);
						outlinePixels.add(new Point(outWidth - 2, y + 2));
					}
				}
			}
		}
		outlinePixels.forEach(pixel -> {
			outline.setPixelColor((int) pixel.x, (int) pixel.y, outlineColor);
		});
		
		return outline;
	}
	
	private static Identifier iconId(Identifier id) {
		String path = String.format("textures/minimap/entities/%s.png", id.getPath());
		return new Identifier(id.getNamespace(), path);
	}
	
	private static EntityHeadIcon registerIcon(Entity entity, Identifier entityId, Identifier texture) {
		EntityHeadIcon icon = new EntityHeadIcon(entityId, texture, 32, 32);
		if (entity instanceof HostileEntity) {
			icon.color = Colors.DARK_RED;
		} else if (entity instanceof TameableEntity) {
			TameableEntity tameable = (TameableEntity) entity;
			icon.color = tameable.isTamed() ? Colors.GREEN : Colors.YELLOW;
		} else {
			icon.color = Colors.YELLOW;
		}
		
		ICONS.put(entityId, icon);
		
		return icon;
	}
}
