package ru.bulldog.justmap.map.minimap;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.util.math.MatrixStack;

public class TextManager {
	public enum TextPosition {
		ABOVE,
		UNDER
	}	

	private int x, y;
	private TextPosition position = TextPosition.UNDER;
	
	private Minimap map;
	
	private List<MapText> mapInfo;
	private List<MapText> mapElements;
	
	private int spacing = 16;
  
	public TextManager(Minimap map) {
		this.map = map;
		this.mapInfo = new ArrayList<>();
		this.mapElements = new ArrayList<>();
	}
  
	public void clear() {
		this.mapInfo.clear();
		this.mapElements.clear();
	}
  
	public void draw(MatrixStack matrixStack) {
		spacing = 10;
		int yp = y;
		
		if (position == TextPosition.ABOVE) {
			yp = y - spacing * mapInfo.size();
		}
		
		for (MapText line : mapInfo) {
			switch (line.alignment) {
		  		case LEFT: line.x = x; break;
		  		case CENTER: line.x = x + map.getWidth() / 2; break;
		  		case RIGHT: line.x = x + map.getWidth(); break;
			}
		  
			line.y = yp;
			line.draw(matrixStack);
		  
			yp += spacing;
		}
		
		for (MapText elem : mapElements) {
			elem.draw(matrixStack);
		}
	}
  
	public void add(MapText info) {
		this.mapInfo.add(info);
	}
	
	public void add(MapText element, int x, int y) {
		element.x = x;
		element.y = y;
		this.mapElements.add(element);
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
}