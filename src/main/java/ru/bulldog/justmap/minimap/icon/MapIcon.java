package ru.bulldog.justmap.minimap.icon;

import ru.bulldog.justmap.minimap.Minimap;
import ru.bulldog.justmap.util.math.MathUtil;
import net.minecraft.client.MinecraftClient;

public abstract class MapIcon<T extends MapIcon<T>> {
	
	protected Minimap map;
	public double x, y;
	
	protected static final MinecraftClient client = MinecraftClient.getInstance();
	
	public MapIcon(Minimap map) {
		this.map = map;
	}
	
	@SuppressWarnings("unchecked")
	public T setPosition(double x, double y) {
		this.x = x;
		this.y = y;
		
		return (T) this;
	}
	
	protected void rotatePos(IconPos pos, int mapSize, int mapX, int mapY, float rotation) {
		double centerX = mapX + mapSize / 2;
		double centerY = mapY + mapSize / 2;
		
		rotation = MathUtil.correctAngle(rotation) + 180;
		
		double angle = Math.toRadians(-rotation);
		
		double posX = centerX + (pos.x - centerX) * Math.cos(angle) - (pos.y - centerY) * Math.sin(angle);
		double posY = centerY + (pos.y - centerY) * Math.cos(angle) + (pos.x - centerX) * Math.sin(angle);
		
		pos.x = posX;
		pos.y = posY;
	}
	
	public static double scaledPos(double val, double startVal, double endVal, int range) {
		return ((val - startVal) / (endVal - startVal)) * range;
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
