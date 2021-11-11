package ru.bulldog.justmap.config;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.config.ConfigKeeper.BooleanEntry;
import ru.bulldog.justmap.config.ConfigKeeper.Entry;
import ru.bulldog.justmap.config.ConfigKeeper.FloatEntry;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerEntry;
import ru.bulldog.justmap.config.ConfigKeeper.RangeEntry;
import ru.bulldog.justmap.config.ConfigKeeper.StringEntry;

public abstract class Config {
	
	protected final static ConfigKeeper KEEPER = ConfigKeeper.getInstance();
	
	public abstract void saveChanges();
	
	public <E extends Entry<?>> E getEntry(String key) {
		return KEEPER.getEntry(key);
	}
	
	public <T> T getDefault(String key) {
		Entry<T> entry = KEEPER.getEntry(key);
		return entry != null ? entry.getDefault() : null;
	}
	
	public String getString(String key) {
		String str = KEEPER.getValue(key);
		return str != null ? str : "";
	}
	
	public void setString(String key, String value) {
		try {
			StringEntry entry = KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
	}
	
	public int getInt(String key) {
		Integer val = KEEPER.getValue(key);		
		return val != null ? val : 0;
	}
	
	public void setInt(String key, int value) {
		try {
			IntegerEntry entry = KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
        } catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
    }
	
	public <T extends Comparable<T>> void setRanged(String key, T value) {
		try {
			RangeEntry<T> entry = KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
		} catch (NullPointerException | ClassCastException ex) {
			JustMap.LOGGER.catching(ex);
		}
	}
	
	public float getFloat(String key) {
		Float val = KEEPER.getValue(key);		
		return val != null ? val : 0.0F;
	}
	
	public void setFloat(String key, float value) {
		try {
			FloatEntry entry = KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
	}
	
	public boolean getBoolean(String key) {
		Boolean val = KEEPER.getValue(key);		
		return val != null ? val : false;
	}
	
	public void setBoolean(String key, boolean value) {
		try {
			BooleanEntry entry = KEEPER.getEntry(key);
			entry.setValue(value);
			KEEPER.set(key, entry);
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
	}
}
