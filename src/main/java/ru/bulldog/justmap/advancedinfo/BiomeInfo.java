package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.resource.language.I18n;
import net.minecraft.world.biome.Biome;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;
import ru.bulldog.justmap.util.PosUtil;

public class BiomeInfo extends InfoText {

	private String title;
	
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
		this.setVisible(ClientParams.showBiome);
		if (visible && minecraft.world != null) {
			Biome currentBiome = minecraft.world.getBiome(PosUtil.currentPos());
			this.setText(title + I18n.translate(currentBiome.getTranslationKey()));
		}
	}
}
