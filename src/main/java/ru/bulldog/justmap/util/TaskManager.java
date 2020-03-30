package ru.bulldog.justmap.util;

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
    
    public TaskManager() {
    	this.thread = new Thread(this::work, this.name);
    	thread.start();
    }
    
    public TaskManager(String name) {
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
