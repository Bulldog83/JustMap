package ru.bulldog.justmap.advancedinfo;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.MinecraftClient;

import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.enums.ScreenPosition;
import ru.bulldog.justmap.enums.TextAlignment;
import ru.bulldog.justmap.enums.TextPosition;
import ru.bulldog.justmap.util.DataUtil;

public class TextManager {
	private TextPosition textPosition = TextPosition.RIGHT;
	private List<InfoText> elements;
	private int x, y;
	private int lineWidth;
	private int spacing = 12;
  
	public TextManager() {
		this.elements = new ArrayList<>();
	}
  
	public void draw() {
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
			line.draw();
		}
	}
	
	public TextManager updatePosition(ScreenPosition position) {
		int offset = ClientParams.positionOffset;
		MinecraftClient minecraft = DataUtil.getMinecraft();
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
		return this;
	}
	
	public TextManager updatePosition(TextPosition pos, int x, int y) {
		if (textPosition != pos || this.x != x || this.y != y) {
			this.textPosition = pos;
			this.x = x;
			this.y = y;
			this.updateLines();
		}
		return this;
	}
	
	private void updateLines() {
		int xp = x;
		if (textPosition == TextPosition.LEFT ||
			textPosition == TextPosition.ABOVE_LEFT) {
			
			xp = x - lineWidth;
		}
		for (InfoText line : elements) {
			if (line.fixed) continue;
			line.offset = ClientParams.positionOffset;
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
	
	public TextManager setLineWidth(int width) {
		this.lineWidth = width;
		return this;
	}
	
	public TextManager setSpacing(int spacing) {
		this.spacing = spacing;
		return this;
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