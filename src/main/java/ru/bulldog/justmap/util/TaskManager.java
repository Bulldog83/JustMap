package ru.bulldog.justmap.util;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;

import ru.bulldog.justmap.JustMap;

public class TaskManager implements Executor {
    private final Queue<Runnable> workQueue = new ConcurrentLinkedQueue<>();
    private volatile boolean isRunning = false;
    
    public TaskManager() {}

    @Override
    public void execute(Runnable command) {
    	workQueue.offer(command);
    	if (!isRunning) start();
    }

    public void start() {
        isRunning = true;
        new Thread(new Task(), JustMap.MODID).start();
    }
    
    public void stop() {
    	isRunning = false;
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
                } else {
                	stop();
                }
            }
        }
    }
}
