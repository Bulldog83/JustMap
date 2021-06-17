package ru.bulldog.justmap.client.widget;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.AbstractContainerEventHandler;
import net.minecraft.client.gui.components.events.ContainerEventHandler;
import net.minecraft.client.gui.components.events.GuiEventListener;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public class DropDownListWidget extends AbstractWidget implements ContainerEventHandler {

	private final List<ListElementWidget> children = new ArrayList<>();
	private final int padding = 3;
	private final int spacing = 1;
	private final int elemHeight;
	@Nullable
	private GuiEventListener focused;
	private boolean visible = false;
	private boolean isDragging;

	public DropDownListWidget(int x, int y, int width, int height) {
		super(x, y, width, height, null);
		this.elemHeight = height;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		if (!visible) return;
		RenderSystem.enableBlend();
		RenderSystem.defaultBlendFunc();
		renderBackground(matrices);
		int x = this.x + padding;
		int y = this.y + padding;
		for (ListElementWidget element : children) {
			element.x = x;
			element.y = y;			
			element.render(matrices, mouseX, mouseY, delta);			
			y += elemHeight + spacing;
		}
	}
	
	private void renderBackground(PoseStack matrices) {
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
		for (GuiEventListener elem : children) {
			if (elem.isMouseOver(mouseX, mouseY)) {
				isHovered = true;
				return true;
			}
		}
		isHovered = false;
		return false;
	}

	@Override
	public List<? extends GuiEventListener> children() {
		return children;
	}

	@Override
	public boolean isDragging() {
		return isDragging;
	}

	@Override
	public void setDragging(boolean value) {
		this.isDragging = value;
	}

	@Nullable
	@Override
	public GuiEventListener getFocused() {
		return focused;
	}

	@Override
	public void setFocused(@Nullable GuiEventListener guiEventListener) {
		this.focused = guiEventListener;
	}

	@Override
	public void updateNarration(NarrationElementOutput narrationElementOutput) {
		defaultButtonNarrationText(narrationElementOutput);
	}
}
