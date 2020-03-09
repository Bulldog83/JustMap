package ru.bulldog.justmap.util.math;

public class Line {	
	public final Point first;
	public final Point second;
	
	public Line(int sx, int sy, int ex, int ey) {
		this.first = new Point(sx, sy);
		this.second = new Point(ex, ey);
	}
	
	public Point intersect(Line line) {
		Point p1 = this.first;
		Point p2 = this.second;
		Point p3 = line.first;
		Point p4 = line.second;
		
		if (p2.x < p1.x) {
			Point tmp = p1;
		    p1 = p2;
		    p2 = tmp;
		}
		 
		if (p4.x < p3.x) {
			Point tmp = p3;
		    p3 = p4;
		    p4 = tmp;
		}
		
		int xa, ya, a1, a2, b1, b2;
		
		if (p1.x == p2.x) {
			
			xa = p1.x;
			if (p3.x == p4.x) {
				return new Point(xa, Math.max(p2.y, p4.y));
			}
			
			a2 = (p3.y - p4.y) / (p3.x - p4.x);
			b2 = p3.y - a2 * p3.x;
			ya = a2 * xa + b2;
			
			if (p3.x <= xa && p4.x >= xa && Math.min(p1.y, p2.y) <= ya &&
				Math.max(p1.y, p2.y) >= ya) {
				
				return new Point(xa, ya);
			}
		} else if (p3.x == p4.x) {
			
			xa = p3.x;
			a1 = (p1.y - p2.y) / (p1.x - p2.x);
			b1 = p1.y - a1 * p1.x;
			ya = a1 * xa + b1;
			
			if (p1.x <= xa && p2.x >= xa && Math.min(p3.y, p4.y) <= ya &&
				Math.max(p3.y, p4.y) >= ya) {
				
				return new Point(xa, ya);
			}
		} else {
			a1 = (p1.y - p2.y) / (p1.x - p2.x);
			a2 = (p3.y - p4.y) / (p3.x - p4.x);
			b1 = p1.y - a1 * p1.x;
			b2 = p3.y - a2 * p3.x;
			
			if (a1 == a2) return null;
			
			xa = (b2 - b1) / (a1 - a2);
			ya = a2 * xa + b2;
			
			if ((xa < Math.max(p1.x, p3.x)) || (xa > Math.min( p2.x, p4.x))) {
				return new Point(xa, ya);
			}
		}
		
		return null;
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
		public int x;
		public int y;
		
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
