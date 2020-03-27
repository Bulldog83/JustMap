package ru.bulldog.justmap.map.data;

import java.util.Arrays;

public class Layers {	
	public enum Type {
		SURFACE("surface", 256),
		CAVES("caves", 8),
		NETHER("nether", 16);
		
		public final Layers value;
		
		Type(String name, int height) {
			this.value = new Layers(name, height);
		}
	}
	
	public final String name;
	public final int height;
	
	private Layers(String name, int height) {
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
		if (!(obj instanceof Layers)) return false;
		
		Layers layer = (Layers) obj;
		return this.name == layer.name &&
			   this.height == layer.height;
	}
	
	@Override
	public String toString() {
		return this.name.toUpperCase();
	}
}
