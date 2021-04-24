package ru.bulldog.justmap.fabric.util;

import net.fabricmc.fabric.api.client.render.fluid.v1.FluidRenderHandlerRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import ru.bulldog.justmap.color.ColorUtil;

public class FabricColorUtil extends ColorUtil {
	private static final FluidRenderHandlerRegistry fluidRenderHandlerRegistry = FluidRenderHandlerRegistry.INSTANCE;

	@Override
	protected int fluidColor(Level world, BlockState state, BlockPos pos, int defColor) {
		int color = colorPalette.getFluidColor(state);
		if (color == 0x0) {
			FluidState fluidState = state.getBlock().getFluidState(state);
			color = fluidRenderHandlerRegistry.get(fluidState.getType()).getFluidColor(world, pos, fluidState);
		}
		return color == -1 ? defColor : color;
	}
}
