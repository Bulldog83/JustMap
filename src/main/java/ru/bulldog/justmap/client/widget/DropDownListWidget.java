package ru.bulldog.justmap.client.widget;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.ParentElement;
import net.minecraft.client.util.math.MatrixStack;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;

public class DropDownListWidget implements Drawable, ParentElement {

	private List<ListElementWidget> children = new ArrayList<>();
	private ListElementWidget focused;
	private boolean visible = false;
	private int x, y;
	private int width, height;
	private int elemHeight;
	private int padding = 3;
	private int spacing = 1;
	
	public DropDownListWidget(int x, int y, int width, int height) {
		this.elemHeight = height;
		this.width = width;
		this.height = height + padding * 2;
		this.x = x;
		this.y = y;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (!visible) return;
		RenderSystem.enableBlend();
		this.renderBackground();
		int x = this.x + padding;
		int y = this.y + padding;
		for (ListElementWidget element : children) {
			element.x = x;
			element.y = y;			
			element.render(matrices, mouseX, mouseY, delta);			
			y += elemHeight + spacing;
		}
	}
	
	private void renderBackground() {
		RenderUtil.fill(x, y, width, height, 0x66000000);
		RenderUtil.drawLine(x, y, x + width, y, Colors.LIGHT_GRAY);
		RenderUtil.drawLine(x, y, x, y + height, Colors.LIGHT_GRAY);
		RenderUtil.drawLine(x + width, y, x + width, y + height, Colors.LIGHT_GRAY);
		RenderUtil.drawLine(x, y + height, x + width, y + height, Colors.LIGHT_GRAY);
	}
	
	public void addElement(ListElementWidget element) {
		element.width = elemWidth;
		element.height = elemHeight;
		this.children.add(element);
		this.height = children.size() * elemHeight + padding * 2;
	}
	
	public void toggleVisible() {
		this.visible = !visible;
	}

	@Override
	public List<? extends Element> children() {
		return this.children;
	}

	@Override
	public boolean isDragging() {
		return false;
	}

	@Override
	public void setDragging(boolean dragging) {}

	@Override
	public Element getFocused() {
		return this.focused;
	}

	@Override
	public void setFocused(Element focused) {
		if (!(focused instanceof ListElementWidget)) return;
		if (children.contains(focused)) {
			this.focused = (ListElementWidget) focused;
		}
	}

}
