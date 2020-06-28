package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.PosUtil;

public class BiomeInfo extends InfoText {

	public BiomeInfo() {
		super("Biome: Void");
	}

	@Override
	public void update() {
		this.setVisible(ClientParams.showBiome);
		if (visible && minecraft.world != null) {
			Biome currentBiome = minecraft.world.getBiome(PosUtil.currentPos());
			this.setText("Biome: " + I18n.translate(currentBiome.getTranslationKey()));
		}
	}
}
