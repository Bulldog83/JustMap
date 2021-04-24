package ru.bulldog.justmap.color;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;

import ru.bulldog.justmap.util.math.MathUtil;

public class ColorExtractor {
	private final List<Center> centers = new ArrayList<>();
	private final List<Integer> colors;
	private Integer result;
	
	public ColorExtractor(List<Integer> colors) {
		this.colors = colors;
		Random rnd = new Random();
		int size = colors.size();
		for (int i = 0; i < 4; i++) {
			int color = colors.get(rnd.nextInt(size));
			centers.add(new Center(color));
		}
	}
	
	public int analyze() {
		boolean moved = true;
		while (moved) {
			remap();
			moved = false;
			for (Center center : centers) {
				if (center.move()) {
					moved = true;
				}
			}
		}
		List<Center> toClear = new ArrayList<>();
		centers.forEach(center -> {
			if (center.colors.isEmpty()) {
				toClear.add(center);
			}
		});
		if (toClear.size() > 0) {
			toClear.forEach(centers::remove);
		}
		centers.sort(Center.COMPARATOR);
		
		return getResult();
	}
	
	public int getResult() {
		if (result == null) {
			double weights = 0;
			double alpha = 0;
			double red = 0;
			double green = 0;
			double blue = 0;
			for (Center center : centers) {
				double weight = (double) center.colors.size() / colors.size();
				weights += weight;
				alpha += center.alpha * weight;
				red += center.red * weight;
				green += center.green * weight;
				blue += center.blue * weight;
			}
			
			int a = (int) Math.round(alpha / weights);
			int r = (int) Math.round(red / weights);
			int g = (int) Math.round(green / weights);
			int b = (int) Math.round(blue / weights);
			
			result = a << 24 | r << 16 | g << 8 | b;
		}
		
		return result;
	}
	
	private void remap() {
		centers.forEach(entry -> entry.colors.clear());
		colors.forEach(color -> {
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
			centers.get(id).colors.add(color);
		});
	}
	
	private static class Center {
		static final Comparator<Center> COMPARATOR = Comparator.comparingInt(Center::getColor);
		
		List<Integer> colors = new ArrayList<>();
		double alpha, red, green, blue;
		
		Center(int color) {
			this.alpha = (color >> 24) & 255;
			this.red = (color >> 16) & 255;
			this.green = (color >> 8) & 255;
			this.blue = color & 255;
		}
		
		private void update(double a, double r, double g, double b) {
			this.alpha = a;
			this.red = r;
			this.green = g;
			this.blue = b;
		}
		
		public int getColor() {
			int a = (int) Math.round(alpha);
			int r = (int) Math.round(red);
			int g = (int) Math.round(green);
			int b = (int) Math.round(blue);
			return a << 24 | r << 16 | g << 8 | b;
		}
		
		public boolean move() {
			double or = red;
			double og = green;
			double ob = blue;
			double a = 0, r = 0, g = 0, b = 0;
			int size = colors.size();
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
			
			this.update(a, r, g, b);
			
			return Math.abs(r - or) > 0.1 ||
				   Math.abs(g - og) > 0.1 ||
				   Math.abs(b - ob) > 0.1;
		}
	}
}
