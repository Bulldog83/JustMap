package ru.bulldog.justmap.client.widget;

import java.util.function.Consumer;

import net.minecraft.client.gui.Drawable;
import net.minecraft.client.gui.Element;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.Text;
import ru.bulldog.justmap.util.Colors;
import ru.bulldog.justmap.util.RenderUtil;

public class ListElementWidget implements Drawable, Element {
	
	private Text text;
	private final Consumer<ListElementWidget> onPress;
	int width, height;
	int x, y;
	
	public ListElementWidget(Text text, Consumer<ListElementWidget> action) {
		this.onPress = action;
		this.text = text;
	}

	@Override
	public void render(MatrixStack matrices, int mouseX, int mouseY, float delta) {
		if (isMouseOver(mouseX, mouseY)) {
			RenderUtil.fill(matrices, x, y, width, height, 0x33FFFFFF);
		} else {
			RenderUtil.fill(matrices, x, y, width, height, 0x33666666);
		}
		RenderUtil.drawCenteredText(matrices, text, x + width / 2, y + height / 2, Colors.WHITE);
	}
	
	@Override
	public boolean isMouseOver(double mouseX, double mouseY) {
		return (mouseX > x && mouseY > y && mouseX < x + width && mouseY < y + height);
	}
	
	@Override
	public boolean mouseReleased(double mouseX, double mouseY, int button) {
		this.onPress.accept(this);
		return true;
	}

}
