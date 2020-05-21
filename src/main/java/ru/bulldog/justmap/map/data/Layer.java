package ru.bulldog.justmap.map.data;

import java.util.Arrays;

public class Layer {	
	public enum Type {
		SURFACE("surface", 256),
		CAVES("caves", 8),
		NETHER("nether", 16);
		
		public final Layer value;
		
		Type(String name, int height) {
			this.value = new Layer(this, name, height);
		}
	}
	
	public final Type type;
	public final String name;
	public final int height;
	
	private Layer(Type type, String name, int height) {
		this.type = type;
		this.name = name;
		this.height = height;
	}
	
	@Override
	public int hashCode() {
		return Arrays.hashCode(new int[] {name.hashCode(), height});
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if (!(obj instanceof Layer)) return false;
		
		Layer layer = (Layer) obj;
		return this.name == layer.name &&
			   this.height == layer.height;
	}
	
	@Override
	public String toString() {
		return this.name.toUpperCase();
	}
}
