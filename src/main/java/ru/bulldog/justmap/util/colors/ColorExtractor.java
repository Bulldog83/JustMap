package ru.bulldog.justmap.util.colors;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ru.bulldog.justmap.util.math.MathUtil;

public class ColorExtractor {
	
	private List<Center> centers = new ArrayList<>();
	private List<Integer> colors;
	
	public ColorExtractor(List<Integer> colors) {
		this.colors = colors;
		Random rnd = new Random();
		int size = colors.size();
		for (int i = 0; i < 4; i++) {
			int color = colors.get(rnd.nextInt(size));
			this.centers.add(new Center(color));
		}
	}
	
	public void remap() {
		this.centers.forEach(entry -> entry.colors.clear());
		this.colors.forEach(color -> {
			int id = 0;
			int base = centers.get(0).getColor();
			int dst = MathUtil.colorDistance(color, base);
			for (Center center : centers) {
				base = center.getColor();
				int dst1 = MathUtil.colorDistance(color, base);
				if (dst1 < dst) {
					dst = dst1;
					id = centers.indexOf(center);
				}
			}
			this.centers.get(id).colors.add(color);
		});
	}
	
	public class Center {
		List<Integer> colors = new ArrayList<>();
		int a, r, g, b;
		
		Center(int color) {
			this.a = (color >> 24) & 255;
			this.r = (color >> 16) & 255;
			this.g = (color >> 8) & 255;
			this.b = color & 255;
		}
		
		public void update(int a, int r, int g, int b) {
			this.a = a;
			this.r = r;
			this.g = g;
			this.b = b;
		}
		
		public int getColor() {
			return a << 24 | r << 16 | g << 8 | b;
		}
		
		public boolean move() {
			int or = r;
			int og = g;
			int ob = b;
			double a = 0, r = 0, g = 0, b = 0;
			int size = this.colors.size();
			for (int col : colors) {
				a += (col >> 24) & 255;
				r += (col >> 16) & 255;
				g += (col >> 8) & 255;
				b += col & 255;
			}
			a /= size;
			r /= size;
			g /= size;
			b /= size;
			
			this.update((int) a, (int) r, (int) g, (int) b);
			
			return Math.abs(r - or) > 0.1 ||
				   Math.abs(g - og) > 0.1 ||
				   Math.abs(b - ob) > 0.1;
		}
	}
}
