package ru.bulldog.justmap.minimap.icon;

import ru.bulldog.justmap.minimap.Minimap;
import net.minecraft.client.MinecraftClient;

public abstract class MapIcon<T extends MapIcon<T>> {
	
	protected Minimap map;
	public int x, y;
	
	protected static final MinecraftClient client = MinecraftClient.getInstance();
	
	public MapIcon(Minimap map) {
		this.map = map;
	}
	
	@SuppressWarnings("unchecked")
	public T setPosition(int x, int y) {
		this.x = x;
		this.y = y;
		
		return (T) this;
	}
	
	public static int scaledPos(int val, int startVal, int endVal, int range) {
		return (int) (((val - startVal) / ((float) (endVal - startVal))) * range);
	}
	
	public abstract void draw(int mapX, int mapY, float rotation);
}
