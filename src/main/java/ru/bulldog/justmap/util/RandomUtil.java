package ru.bulldog.justmap.util;

import java.util.List;
import java.util.Random;

public class RandomUtil {
	public static final Random rand = new Random();
	
	public static int getRange(int max) {
		return getRange(0, max);
	}
	
	public static int getRange(int min, int max) {
		return rand.nextInt(max - min) + min;
	}
	
	public static <T> T getElement(T[] array) {
		return array[getRange(array.length)];
	}
	
	public static <T> T getElement(List<T> list) {
		return list.get(getRange(list.size()));
	}
}
