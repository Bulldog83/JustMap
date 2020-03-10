package ru.bulldog.justmap.minimap.icon;

import java.util.HashMap;
import java.util.Map;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.ImageUtil;
import ru.bulldog.justmap.util.SpriteAtlas;
import ru.bulldog.justmap.util.DrawHelper;
import net.minecraft.client.resource.metadata.AnimationResourceMetadata;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public class EntityHeadIcon extends AbstractIcon {
	
	private final static Map<Identifier, EntityHeadIcon> ICONS = new HashMap<>();
	
	private EntityHeadIcon(Identifier texture, int w, int h) {
		super(SpriteAtlas.ENTITY_HEAD_ICONS, new Sprite.Info(texture, w, h, AnimationResourceMetadata.EMPTY), 0, w, h, 0, 0, ImageUtil.loadImage(texture, w, h));
	}
	
	public static EntityHeadIcon getIcon(Entity entity) {
		if (ICONS.isEmpty()) {
			initDefaultIcons();
		}
		
		Identifier id = EntityType.getId(entity.getType());
		return ICONS.get(id);
	}
	
	@Override
	public void draw(double x, double y, int w, int h) {
		if (ClientParams.showIconsOutline) {
			DrawHelper.fill(x - 1, y - 1, x + w + 1, y + h + 1, 0xFF444444);
		}
		textureManager.bindTexture(this.getId());
		
		this.draw(x, y, (float) w, (float) h);
	}
	
	private static void registerIcon(Identifier id) {
		String path = String.format("textures/entities/%s.png", id.getPath());
		registerIcon(id, new Identifier(JustMap.MODID, path), 32, 32);
	}
	
	private static void registerIcon(Identifier entityId, Identifier image, int imageWidth, int imageHeight) {
		ICONS.put(entityId, new EntityHeadIcon(image, imageWidth, imageHeight));
	}
	
	private static void initDefaultIcons() {
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
		registerIcon(EntityType.getId(EntityType.HOGLIN));
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
		registerIcon(EntityType.getId(EntityType.PIGLIN));
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
		registerIcon(EntityType.getId(EntityType.ZOMBIFIED_PIGLIN));
		registerIcon(EntityType.getId(EntityType.ZOMBIE_VILLAGER));		
	}
}
