package ru.bulldog.justmap.util;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;

import ru.bulldog.justmap.JustMap;

public class TaskManager implements Executor {
    private final Queue<Runnable> workQueue = new ConcurrentLinkedQueue<>();
    private final Thread thread;
    private boolean running = true;
    
    private String name = JustMap.MODID;
    
    private static Map<String, TaskManager> managers = new HashMap<>();
    
    public static TaskManager getManager(String name) {
    	if (managers.containsKey(name)) {
    		TaskManager manager = managers.get(name);
    		if (!manager.isRunning()) {
    			manager = new TaskManager(name);
    			managers.replace(name, manager);
    		}
    		
    		return manager;
    	}
    	
    	TaskManager manager = new TaskManager(name);
    	managers.put(name, manager);
    	
    	return manager;
    }
    
    public static void shutdown() {
    	managers.forEach((name, manager) -> {
    		if (manager.isRunning()) manager.stop();
    	});
    }
    
    private TaskManager(String name) {
    	this.name += "-" + name;
    	this.thread = new Thread(this::work, this.name);    	
    	thread.start();
    }

    @Override
    public void execute(Runnable command) {
    	workQueue.offer(command);
    	LockSupport.unpark(this.thread);
    }
    
    public void stop() {
    	this.execute(() -> {
    		this.running = false;
    	});    	
    }
    
    public boolean isRunning() {
    	return this.running;
    }

    private void work() {
    	while (running) {
            Runnable nextTask = workQueue.poll();
            if (nextTask != null) {
                nextTask.run();
            } else {
            	LockSupport.park("Tasks waiting...");
            }
        }
    }
}
