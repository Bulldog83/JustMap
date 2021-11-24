package ru.bulldog.justmap.util;

import java.util.Objects;
import java.util.function.Predicate;

public class Predicates {
	@SafeVarargs
	public static <T> boolean or(T value, Predicate<T>... predicates) {
		for (Predicate<T> predicate : predicates) {
			if (predicate.test(value)) {
				return true;
			}
		}

		return false;
	}

	@SafeVarargs
	public static <T> boolean and(T value, Predicate<T>... predicates) {
		for (Predicate<T> predicate : predicates) {
			if (!predicate.test(value)) {
				return false;
			}
		}

		return true;
	}

	public static final Predicate<String> isInteger = (s) -> s.matches("-?\\d+");
	public static boolean isInteger(String s, int min, int max) {
		boolean isInt = isInteger.test(s);
		if (isInt) {
			int i = Integer.parseInt(s);
			return i >= min && i <= max;
		}

		return false;
	}

	public static final Predicate<String> isPositiveInteger = (s) -> s.matches("\\d+");
	public static boolean isPositiveInteger(String s, int min, int max) {
		boolean isPosInt = isPositiveInteger.test(s);

		System.out.println(s + " -- " + isPosInt);

		if (isPosInt) {
			int i = Integer.parseInt(s);
			return i >= min && i <= max;
		}

		return false;
	}

	public static final Predicate<String> isEmpty = (s) -> Objects.equals(s, "");
}
