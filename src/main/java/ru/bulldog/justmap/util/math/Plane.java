package ru.bulldog.justmap.util.math;

public class Plane {

	public final Point first, second;
	
	public Plane(double x1, double y1, double x2, double y2) {
		this(new Point(x1, y1),
			 new Point(x2, y2));
	}
	
	public Plane(Point first, Point second) {
		if (first.x == second.x || first.y == second.y) {
			throw new IllegalArgumentException("Invalid Plane corners!");
		}
		this.first = first;
		this.second = second;
	}
	
	public boolean contains(Point point) {
		return (first.compareTo(point) >= 0 &&
			    second.compareTo(point) <= 0) ||
			   (first.compareTo(point) <= 0 &&
				second.compareTo(point) >= 0);
	}
}
