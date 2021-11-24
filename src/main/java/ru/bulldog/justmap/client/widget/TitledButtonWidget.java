package ru.bulldog.justmap.client.widget;

import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.Element;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.sound.SoundManager;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.text.LiteralText;


public class TitledButtonWidget<W extends ClickableWidget> extends ClickableWidget implements Element {
	public final W widget;
	public final LiteralText title;
	private final TextRenderer font;

	private final static int SPACING = 3;

	public TitledButtonWidget(TextRenderer font, W widget, int x, int y, int width, int height, String message, String title) {
		super(x, y, width, height, new LiteralText(message));
		this.widget = widget;
		this.title = new LiteralText(title);
		this.font = font;

		update();
	}

	private void update() {
		int titleWidth = font.getWidth(title);
		int widgetWidth = widget.getWidth();
		int wx = x + width - widgetWidth;
		if (x + titleWidth + SPACING > wx) {
			wx = x + titleWidth + SPACING;
			widget.setWidth((x + width) - wx);
		}

		this.widget.x = wx;
		this.widget.y = y;
	}

	@Override
	public void render(MatrixStack matrixStack, int int_1, int int_2, float float_1) {
		drawStringWithShadow(matrixStack, font, title.getString(), x, y, 0xFFFFFFFF);
		widget.render(matrixStack, int_1, int_2, float_1);
	}

	@Override
	public void mouseMoved(double double_1, double double_2) {
		this.widget.mouseMoved(double_1, double_2);
	}

	@Override
	public boolean mouseScrolled(double double_1, double double_2, double double_3) {
		return this.widget.mouseScrolled(double_1, double_2, double_3);
	}

	@Override
	public boolean keyPressed(int int_1, int int_2, int int_3) {
		return this.widget.keyPressed(int_1, int_2, int_3);
	}

	@Override
	public boolean keyReleased(int int_1, int int_2, int int_3) {
		return this.widget.keyReleased(int_1, int_2, int_3);
	}

	@Override
	public boolean charTyped(char char_1, int int_1) {
		return this.widget.charTyped(char_1, int_1);
	}

	@Override
	public boolean changeFocus(boolean boolean_1) {
		return this.widget.changeFocus(boolean_1);
	}

	@Override
	public void renderButton(MatrixStack matrixStack, int int_1, int int_2, float float_1) {
		this.widget.renderButton(matrixStack, int_1, int_2, float_1);
	}

	@Override
	public void onClick(double double_1, double double_2) {
		this.widget.onClick(double_1, double_2);
	}

	@Override
	public void onRelease(double double_1, double double_2) {
		this.widget.onRelease(double_1, double_2);
	}

	@Override
	public boolean mouseClicked(double double_1, double double_2, int int_1) {
		return this.widget.mouseClicked(double_1, double_2, int_1);
	}

	@Override
	public boolean mouseReleased(double double_1, double double_2, int int_1) {
		return this.widget.mouseReleased(double_1, double_2, int_1);
	}

	@Override
	public boolean mouseDragged(double double_1, double double_2, int int_1, double double_3, double double_4) {
		return this.widget.mouseDragged(double_1, double_2, int_1, double_3, double_4);
	}

	@Override
	public boolean isHovered() {
		return this.widget.isHovered();
	}

	@Override
	public boolean isMouseOver(double double_1, double double_2) {
		return this.widget.isMouseOver(double_1, double_2);
	}

	@Override
	public void renderTooltip(MatrixStack matrixStack, int int_1, int int_2) {
		this.widget.renderTooltip(matrixStack, int_1, int_2);
	}

	@Override
	public void playDownSound(SoundManager soundManager_1) {
		this.widget.playDownSound(soundManager_1);
	}

	@Override
	public void appendNarrations(NarrationMessageBuilder builder) {
		// FIXME: implement?
	}
}
