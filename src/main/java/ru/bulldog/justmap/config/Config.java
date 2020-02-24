package ru.bulldog.justmap.config;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.config.ConfigKeeper.*;

public abstract class Config {
	
	protected final static ConfigKeeper KEEPER = ConfigKeeper.getInstance();
	
	public abstract void saveChanges();
	
	public Entry<?> getEntry(String key) {
		return KEEPER.getEntry(key);
	}
	
	public Object getDefault(String key) {
		Entry<?> entry = KEEPER.getEntry(key);
		return entry != null ? entry.getDefault() : null;
	}
	
	public String getString(String key) {
		String str = (String) KEEPER.getValue(key);		
		return str != null ? str : "";
	}
	
	public boolean setString(String key, String value) {
		try {
			StringEntry entry = (StringEntry) KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
			
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public int getInt(String key) {
		Integer val = (Integer) KEEPER.getValue(key);		
		return val != null ? val : 0;
	}
	
	public boolean setInt(String key, int value) {
		try {
			IntegerEntry entry = (IntegerEntry) KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
			
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public <T extends Comparable<T>> boolean setRanged(String key, T value) {
		try {
			Entry<?> entry = KEEPER.getEntry(key);
			if (entry instanceof RangeEntry) {
				@SuppressWarnings("unchecked")
				RangeEntry<T> range = (RangeEntry<T>) entry;
				
				range.setValue(value);
				KEEPER.set(key, range);
				
				return true;
			}
			
			return false;
		} catch (NullPointerException | ClassCastException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public float getFloat(String key) {
		Float val = (Float) KEEPER.getValue(key);		
		return val != null ? val : 0.0F;
	}
	
	public boolean setFloat(String key, float value) {
		try {
			FloatEntry entry = (FloatEntry) KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
			
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
	
	public boolean getBoolean(String key) {
		Boolean val = (Boolean) KEEPER.getValue(key);		
		return val != null ? val : false;
	}
	
	public boolean setBoolean(String key, boolean value) {
		try {
			BooleanEntry entry = (BooleanEntry) KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
			
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		
		return false;
	}
}
