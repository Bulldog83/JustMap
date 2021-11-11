package ru.bulldog.justmap.map.data;

public class Layer {
	public final static Layer SURFACE = new Layer("surface", 256);
	public final static Layer CAVES = new Layer("caves", 8);
	public final static Layer NETHER = new Layer("nether", 16);

	public final String name;
	public final int height;

	private Layer(String name, int height) {
		this.name = name;
		this.height = height;
	}

	@Override
	public int hashCode() {
		return 31 * name.hashCode() + height;
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
