package ru.bulldog.justmap.advancedinfo;

import net.minecraft.client.MinecraftClient;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.util.DataUtil;

public class LightLevelInfo extends InfoText {

	public LightLevelInfo() {
		super("Light: 0 (Block: 0)");
	}

	@Override
	public void update() {
		this.setVisible(ClientSettings.showLight);
		MinecraftClient minecraft = MinecraftClient.getInstance();
		if (visible && minecraft.world != null) {
			BlockPos currentPos = DataUtil.currentPos();
			minecraft.world.calculateAmbientDarkness();
			int skyLight = minecraft.world.getLightLevel(currentPos);
			int blockLight = minecraft.world.getLightLevel(LightType.BLOCK, currentPos);
			this.setText(String.format("Light: %d (Block: %d)", Math.max(skyLight, blockLight), blockLight));
		}
	}
}
