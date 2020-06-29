package ru.bulldog.justmap.advancedinfo;

import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.PosUtil;

public class LightLevelInfo extends InfoText {

	public LightLevelInfo() {
		super("Light: 0 (Block: 0)");
	}

	@Override
	public void update() {
		this.setVisible(ClientParams.showLight);
		if (visible && minecraft.world != null) {
			BlockPos currentPos = PosUtil.currentPos();
			minecraft.world.calculateAmbientDarkness();
			int skyLight = minecraft.world.getLightLevel(currentPos);
			int blockLight = minecraft.world.getLightLevel(LightType.BLOCK, currentPos);
			this.setText(String.format("Light: %d (Block: %d)", Math.max(skyLight, blockLight), blockLight));
		}
	}
}
