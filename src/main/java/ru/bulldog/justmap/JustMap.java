package ru.bulldog.justmap;

import ru.bulldog.justmap.util.Logger;
import ru.bulldog.justmap.util.tasks.TaskManager;
import net.fabricmc.api.ModInitializer;

public class JustMap implements ModInitializer {
	public static final String MODID = "justmap";
	public static final Logger LOGGER = Logger.get();
	public static final TaskManager WORKER = TaskManager.getManager("worker");
	
	@Override
	public void onInitialize() {}
}
