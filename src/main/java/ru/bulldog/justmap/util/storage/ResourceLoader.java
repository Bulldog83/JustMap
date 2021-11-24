package ru.bulldog.justmap.util.storage;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import ru.bulldog.justmap.JustMap;

public class ResourceLoader {
	private final static String RESOURCES_FOLDER = "/assets";
	private final static String DEFAULT_LOCATION = String.format("%s/%s", RESOURCES_FOLDER, JustMap.MODID);

	private final URL location;

	public ResourceLoader(String path) {
		String resourceLocation = String.format("%s/%s", DEFAULT_LOCATION, path);
		this.location = ResourceLoader.class.getResource(resourceLocation);
	}

	public InputStream getInputStream() throws IOException {
		return this.location.openStream();
	}
}