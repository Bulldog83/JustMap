package ru.bulldog.justmap.map.data;

import net.minecraft.client.util.math.MatrixStack;

public interface IRegionData {
    int getX();

    int getZ();

    void swapLayer(Layer layer, int level);

    void draw(MatrixStack matrices, double x, double y, double width, double height, int imgX, int imgY, int imgW, int imgH);
}
