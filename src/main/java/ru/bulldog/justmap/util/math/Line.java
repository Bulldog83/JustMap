package ru.bulldog.justmap.util.math;

public class Line {	
	public final Point first;
	public final Point second;
	
	public Line(int sx, int sy, int ex, int ey) {
		this.first = new Point(sx, sy);
		this.second = new Point(ex, ey);
	}
	
	public Point intersect(Line line) {
		if (this.isIntersect(line)) {
			Point p1 = this.first;
			Point p2 = this.second;
			Point p3 = line.first;
			Point p4 = line.second;
			
			int a1, a2, b1, b2;
			
			int xa, ya;
			if (p1.x - p2.x == 0) {
				xa = p1.x;
			    a2 = (p3.y - p4.y) / (p3.x - p4.x);
			    b2 = p3.y - a2 * p3.x;
			    ya = a2 * xa + b2;
			} else if (p3.x - p4.x == 0) {
				xa = p3.x;
			    a1 = (p1.y - p2.y) / (p1.x - p2.x);
			    b1 = p1.y - a1 * p1.x;
			    ya = a1 * xa + b1;
			} else {
				a1 = (p1.y - p2.y) / (p1.x - p2.x);
				a2 = (p3.y - p4.y) / (p3.x - p4.x);
				b1 = p1.y - a1 * p1.x;
				b2 = p3.y - a2 * p3.x;
				
				xa = (b2 - b1) / (a1 - a2);
				ya = a2 * xa + b2;
			}
			
			return new Point(xa, ya);
		}
		
		return null;
	}
	
	public boolean isIntersect(Line line) {
		Point p1 = this.first;
		Point p2 = this.second;
		Point p3 = line.first;
		Point p4 = line.second;
		
		if (p2.x < p3.x) {
			return false;
		}
		if((p1.x - p2.x == 0) && (p3.x - p4.x == 0)) {
			if(p1.x == p3.x) {
				return (!((Math.max(p1.y, p2.y) < Math.min(p3.y, p4.y)) ||
		                (Math.min(p1.y, p2.y) > Math.max(p3.y, p4.y))));
			}
		 
		    return false;
		}
		if (p1.x - p2.x == 0) {
			double Xa = p1.x;
		    double A2 = (p3.y - p4.y) / (p3.x - p4.x);
		    double b2 = p3.y - A2 * p3.x;
		    double Ya = A2 * Xa + b2;
		 
		    return (p3.x <= Xa && p4.x >= Xa && Math.min(p1.y, p2.y) <= Ya &&
		            Math.max(p1.y, p2.y) >= Ya);
		}
		if (p3.x - p4.x == 0) {
			double Xa = p3.x;
		    double A1 = (p1.y - p2.y) / (p1.x - p2.x);
		    double b1 = p1.y - A1 * p1.x;
		    double Ya = A1 * Xa + b1;
		 
		    return (p1.x <= Xa && p2.x >= Xa && Math.min(p3.y, p4.y) <= Ya &&
		            Math.max(p3.y, p4.y) >= Ya);
		}
		
		double A1 = (p1.y - p2.y) / (p1.x - p2.x);
		double A2 = (p3.y - p4.y) / (p3.x - p4.x);
		double b1 = p1.y - A1 * p1.x;
		double b2 = p3.y - A2 * p3.x;
		 
		if (A1 == A2) {
		    return false;
		}
		 
		double Xa = (b2 - b1) / (A1 - A2);
		 
		return ((Xa < Math.max(p1.x, p3.x)) || (Xa > Math.min( p2.x, p4.x)));
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if(!(obj instanceof Line)) return false;
		
		Line line = (Line) obj;
		return this.first.equals(line.first) &&
			   this.second.equals(line.second);
	}
	
	public class Point {
		public final int x;
		public final int y;
		
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if(!(obj instanceof Point)) return false;
			
			Point point = (Point) obj;
			return this.x == point.x && this.y == point.y;
		}
	}
}
