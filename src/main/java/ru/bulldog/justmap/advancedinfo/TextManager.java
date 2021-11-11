package ru.bulldog.justmap.advancedinfo;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.util.math.MatrixStack;

import ru.bulldog.justmap.client.config.ClientSettings;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.enums.TextPosition;

public class TextManager {
	private TextPosition textPosition = TextPosition.RIGHT;
	private final List<InfoText> elements;
	private int x, y;
	private int lineWidth;
	private int spacing = 12;

	public TextManager() {
		this.elements = new ArrayList<>();
	}

	public void draw(MatrixStack matrixStack) {
		int yp = this.y;
		for (InfoText line : elements) {
			if (!line.visible) continue;
			if (!line.fixed) {
				line.y = yp + line.offsetY;
				if (textPosition == TextPosition.ABOVE ||
					textPosition == TextPosition.ABOVE_LEFT ||
					textPosition == TextPosition.ABOVE_RIGHT) {

					yp -= spacing;
				} else {
					yp += spacing;
				}
			}
			line.draw(matrixStack);
		}
	}

	public void updatePosition(ScreenPosition position) {
		int offset = ClientSettings.positionOffset;
		MinecraftClient minecraft = MinecraftClient.getInstance();
		int screenW = minecraft.getWindow().getScaledWidth();
		int screenH = minecraft.getWindow().getScaledHeight();
		switch(position) {
			case USER_DEFINED:
			case TOP_LEFT:
				this.updatePosition(TextPosition.RIGHT, offset, offset);
				break;
			case TOP_CENTER:
				this.updatePosition(TextPosition.UNDER,
						screenW / 2 - lineWidth / 2, offset);
				break;
			case TOP_RIGHT:
				this.updatePosition(TextPosition.LEFT,
						screenW - offset, offset);
				break;
			case MIDDLE_LEFT:
				this.updatePosition(TextPosition.RIGHT,
						offset, screenH / 2 - (this.size() / 2) * spacing);
				break;
			case MIDDLE_RIGHT:
				this.updatePosition(TextPosition.LEFT,
						screenW - offset, screenH / 2 - (this.size() / 2) * spacing);
				break;
			case BOTTOM_LEFT:
				this.updatePosition(TextPosition.ABOVE_RIGHT,
						offset, screenH - offset - spacing);
				break;
			case BOTTOM_RIGHT:
				this.updatePosition(TextPosition.ABOVE_LEFT,
						screenW - offset, screenH - offset - spacing);
				break;
		}
	}

	public void updatePosition(TextPosition pos, int x, int y) {
		if (textPosition != pos || this.x != x || this.y != y) {
			this.textPosition = pos;
			this.x = x;
			this.y = y;
			this.updateLines();
		}
	}

	private void updateLines() {
		int xp = x;
		if (textPosition == TextPosition.LEFT ||
			textPosition == TextPosition.ABOVE_LEFT) {

			xp = x - lineWidth;
		}
		for (InfoText line : elements) {
			if (line.fixed) continue;
			line.offset = ClientSettings.positionOffset;
			if (textPosition == TextPosition.ABOVE || textPosition == TextPosition.UNDER) {
				line.alignment = TextAlignment.CENTER;
			} else if (textPosition == TextPosition.LEFT ||
					   textPosition == TextPosition.ABOVE_LEFT) {
				line.alignment = TextAlignment.RIGHT;
			}
			switch (line.alignment) {
		  		case CENTER: line.x = (xp + lineWidth / 2); break;
		  		case RIGHT: line.x = xp + lineWidth; break;
		  		default: line.x = xp;
			}
			if (textPosition == TextPosition.LEFT ||
				textPosition == TextPosition.ABOVE_LEFT) {

				line.x -= line.offsetX;
			} else {
				line.x += line.offsetX;
			}
		}
	}

	public void add(InfoText element) {
		this.elements.add(element);
	}

	public void setLineWidth(int width) {
		this.lineWidth = width;
	}

	public void setSpacing(int spacing) {
		this.spacing = spacing;
	}

	public void update() {
		this.elements.forEach(element -> element.update());
	}

	public int size() {
		return this.elements.size();
	}

	public void clear() {
		this.elements.clear();
		this.textPosition = TextPosition.RIGHT;
		this.x = 0;
		this.y = 0;
	}
}
