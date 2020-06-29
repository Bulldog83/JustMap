package ru.bulldog.justmap.advancedinfo;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.util.math.MatrixStack;
import ru.bulldog.justmap.client.config.ClientParams;
import ru.bulldog.justmap.util.DrawHelper.TextAlignment;

public class TextManager {
	public enum TextPosition {
		ABOVE,
		UNDER,
		LEFT,
		RIGHT,
		ABOVE_LEFT,
		ABOVE_RIGHT
	}	

	private TextPosition position = TextPosition.RIGHT;
	private List<InfoText> elements;
	private int x, y;
	private int lineWidth;
	private int spacing = 12;
  
	public TextManager() {
		this.elements = new ArrayList<>();
	}
  
	public void clear() {
		this.elements.clear();
	}
	
	public int getX() {
		return x;
	}
	
	public int getY() {
		return y;
	}
  
	public void draw(MatrixStack matrixStack) {
		int yp = y;
		int xp = x;
		
		if (position == TextPosition.ABOVE ||
			position == TextPosition.ABOVE_LEFT ||
			position == TextPosition.ABOVE_RIGHT) {
			
			yp -= spacing / 2;
		}
		if (position == TextPosition.LEFT ||
				   position == TextPosition.ABOVE_LEFT) {
			xp -= lineWidth;
		}
		
		for (InfoText line : elements) {
			if (!line.visible) continue;
			if (!line.fixed) {
				line.offset = ClientParams.positionOffset;
				if (position == TextPosition.ABOVE || position == TextPosition.UNDER) {
					line.alignment = TextAlignment.CENTER;
				} else if (position == TextPosition.LEFT ||
						   position == TextPosition.ABOVE_LEFT) {
					line.alignment = TextAlignment.RIGHT;
				}
				switch (line.alignment) {
			  		case CENTER: line.x = (xp + lineWidth / 2); break;
			  		case RIGHT: line.x = xp + lineWidth; break;
			  		default: line.x = xp;
				}
				if (position == TextPosition.LEFT ||
					position == TextPosition.ABOVE_LEFT) {
					
					line.x -= line.offsetX;
				} else {
					line.x += line.offsetX;
				}
			  
				line.y = yp + line.offsetY;
				if (position == TextPosition.ABOVE ||
					position == TextPosition.ABOVE_LEFT ||
					position == TextPosition.ABOVE_RIGHT) {
					
					yp -= spacing;
				} else {
					yp += spacing;
				}
			}
			line.draw(matrixStack);
		}
	}
  
	public void add(InfoText element) {
		this.elements.add(element);
	}
	
	public TextManager setLineWidth(int width) {
		this.lineWidth = width;
		return this;
	}
	
	public int getLineWidth() {
		return this.lineWidth;
	}
	
	public TextManager setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		return this;
	}
  
	public TextManager setDirection(TextPosition pos) {
		this.position = pos;
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

	public int getSpacing() {
		return this.spacing;
	}
}