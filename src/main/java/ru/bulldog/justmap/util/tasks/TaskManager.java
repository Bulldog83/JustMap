package ru.bulldog.justmap.util.tasks;

import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.locks.LockSupport;
import java.util.function.Function;

import ru.bulldog.justmap.JustMap;

public class TaskManager implements Executor {
    private final Queue<Task> workQueue = new ConcurrentLinkedQueue<>();
    private final QueueBlocker queueBlocker;
    private final ThreadGroup group;
    private final Thread[] workers;
    private String name = JustMap.MODID;
    
    private boolean running = true;
    
    private static final Map<String, TaskManager> managers = new HashMap<>();
    
    public static TaskManager getManager(String name) {
    	return getManager(name, 1);
    }
    
    public static TaskManager getManager(String name, int maxThreads) {
    	if (managers.containsKey(name)) {
    		TaskManager manager = managers.get(name);
    		if (!manager.isRunning()) {
    			manager = new TaskManager(name, maxThreads);
    			managers.replace(name, manager);
    		}
    		
    		return manager;
    	}
    	
    	TaskManager manager = new TaskManager(name, maxThreads);
    	managers.put(name, manager);
    	
    	return manager;
    }
    
    public static void shutdown() {
    	long timeout = 5000;
    	managers.forEach((name, manager) -> {
    		if (manager.isRunning()) {
    			manager.stop();
    			long time = System.currentTimeMillis();
    			while(manager.isRunning()) {
    				long now = System.currentTimeMillis();
    				if (now - time > timeout) {
    					manager.running = false;
    					manager.workQueue.clear();
    				}
    			}
    			JustMap.LOGGER.debug("{} stopped", manager.name);
    		}
    	});
    }
    
    private TaskManager(String name, int maxThreads) {
    	this.name += "-" + name;
    	this.queueBlocker = new QueueBlocker(this.name + "-blocker");
    	this.workers = new Thread[maxThreads];
    	this.group = new ThreadGroup(this.name);
    	for (int i = 0; i < maxThreads; i++) {
    		String threadName = String.format("%s-%d", this.name, i + 1);
    		this.workers[i] = new Thread(group, this::work, threadName);
    		this.workers[i].start();
    	}
    }
    
    public void execute(String reason, Runnable command) {
    	this.workQueue.offer(new Task(reason, command));
    	this.unpark();
    }
    
    private void unpark() {
    	for (Thread worker : workers) {
    		LockSupport.unpark(worker);
    	}
    }

    @Override
    public void execute(Runnable command) {
    	this.execute(null, command);
    }
    
    public <T> CompletableFuture<T> run(Function<CompletableFuture<T>, Runnable> function) {
		return this.run(null, function);
	}
    
    public <T> CompletableFuture<T> run(String reason, Function<CompletableFuture<T>, Runnable> function) {
    	CompletableFuture<T> completableFuture = new CompletableFuture<>();
    	this.execute(reason, function.apply(completableFuture));
    	return completableFuture;
    }
    
    public void stop() {
    	this.execute("Stopping " + this.name, () -> {
    		this.running = false;
    	});    	
    }
    
    public int queueSize() {
    	return this.workQueue.size();
    }
    
    public boolean isRunning() {
    	return this.running;
    }

    private void work() {
    	while (running) {
    		Task nextTask = workQueue.poll();
    		if (nextTask != null) {
    			if (nextTask.hasReason()) {
    				JustMap.LOGGER.debug(nextTask);
    			}
    			nextTask.run();
            } else {
            	LockSupport.park(queueBlocker);
            }
        }
    }
    
    private static class Task implements Runnable {
    	
    	private final Runnable task;
    	private final String reason;
    	
    	private Task(String reason, Runnable task) {
    		this.reason = reason;
    		this.task = task;
    	}
    	
    	public String getReason() {
    		return this.reason;
    	}
    	
    	public boolean hasReason() {
    		return this.reason != null;
    	}
    	
    	@Override
		public void run() {
			this.task.run();
		}
    	
    	@Override
    	public String toString() {
    		return this.getReason();
    	}
    }
    
    private static class QueueBlocker {
    	private final String name;
    	
    	private QueueBlocker(String name) {
    		this.name = name;
    	}
    	
    	@Override
    	public String toString() {
    		return this.name;
    	}
    }
}
