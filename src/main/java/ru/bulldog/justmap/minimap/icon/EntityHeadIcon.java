package ru.bulldog.justmap.minimap.icon;

import java.util.HashMap;
import java.util.Map;

import com.mojang.blaze3d.systems.RenderSystem;
import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.config.Params;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.DrawHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.Sprite;
import net.minecraft.client.texture.SpriteAtlasTexture;
import net.minecraft.client.texture.TextureManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public class EntityHeadIcon extends Sprite {
	
	private final static SpriteAtlasTexture ATLAS = new SpriteAtlasTexture(new Identifier(JustMap.MODID, "textures/atlas/entity_head_icons.png"));
	private final static Map<Identifier, EntityHeadIcon> ICONS = new HashMap<>();
	
	private static TextureManager textureManager = MinecraftClient.getInstance().getTextureManager();
	
	private EntityHeadIcon(Identifier texture, int w, int h) {
		super(ATLAS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	}
	
	private EntityHeadIcon(Identifier id, NativeImage image, int w, int h, int x, int y) {
		super(ATLAS, new Sprite.Info(id, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, x, y, image);
		textureManager.registerTexture(id, new NativeImageBackedTexture(image));
	}
	
	public static EntityHeadIcon getIcon(Entity entity) {
		Identifier id = EntityType.getId(entity.getType());
		return ICONS.get(id);
	}
	
	public void draw(int x, int y, int w, int h) {		
		if (Params.showIconsOutline) {
			DrawHelper.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF444444);
		}
		textureManager.bindTexture(this.getId());
		RenderSystem.enableAlphaTest();
		RenderSystem.color4f(1.0F, 1.0F, 1.0F, 1.0F);
		DrawHelper.blit(x, y, 0, w, h, this);
	}
	
	public void draw(int x, int y, int size) {
		this.draw(x, y, size, size);
	}
	
	private static void registerIcon(Identifier id) {
		String path = String.format("textures/entities/%s.png", id.getPath());
		registerIcon(id, new Identifier(JustMap.MODID, path), 32, 32);
	}
	
	public static void registerIcon(Identifier entityId, Identifier image, int imageWidth, int imageHeight) {
		ICONS.put(entityId, new EntityHeadIcon(image, imageWidth, imageHeight));
	}
	
	public static void registerIcon(Identifier entityId, NativeImage image, int w, int h, int x, int y) {
		ICONS.put(entityId, new EntityHeadIcon(entityId, image, w, h, x, y));
	}
	
	static {
		registerIcon(EntityType.getId(EntityType.BAT));
		registerIcon(EntityType.getId(EntityType.BEE));
		registerIcon(EntityType.getId(EntityType.BLAZE));
		registerIcon(EntityType.getId(EntityType.CAT));
		registerIcon(EntityType.getId(EntityType.CAVE_SPIDER));
		registerIcon(EntityType.getId(EntityType.CHICKEN));
		registerIcon(EntityType.getId(EntityType.COD));
		registerIcon(EntityType.getId(EntityType.COW));
		registerIcon(EntityType.getId(EntityType.CREEPER));
		registerIcon(EntityType.getId(EntityType.DOLPHIN));
		registerIcon(EntityType.getId(EntityType.DONKEY));
		registerIcon(EntityType.getId(EntityType.DROWNED));
		registerIcon(EntityType.getId(EntityType.ELDER_GUARDIAN));
		registerIcon(EntityType.getId(EntityType.ENDER_DRAGON));
		registerIcon(EntityType.getId(EntityType.ENDERMAN));
		registerIcon(EntityType.getId(EntityType.ENDERMITE));
		registerIcon(EntityType.getId(EntityType.EVOKER));
		registerIcon(EntityType.getId(EntityType.FOX));
		registerIcon(EntityType.getId(EntityType.GHAST));
		registerIcon(EntityType.getId(EntityType.GIANT));
		registerIcon(EntityType.getId(EntityType.GUARDIAN));
		registerIcon(EntityType.getId(EntityType.HORSE));
		registerIcon(EntityType.getId(EntityType.HUSK));
		registerIcon(EntityType.getId(EntityType.ILLUSIONER));
		registerIcon(EntityType.getId(EntityType.IRON_GOLEM));
		registerIcon(EntityType.getId(EntityType.LLAMA));
		registerIcon(EntityType.getId(EntityType.MAGMA_CUBE));
		registerIcon(EntityType.getId(EntityType.MOOSHROOM));
		registerIcon(EntityType.getId(EntityType.MULE));
		registerIcon(EntityType.getId(EntityType.OCELOT));
		registerIcon(EntityType.getId(EntityType.PANDA));
		registerIcon(EntityType.getId(EntityType.PARROT));
		registerIcon(EntityType.getId(EntityType.PHANTOM));
		registerIcon(EntityType.getId(EntityType.PIG));
		registerIcon(EntityType.getId(EntityType.PILLAGER));
		registerIcon(EntityType.getId(EntityType.POLAR_BEAR));
		registerIcon(EntityType.getId(EntityType.PUFFERFISH));
		registerIcon(EntityType.getId(EntityType.RABBIT));
		registerIcon(EntityType.getId(EntityType.RAVAGER));
		registerIcon(EntityType.getId(EntityType.SALMON));
		registerIcon(EntityType.getId(EntityType.SHEEP));
		registerIcon(EntityType.getId(EntityType.SHULKER));
		registerIcon(EntityType.getId(EntityType.SILVERFISH));
		registerIcon(EntityType.getId(EntityType.SKELETON));
		registerIcon(EntityType.getId(EntityType.SKELETON_HORSE));
		registerIcon(EntityType.getId(EntityType.SLIME));
		registerIcon(EntityType.getId(EntityType.SNOW_GOLEM));
		registerIcon(EntityType.getId(EntityType.SPIDER));
		registerIcon(EntityType.getId(EntityType.SQUID));
		registerIcon(EntityType.getId(EntityType.STRAY));
		registerIcon(EntityType.getId(EntityType.TRADER_LLAMA));
		registerIcon(EntityType.getId(EntityType.TROPICAL_FISH));
		registerIcon(EntityType.getId(EntityType.TURTLE));
		registerIcon(EntityType.getId(EntityType.VEX));
		registerIcon(EntityType.getId(EntityType.VILLAGER));
		registerIcon(EntityType.getId(EntityType.VINDICATOR));
		registerIcon(EntityType.getId(EntityType.WANDERING_TRADER));
		registerIcon(EntityType.getId(EntityType.WITCH));
		registerIcon(EntityType.getId(EntityType.WITHER));
		registerIcon(EntityType.getId(EntityType.WITHER_SKELETON));
		registerIcon(EntityType.getId(EntityType.WOLF));
		registerIcon(EntityType.getId(EntityType.ZOMBIE));
		registerIcon(EntityType.getId(EntityType.ZOMBIE_HORSE));
		registerIcon(EntityType.getId(EntityType.ZOMBIE_PIGMAN));
		registerIcon(EntityType.getId(EntityType.ZOMBIE_VILLAGER));
	}
}
