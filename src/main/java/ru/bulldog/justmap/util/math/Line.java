package ru.bulldog.justmap.util.math;

public class Line {	
	public final Point first;
	public final Point second;
	
	private int lenght;
	
	public Line(int sx, int sy, int ex, int ey) {
		this(new Point(sx, sy),
			 new Point(ex, ey));
	}
	
	public Line(Point first, Point second) {
		this.first = first;
		this.second = second;
		this.lenght = first.distance(second);
	}
	
	public int lenght() {
		return lenght;
	}
	
	public void add(int length) {
		int len = this.lenght + length;
		
		if (len <= 0) return;
		
		int cx = second.x + (second.x - first.x) / this.lenght * length;
		int cy = second.y + (second.y - first.y) / this.lenght * length;
		
		this.second.x = cx;
		this.second.y = cy;			
		this.lenght = len;
	}
	
	public void add(Line line) {
		this.add(line.lenght());
	}
	
	@Override
	public boolean equals(Object obj) {
		if (this == obj) return true;
		if(!(obj instanceof Line)) return false;
		
		Line line = (Line) obj;
		return this.first.equals(line.first) &&
			   this.second.equals(line.second);
	}
	
	public static class Point {
		public int x;
		public int y;
		
		public Point(int x, int y) {
			this.x = x;
			this.y = y;
		}
		
		public int distance(Point target) {
			return (int) Math.sqrt(MathUtil.pow2(target.x - x) + MathUtil.pow2(target.y - y));
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
