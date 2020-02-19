package ru.bulldog.justmap.util;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;

import ru.bulldog.justmap.JustMap;

public final class Logger {
	
	private static final org.apache.logging.log4j.Logger LOGGER = LogManager.getLogger();
	
	private String modPref = "[" + JustMap.MODID + "] ";
	
	private Logger() {}
	
	public static Logger getLogger() {
		return new Logger();
	}
	
	public void log(Level level, String message) {
		LOGGER.log(level, modPref + message);
	}
	
	public void debug(Object message) {
		this.log(Level.DEBUG, message.toString());
	}
	
	public void catching(Throwable ex) {
		this.logError(ex.getLocalizedMessage());
		LOGGER.catching(ex);
	}
	
	public void logInfo(String message) {
		this.log(Level.INFO, message);
	}
	
	public void logWarning(String message) {
		this.log(Level.WARN, message);
	}
	
	public void logError(String message) {
		this.log(Level.ERROR, message);
	}
}
