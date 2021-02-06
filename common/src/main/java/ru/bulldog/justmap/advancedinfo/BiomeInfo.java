package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Util;
import net.minecraft.world.Level;
import net.minecraft.world.biome.Biome;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.util.DataUtil;

public class BiomeInfo extends InfoText {

	private String title;
	private ResourceLocation currentBiome;
	
	public BiomeInfo() {
		super("Void");
		this.title = "Biome: ";
	}
	
	public BiomeInfo(TextAlignment alignment, String title) {
		super(alignment, "Void");
		this.title = title;
	}

	@Override
	public void update() {
		this.setVisible(ClientSettings.showBiome);
		Minecraft minecraft = Minecraft.getInstance();
		if (visible && minecraft.world != null) {
			Level world = minecraft.world;
			Biome biome = world.getBiome(DataUtil.currentPos());
			ResourceLocation biomeId = DataUtil.getBiomeId(world, biome);
			if (biomeId != null && !biomeId.equals(currentBiome)) {
				this.currentBiome = biomeId;
				this.setText(title + this.getTranslation());
			} else if (biomeId == null) {
				this.setText(title + "Unknown");
			}
		}
	}

	private String getTranslation() {
		return I18n.translate(Util.createTranslationKey("biome", currentBiome));
	}
}
