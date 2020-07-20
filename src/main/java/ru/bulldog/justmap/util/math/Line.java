package ru.bulldog.justmap.util.math;

public class Line {	
	public final Point first;
	public final Point second;
	
	private double lenght;
	
	public Line(double sx, double sy, double ex, double ey) {
		this(new Point(sx, sy),
			 new Point(ex, ey));
	}
	
	public Line(Point first, Point second) {
		this.first = first;
		this.second = second;
		this.lenght = first.distance(second);
		
		if (lenght <= 0) {
			String error = String.format("Invalid Line: %s: %s! Lenght: %f.",
					first.toString(), second.toString(), lenght);
			throw new IllegalArgumentException(error);
		}
	}
	
	public double lenght() {
		return this.lenght;
	}
	
	public void add(double length) {
		double len = this.lenght + length;
		
		if (len <= 0.0) return;
		
		double cx = second.x + (second.x - first.x) / this.lenght * length;
		double cy = second.y + (second.y - first.y) / this.lenght * length;
		
		this.second.x = cx;
		this.second.y = cy;			
		this.lenght = len;
	}
	
	public void add(Line line) {
		this.add(line.lenght);
	}
	
	public void subtract(double length) {
		this.add(-length);
	}
	
	public void subtract(Line line) {
		this.subtract(line.lenght);
	}
	
	public double difference(Line line) {
		return this.lenght - line.lenght;
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if(!(obj instanceof Line)) return false;
		
		Line line = (Line) obj;
		return this.first.equals(line.first) &&
			   this.second.equals(line.second);
	}
}
