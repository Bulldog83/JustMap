package ru.bulldog.justmap.advancedinfo;

import net.minecraft.world.biome.Biome;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.util.DataUtil;

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
		this.setVisible(ClientSettings.showBiome);
		if (visible && minecraft.world != null) {
			Biome currentBiome = minecraft.world.getBiome(DataUtil.currentPos());
			this.setText(title + currentBiome.getName().getString());
		}
	}
}
