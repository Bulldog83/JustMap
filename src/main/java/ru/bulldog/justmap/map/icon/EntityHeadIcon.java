package ru.bulldog.justmap.map.icon;

import com.google.common.collect.Maps;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.monster.Monster;
import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.Image;
import ru.bulldog.justmap.util.render.RenderUtil;
import ru.bulldog.justmap.util.storage.StorageUtil;

public class EntityHeadIcon extends Image {
	
	private final static Map<ResourceLocation, EntityHeadIcon> ICONS = Maps.newHashMap();
	
	public static EntityHeadIcon getIcon(Entity entity) {
		ResourceLocation id = EntityType.getKey(entity.getType());
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
	private final boolean solid;
	
	private EntityHeadIcon(ResourceLocation id, ResourceLocation texture, int w, int h) {
		this(id, texture, ImageUtil.loadImage(texture, w, h));
	}
	
	private EntityHeadIcon(ResourceLocation id, ResourceLocation texture, NativeImage image) {
		super(texture, image);
		this.solid = isSolid();
		this.id = id;
	}

	@Override
	public void draw(PoseStack matrices, double x, double y, int w, int h) {
		if (ClientSettings.showIconsOutline) {
			double thickness = ClientSettings.entityOutlineSize;
			if (solid) {
				RenderUtil.fill(matrices, x - thickness / 2, y - thickness / 2, w + thickness, h + thickness, this.color);
			} else {
				bindOutline();
				RenderUtil.draw(matrices, x - thickness / 2, y - thickness / 2, (float) (w + thickness), (float) (h + thickness));
			}
		}
		draw(matrices, x, y, (float) w, (float) h);
	}
	
	private void bindOutline() {
		if (outlineId == null) {
			NativeImage outline = ImageUtil.generateOutline(image, width, height, color);
			DynamicTexture outTexture = new DynamicTexture(outline);
			outlineId = textureManager.register(String.format("%s_%s_outline", id.getNamespace(), id.getPath()), outTexture);
		}
		RenderUtil.bindTexture(outlineId);
	}
	
	private boolean isSolid() {
		NativeImage icon = this.image;
		
		int width = icon.getWidth();
		int height = icon.getHeight();
		
		boolean solid = true;
		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				int alpha = (icon.getPixelRGBA(i, j) >> 24) & 255;
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
		ResourceLocation textureId = textureManager.register(prefix, new DynamicTexture(iconImage));
		EntityHeadIcon icon = new EntityHeadIcon(entityId, textureId, iconImage);
		return registerIcon(entity, entityId, icon);
	}
	
	private static EntityHeadIcon registerIcon(Entity entity, ResourceLocation entityId, EntityHeadIcon icon) {
		if (entity instanceof Monster) {
			icon.color = Colors.DARK_RED;
		} else if (entity instanceof TamableAnimal tameable) {
			icon.color = tameable.isTame() ? Colors.GREEN : Colors.YELLOW;
		} else {
			icon.color = Colors.YELLOW;
		}
		
		ICONS.put(entityId, icon);
		
		return icon;
	}
}
