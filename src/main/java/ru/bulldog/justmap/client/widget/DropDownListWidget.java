package ru.bulldog.justmap.client.widget;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.AbstractParentElement;
import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.Selectable;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.util.math.MatrixStack;

import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public class DropDownListWidget extends AbstractParentElement implements Drawable, Selectable {

	private final List<ListElementWidget> children = new ArrayList<>();
	private boolean visible = false;
	private final int x;
	private final int y;
	private int width, height;
	private final int elemHeight;
	private final int padding = 3;
	private final int spacing = 1;
	
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
		RenderSystem.defaultBlendFunc();
		this.renderBackground(matrices);
		int x = this.x + padding;
		int y = this.y + padding;
		for (ListElementWidget element : children) {
			element.x = x;
			element.y = y;			
			element.render(matrices, mouseX, mouseY, delta);			
			y += elemHeight + spacing;
		}
	}
	
	private void renderBackground(MatrixStack matrices) {
		RenderUtil.fill(matrices, x, y, x + width, y + height, 0xAA222222);
		RenderUtil.drawLine(x, y, x + width, y, Colors.LIGHT_GRAY);
		RenderUtil.drawLine(x, y, x, y + height, Colors.LIGHT_GRAY);
		RenderUtil.drawLine(x + width, y, x + width, y + height, Colors.LIGHT_GRAY);
		RenderUtil.drawLine(x, y + height, x + width, y + height, Colors.LIGHT_GRAY);
	}
	
	public void addElement(ListElementWidget element) {
		element.height = elemHeight;
		this.width = Math.max(width, element.width + padding * 2);
		this.children.add(element);
		this.children.forEach(elem -> elem.width = width - padding * 2);
		this.height = children.size() * (elemHeight + spacing) + padding * 2;
	}
	
	public void toggleVisible() {
		this.visible = !visible;
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		for (Element elem : children) {
			if (elem.isMouseOver(mouseX, mouseY)) return true;
		}
		return false;
	}

	@Override
	public List<? extends Element> children() {
		return this.children;
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		// FIXME: implement?
	}

	@Override
	public SelectionType getType() {
		// FIXME: correct?
		return SelectionType.NONE;
	}
}
