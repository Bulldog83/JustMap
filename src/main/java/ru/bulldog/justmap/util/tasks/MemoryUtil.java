package ru.bulldog.justmap.util.tasks;

public final class MemoryUtil {

	private MemoryUtil() {}

	public static long getMemoryUsage() {
		final long max = Runtime.getRuntime().maxMemory();
		final long total = Runtime.getRuntime().totalMemory();
		final long free = Runtime.getRuntime().freeMemory();
		return (total - free) * 100L / max;
	}
}
