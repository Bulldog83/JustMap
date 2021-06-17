package ru.bulldog.justmap.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import ru.bulldog.justmap.map.minimap.Minimap;
import ru.bulldog.justmap.util.render.RenderUtil;

public class MapWidget implements GuiEventListener, Widget, NarratableEntry {

	final Minimap map;
	int left, right;
	int top, bottom;
	int width, height;
	int bgW, bgH;
	int border;
	double initX, initY;
	double x, y;
	boolean isHovered;
	
	public MapWidget(Screen parent, Minimap map) {
		this.map = map;
		this.initX = x = map.getSkinX();
		this.initY = y = map.getSkinY();
		this.border = map.getBorder();
		this.width = map.getWidth() + border * 2;
		this.height = map.getHeight() + border * 2;
		this.bgW = width - border * 2;
		this.bgH = height - border * 2;

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
	
	public void resetPosition() {
		this.x = initX;
		this.y = initY;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		int color = 0xDD00AA00;
		if (Minimap.isRound()) {
			double centerX = x + border + bgW / 2;
			double centerY = y + border + bgH / 2;
			RenderUtil.drawCircle(centerX, centerY, bgW / 2, color);
		} else {
			RenderUtil.fill(matrices, x + border, y + border, bgW, bgH, color);
		}
		if (map.getSkin() != null) {
			map.getSkin().draw(matrices, x, y, width, height);
		}
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		this.isHovered = mouseX > x && mouseY > y && mouseX < x + width && mouseY < y + height;
		return isHovered;
	}
	
	@Override
	public boolean mouseClicked(double mouseX, double mouseY, int button) {
		return this.isMouseOver(mouseX, mouseY);
	}
	
	@Override
	public boolean mouseDragged(double mouseX, double mouseY, int button, double deltaX, double deltaY) {
		this.x += deltaX;
		this.y += deltaY;
		
		if (x > left) x = left;
		if (x < right) x = right;
		if (y < top) y = top;
		if (y > bottom) y = bottom;
		
		return true;
	}

	@Override
	public NarrationPriority narrationPriority() {
		return isHovered ? NarrationPriority.HOVERED : NarrationPriority.NONE;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {}
}
