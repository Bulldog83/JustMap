package ru.bulldog.justmap.map.data.classic.fast;

import net.minecraft.client.util.math.MatrixStack;
import ru.bulldog.justmap.map.data.IRegionData;
import ru.bulldog.justmap.map.data.Layer;

public class RegionData implements IRegionData {
    @Override
    public int getX() {
        return 0;
    }

    @Override
    public int getZ() {
        return 0;
    }

    @Override
    public void swapLayer(Layer layer, int level) {

    }

    @Override
    public void draw(MatrixStack matrices, double x, double y, double width, double height, int imgX, int imgY, int imgW, int imgH) {

    }
}
