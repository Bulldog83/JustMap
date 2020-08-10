package ru.bulldog.justmap.client.widget;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.util.math.MatrixStack;

import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.RenderUtil;

public class MapWidget implements Element, Drawable {

	final Screen parent;
	final Minimap map;
	int top, left, right, bottom;
	int width, height;
	double x, y;
	
	public MapWidget(Screen parent, Minimap map) {
		this.parent = parent;
		this.map = map;
		this.x = map.getSkinX();
		this.y = map.getSkinY();
		int border = map.getBorder();
		this.width = map.getWidth() + border * 2;
		this.height = map.getHeight() + border * 2;
		int offset = map.getOffset();
		this.top = right = offset;
		this.left = parent.width - width - offset;
		this.bottom = parent.height - height - offset;
	}
	
	public int getX() {
		return (int) this.x;
	}
	
	public int getY() {
		return (int) this.y;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		RenderUtil.fill(matrices, x, y, width, height, 0x9900AA00);
		if (map.getSkin() != null) {
			map.getSkin().draw(matrices, x, y, width, height);
		}
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return (mouseX > x && mouseY > y && mouseX < x + width && mouseY < y + height);
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return this.isMouseOver(mouseX, mouseY);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		this.x += deltaX;
		this.y += deltaY;
		
		if (x < right) x = right;
		if (y < top) y = top;
		if (x > left) x = left;
		if (y > bottom) y = bottom;
		
		return true;
	}
}
