package ru.bulldog.justmap.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import ru.bulldog.justmap.JustMap;

public class TaskManager implements Executor {
    private final Queue<Runnable> workQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isRunning = false;
    
    private String name = JustMap.MODID;
    private long downtimeLimit = 1000;
    private long downtime = 0;
    
    public TaskManager() {}
    
    public TaskManager(String name) {
    	this.name += "-" + name;
    }

    @Override
    public void execute(Runnable command) {
    	workQueue.offer(command);
    	if (!isRunning) start();
    }

    public void start() {
        isRunning = true;
        new Thread(new Task(), name).start();
    }
    
    public void stop() {
    	isRunning = false;
    	if (workQueue.size() > 0) workQueue.clear();
    }
    
    public boolean isRunning() {
    	return isRunning;
    }

    private final class Task implements Runnable {
        @Override
        public void run() {
            while (isRunning) {
                Runnable nextTask = workQueue.poll();
                if (nextTask != null) {
                    nextTask.run();
                    downtime = 0;
                } else {
                	long time = System.currentTimeMillis();
                	if (downtime == 0) downtime = time;
                	if (time - downtime > downtimeLimit) {
                		isRunning = false;
                		downtime = 0;
                	}
                }
            }
        }
    }
}
