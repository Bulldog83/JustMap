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
		if (second.x > first.x && second.y > first.y) {
			return first.x <= point.x && first.y <= point.y &&
				   second.x >= point.x && second.y >= point.y;
		}
		if (second.x > first.x && first.y > second.y) {
			return first.x <= point.x && first.y >= point.y &&
				   second.x >= point.x && second.y <= point.y;
		}
		if (first.x > second.x && second.y > first.y) {
			return first.x >= point.x && first.y <= point.y &&
				   second.x <= point.x && second.y >= point.y;
		}
		if (first.x > second.x && first.y > second.y) {
			return first.x >= point.x && first.y >= point.y &&
				   second.x <= point.x && second.y <= point.y;
		}
		return false;
	}
}
