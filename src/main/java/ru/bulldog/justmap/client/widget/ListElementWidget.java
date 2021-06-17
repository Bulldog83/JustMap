package ru.bulldog.justmap.client.widget;

import com.mojang.blaze3d.vertex.PoseStack;
import java.util.function.Supplier;
import net.minecraft.client.gui.components.Widget;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.network.chat.Component;
import ru.bulldog.justmap.util.colors.Colors;
import ru.bulldog.justmap.util.render.RenderUtil;

public class ListElementWidget implements Widget, GuiEventListener {
	
	private final Supplier<Boolean> onPress;
	private Component text;
	int padding = 2;
	int width, height;
	int x, y;
	
	public ListElementWidget(Component text, Supplier<Boolean> action) {
		this.width = RenderUtil.getWidth(text) + padding * 2;
		this.onPress = action;
		this.text = text;
	}

	@Override
	public void render(PoseStack matrices, int mouseX, int mouseY, float delta) {
		if (isMouseOver(mouseX, mouseY)) {
			RenderUtil.fill(matrices, x, y, x + width, y + height, 0x33FFFFFF);
		}
		RenderUtil.drawCenteredText(matrices, text, x + width / 2, y + height / 2 - 5, Colors.WHITE);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return (mouseX > x && mouseY > y && mouseX < x + width && mouseY < y + height);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		return this.onPress.get();
	}

}
