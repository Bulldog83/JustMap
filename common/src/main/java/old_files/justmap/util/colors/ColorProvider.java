package old_files.justmap.util.colors;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public interface ColorProvider {
	public int getColor(BlockState state, Level world, BlockPos pos);
}
