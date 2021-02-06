package ru.bulldog.justmap.map.icon;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.Image;
import ru.bulldog.justmap.util.render.RenderUtil;
import ru.bulldog.justmap.util.storage.StorageUtil;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.util.math.PoseStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.TameableEntity;
import net.minecraft.util.ResourceLocation;

public class EntityHeadIcon extends Image {
	
	private final static Map<ResourceLocation, EntityHeadIcon> ICONS = new HashMap<>();
	
	public static EntityHeadIcon getIcon(Entity entity) {
		ResourceLocation id = EntityType.getId(entity.getType());
		if (ICONS.containsKey(id)) {
			return ICONS.get(id);
		} else {
			File iconsDir = StorageUtil.iconsDir();
			File iconPng = new File(iconsDir, String.format("%s/%s.png", id.getNamespace(), id.getPath()));
			if (iconPng.exists()) {
				return registerIcon(entity, id, iconPng);
			} else {
				ResourceLocation iconId = iconId(id);
				if (ImageUtil.imageExists(iconId)) {
					return registerIcon(entity, id, iconId);
				}
			}
		}
		
		return null;
	}
	
	private final ResourceLocation id;
	private ResourceLocation outlineId;
	private int color = Colors.LIGHT_GRAY;
	private boolean solid;
	
	private EntityHeadIcon(ResourceLocation id, ResourceLocation texture, int w, int h) {
		this(id, texture, ImageUtil.loadImage(texture, w, h));
	}
	
	private EntityHeadIcon(ResourceLocation id, ResourceLocation texture, NativeImage image) {
		super(texture, image);
		
		this.solid = this.isSolid();
		this.id = id;
	}

	@Override
	public void draw(PoseStack matrices, double x, double y, int w, int h) {
		if (ClientSettings.showIconsOutline) {
			double thickness = ClientSettings.entityOutlineSize;
			if (solid) {
				RenderUtil.fill(matrices, x - thickness / 2, y - thickness / 2, w + thickness, h + thickness, this.color);
			} else {
				this.bindOutline();
				RenderUtil.draw(matrices, x - thickness / 2, y - thickness / 2, (float) (w + thickness), (float) (h + thickness));
			}
		}
		this.draw(matrices, x, y, (float) w, (float) h);
	}
	
	private void bindOutline() {
		if (outlineId == null) {
			NativeImage outline = ImageUtil.generateOutline(image, width, height, color);
			NativeImageBackedTexture outTexture = new NativeImageBackedTexture(outline);
			this.outlineId = textureManager.registerDynamicTexture(String.format("%s_%s_outline", this.id.getNamespace(), this.id.getPath()), outTexture);
		}
		textureManager.bindTexture(outlineId);
	}
	
	private boolean isSolid() {
		NativeImage icon = this.image;
		
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
	
	private static ResourceLocation iconId(ResourceLocation id) {
		String path = String.format("textures/minimap/entities/%s.png", id.getPath());
		return new ResourceLocation(id.getNamespace(), path);
	}
	
	private static EntityHeadIcon registerIcon(Entity entity, ResourceLocation entityId, ResourceLocation texture) {
		EntityHeadIcon icon = new EntityHeadIcon(entityId, texture, 32, 32);
		return registerIcon(entity, entityId, icon);
	}
	
	private static EntityHeadIcon registerIcon(Entity entity, ResourceLocation entityId, File image) {
		NativeImage iconImage = ImageUtil.loadImage(image, 32, 32);
		String prefix = String.format("icon_%s_%s", entityId.getNamespace(), entityId.getPath());
		ResourceLocation textureId = textureManager.registerDynamicTexture(prefix, new NativeImageBackedTexture(iconImage));
		EntityHeadIcon icon = new EntityHeadIcon(entityId, textureId, iconImage);
		return registerIcon(entity, entityId, icon);
	}
	
	private static EntityHeadIcon registerIcon(Entity entity, ResourceLocation entityId, EntityHeadIcon icon) {
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
