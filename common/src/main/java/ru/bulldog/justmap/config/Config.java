package ru.bulldog.justmap.config;

import org.jetbrains.annotations.Nullable;

import ru.bulldog.justmap.JustMap;
import ru.bulldog.justmap.config.ConfigKeeper.BooleanEntry;
import ru.bulldog.justmap.config.ConfigKeeper.Entry;
import ru.bulldog.justmap.config.ConfigKeeper.EnumEntry;
import ru.bulldog.justmap.config.ConfigKeeper.FloatEntry;
import ru.bulldog.justmap.config.ConfigKeeper.IntegerEntry;
import ru.bulldog.justmap.config.ConfigKeeper.RangeEntry;
import ru.bulldog.justmap.config.ConfigKeeper.StringEntry;

public abstract class Config {
	
	protected final ConfigKeeper keeper;
	
	protected abstract void registerEntries();
	
	public Config(String group) {
		this.keeper = new ConfigKeeper(group);
		this.registerEntries();
	}
	
	public void saveChanges() {
		this.keeper.save();
	}
	
	@Nullable
	public <T, E extends Entry<T>> E getEntry(ConfigKey key, Class<E> type) {
		return this.keeper.getEntry(key, type);
	}

	protected <T extends Enum<T>, E extends EnumEntry<T>> E getEnumEntry(ConfigKey key, Class<E> type) {
		return keeper.getEntry(key, type);
	}
	
	@Nullable
	public <T, E extends Entry<T>> T getDefault(ConfigKey key, Class<E> type) {
		Entry<T> entry = keeper.getEntry(key, type);
		return entry != null ? entry.getDefault() : null;
	}

	@Nullable
	protected <T extends Enum<T>, E extends EnumEntry<T>> T getEnum(ConfigKey key, Class<E> type) {
		EnumEntry<T> entry = getEntry(key, type);
		if (entry == null) {
			return null;
		}
		return entry.getValue();
	}

	protected <T extends Enum<T>, E extends EnumEntry<T>> T getEnum(ConfigKey key, T defaultValue, Class<E> type) {
		EnumEntry<T> entry = getEntry(key, type);
		if (entry == null) {
			entry = keeper.registerEntry(key, new EnumEntry<>(defaultValue));
		}
		return entry.getValue();
	}
	
	protected String getString(ConfigKey key, String defaultValue) {
		String str = keeper.getValue(key, StringEntry.class);
		if (str == null) {
			StringEntry entry = keeper.registerEntry(key, new StringEntry(defaultValue));
			return entry.getValue();
		}
		return str;
	}
	
	protected String getString(ConfigKey key) {
		String str = keeper.getValue(key, StringEntry.class);
		return str != null ? str : "";
	}
	
	protected boolean setString(ConfigKey key, String value) {
		try {
			StringEntry entry = keeper.getEntry(key, StringEntry.class);
			if (entry == null) return false;
			entry.setValue(value);
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		return false;
	}
	
	protected int getInt(ConfigKey key, int defaultValue) {
		Integer val = keeper.getValue(key, IntegerEntry.class);		
		if (val == null) {
			IntegerEntry entry = keeper.registerEntry(key, new IntegerEntry(defaultValue));
			return entry.getValue();
		}
		return val;
	}
	
	protected int getInt(ConfigKey key) {
		Integer val = keeper.getValue(key, IntegerEntry.class);		
		return val != null ? val : 0;
	}
	
	protected boolean setInt(ConfigKey key, int value) {
		try {
			IntegerEntry entry = keeper.getEntry(key, IntegerEntry.class);
			if (entry == null) return false;
			entry.setValue(value);
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		return false;
	}
	
	protected <T extends Comparable<T>, RE extends RangeEntry<T>> boolean setRanged(ConfigKey key, T value, Class<RE> type) {
		try {
			RangeEntry<T> entry = keeper.getEntry(key, type);
			if (entry == null) return false;
			entry.setValue(value);
			return true;
		} catch (NullPointerException | ClassCastException ex) {
			JustMap.LOGGER.catching(ex);
		}
		return false;
	}
	
	protected float getFloat(ConfigKey key, float defaultValue) {
		Float val = keeper.getValue(key, FloatEntry.class);
		if (val == null) {
			FloatEntry entry = keeper.registerEntry(key, new FloatEntry(defaultValue));
			return entry.getValue();
		}
		return val;
	}
	
	protected float getFloat(ConfigKey key) {
		Float val = keeper.getValue(key, FloatEntry.class);
		return val != null ? val : 0.0F;
	}
	
	protected boolean setFloat(ConfigKey key, float value) {
		try {
			FloatEntry entry = keeper.getEntry(key, FloatEntry.class);
			if (entry == null) return false;
			entry.setValue(value);
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		return false;
	}
	
	protected boolean getBoolean(ConfigKey key, boolean defaultValue) {
		Boolean val = keeper.getValue(key, BooleanEntry.class);
		if (val == null) {
			BooleanEntry entry = keeper.registerEntry(key, new BooleanEntry(defaultValue));
			return entry.getValue();
		}
		return val;
	}
	
	protected boolean getBoolean(ConfigKey key) {
		Boolean val = keeper.getValue(key, BooleanEntry.class);
		return val != null ? val : false;
	}
	
	protected boolean setBoolean(ConfigKey key, boolean value) {
		try {
			BooleanEntry entry = keeper.getEntry(key, BooleanEntry.class);
			if (entry == null) return false;
			entry.setValue(value);
			return true;
		} catch (NullPointerException ex) {
			JustMap.LOGGER.catching(ex);
		}
		return false;
	}
}
