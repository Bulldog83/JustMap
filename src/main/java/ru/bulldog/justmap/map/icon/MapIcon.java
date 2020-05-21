package ru.bulldog.justmap.map.icon;

import ru.bulldog.justmap.map.IMap;
import ru.bulldog.justmap.util.math.MathUtil;

import net.minecraft.client.MinecraftClient;

public abstract class MapIcon<T extends MapIcon<T>> {
	
	protected IMap map;
	public double x, y;
	
	protected static final MinecraftClient client = MinecraftClient.getInstance();
	
	public MapIcon(IMap map) {
		this.map = map;
	}
	
	@SuppressWarnings("unchecked")
	public T setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		
		return (T) this;
	}
	
	protected void rotatePos(IconPos pos, int mapW, int mapH, int mapX, int mapY, float rotation) {
		double centerX = mapX + mapW / 2.0;
		double centerY = mapY + mapH / 2.0;
		
		rotation = MathUtil.correctAngle(rotation) + 180;
		
		double angle = Math.toRadians(-rotation);		
		double posX = centerX + (pos.x - centerX) * Math.cos(angle) - (pos.y - centerY) * Math.sin(angle);
		double posY = centerY + (pos.y - centerY) * Math.cos(angle) + (pos.x - centerX) * Math.sin(angle);
		
		pos.x = posX;
		pos.y = posY;
	}
	
	public abstract void draw(int mapX, int mapY, float rotation);
	
	protected class IconPos {
		protected double x;
		protected double y;
		
		protected IconPos(double x, double y) {
			this.x = x;
			this.y = y;
		}
	}
}
