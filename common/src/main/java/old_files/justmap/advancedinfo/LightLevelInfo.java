package old_files.justmap.advancedinfo;

import net.minecraft.client.Minecraft;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.LightType;

import old_files.justmap.util.DataUtil;
import ru.bulldog.justmap.client.config.ClientSettings;

public class LightLevelInfo extends InfoText {

	public LightLevelInfo() {
		super("Light: 0 (Block: 0)");
	}

	@Override
	public void update() {
		this.setVisible(ClientSettings.showLight);
		Minecraft minecraft = Minecraft.getInstance();
		if (visible && minecraft.world != null) {
			BlockPos currentPos = DataUtil.currentPos();
			minecraft.world.calculateAmbientDarkness();
			int skyLight = minecraft.world.getLightLevel(currentPos);
			int blockLight = minecraft.world.getLightLevel(LightType.BLOCK, currentPos);
			this.setText(String.format("Light: %d (Block: %d)", Math.max(skyLight, blockLight), blockLight));
		}
	}
}
