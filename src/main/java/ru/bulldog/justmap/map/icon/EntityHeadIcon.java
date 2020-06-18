package ru.bulldog.justmap.map.icon;

import java.util.HashMap;
import java.util.Map;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.SpriteAtlas;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.DrawHelper;

import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
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
				return registerIcon(id, iconId);
			}
		}
		
		return null;
	}
	
	private final Identifier id;
	private Identifier outlineId;
	
	private EntityHeadIcon(Identifier id, Identifier texture, int w, int h) {
		super(SpriteAtlas.ENTITY_HEAD_ICONS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	
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
			this.bindOutline();
			DrawHelper.draw(x - 1, y - 1, w + 2, h + 2);
			//DrawHelper.fill(matrix, x - 0.5, y - 0.5, x + w + 0.5, y + h + 0.5, Colors.LIGHT_GRAY);
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
	
	private NativeImage generateOutline() {
		NativeImage icon = this.images[0];
		
		int width = icon.getWidth();
		int height = icon.getHeight();
		
		NativeImage outline = new NativeImage(width + 4, height + 4, false);
		ImageUtil.fillImage(outline, Colors.TRANSPARENT);
		
		int outWidth = outline.getWidth();
		int outHeight = outline.getHeight();
		
		int outlineColor = Colors.LIGHT_GRAY;
		
		boolean solidBorder = true;
		for (int i = 0; i < width; i++) {
			int alpha = (icon.getPixelRgba(i, 0) >> 24) & 255;
			solidBorder = alpha > 0;
			if (!solidBorder) break;
			
			alpha = (icon.getPixelRgba(i, height - 1) >> 24) & 255;
			solidBorder = alpha > 0;
			if (!solidBorder) break;
		}		
		if (solidBorder) {
			for (int i = 0; i < height; i++) {
				int alpha = (icon.getPixelRgba(0, i) >> 24) & 255;
				solidBorder = alpha > 0;
				if (!solidBorder) break;
				
				alpha = (icon.getPixelRgba(width - 1, i) >> 24) & 255;
				solidBorder = alpha > 0;
				if (!solidBorder) break;
			}
		}
		if (solidBorder) {
			for (int i = 0; i < outWidth; i++) {
				outline.setPixelRgba(i, 0, outlineColor);
				outline.setPixelRgba(i, 1, outlineColor);
				outline.setPixelRgba(i, outHeight - 1, outlineColor);
				outline.setPixelRgba(i, outHeight - 2, outlineColor);
			}
			for (int i = 0; i < outHeight; i++) {
				outline.setPixelRgba(0, i, outlineColor);
				outline.setPixelRgba(1, i, outlineColor);
				outline.setPixelRgba(outWidth - 1, i, outlineColor);
				outline.setPixelRgba(outWidth - 2, i, outlineColor);
			}
		} else {		
			for (int x = 0; x < width; x++) {
				int left = x - 1;
				int right = x + 1;
				for (int y = 0; y < height; y++) {
					int alpha = (icon.getPixelRgba(x, y) >> 24) & 255;
					if (alpha == 0) continue;
					
					int top = y - 1;
					int bottom = y + 1;					
					if (top >= 0) {
						alpha = (icon.getPixelRgba(x, top) >> 24) & 255;
						if (alpha == 0) {
							outline.setPixelRgba(x + 2, y, outlineColor);
							outline.setPixelRgba(x + 3, y + 1, outlineColor);
						}
						if (left >= 0) {
							alpha = (icon.getPixelRgba(left, top) >> 24) & 255;
							if (alpha == 0) {
								outline.setPixelRgba(x, y, outlineColor);
								outline.setPixelRgba(x, y + 1, outlineColor);
								outline.setPixelRgba(x + 1, y, outlineColor);
								outline.setPixelRgba(x + 1, y + 1, outlineColor);
							}
						}
						if (right < width) {
							alpha = (icon.getPixelRgba(right, top) >> 24) & 255;
							if (alpha == 0) {
								outline.setPixelRgba(right + 2, y, outlineColor);
								outline.setPixelRgba(right + 2, y + 1, outlineColor);
								outline.setPixelRgba(right + 3, y, outlineColor);
								outline.setPixelRgba(right + 3, y + 1, outlineColor);
							}
						}
					} else if (y == 0){
						outline.setPixelRgba(x + 2, 0, outlineColor);
						outline.setPixelRgba(x + 3, 1, outlineColor);
					}
					if (bottom < height) {
						alpha = (icon.getPixelRgba(x, bottom) >> 24) & 255;
						if (alpha == 0) {
							outline.setPixelRgba(x + 2, bottom + 2, outlineColor);
							outline.setPixelRgba(x + 3, bottom + 3, outlineColor);
						}
						if (left >= 0) {
							alpha = (icon.getPixelRgba(left, bottom) >> 24) & 255;
							if (alpha == 0) {
								outline.setPixelRgba(x, bottom + 2, outlineColor);
								outline.setPixelRgba(x, bottom + 3, outlineColor);
								outline.setPixelRgba(x + 1, bottom + 2, outlineColor);
								outline.setPixelRgba(x + 1, bottom + 3, outlineColor);
							}
						}
						if (right < width) {
							alpha = (icon.getPixelRgba(right, bottom) >> 24) & 255;
							if (alpha == 0) {
								outline.setPixelRgba(right + 2, bottom + 2, outlineColor);
								outline.setPixelRgba(right + 2, bottom + 3, outlineColor);
								outline.setPixelRgba(right + 3, bottom + 2, outlineColor);
								outline.setPixelRgba(right + 3, bottom + 3, outlineColor);
							}
						}
					} else if (y == height - 1) {
						outline.setPixelRgba(x + 2, outHeight - 1, outlineColor);
						outline.setPixelRgba(x + 3, outHeight - 2, outlineColor);
					}
					if (left >= 0) {
						alpha = (icon.getPixelRgba(left, y) >> 24) & 255;
						if (alpha == 0) {
							outline.setPixelRgba(x, y + 2, outlineColor);
							outline.setPixelRgba(x + 1, y + 3, outlineColor);
						}
					} else if (x == 0) {
						outline.setPixelRgba(0, y + 2, outlineColor);
						outline.setPixelRgba(1, y + 3, outlineColor);
					}
					if (right < width) {
						alpha = (icon.getPixelRgba(right, y) >> 24) & 255;
						if (alpha == 0) {
							outline.setPixelRgba(right + 2, y + 2, outlineColor);
							outline.setPixelRgba(right + 3, y + 3, outlineColor);
						}
					} else if (x == width - 1) {
						outline.setPixelRgba(outWidth - 1, y + 2, outlineColor);
						outline.setPixelRgba(outWidth - 2, y + 3, outlineColor);
					}
				}
			}
		}
		
		return outline;
	}
	
	private static Identifier iconId(Identifier id) {
		String path = String.format("textures/minimap/entities/%s.png", id.getPath());
		return new Identifier(id.getNamespace(), path);
	}
	
	private static EntityHeadIcon registerIcon(Identifier entityId, Identifier texture) {
		EntityHeadIcon icon = new EntityHeadIcon(entityId, texture, 32, 32);
		ICONS.put(entityId, icon);
		
		return icon;
	}
}
