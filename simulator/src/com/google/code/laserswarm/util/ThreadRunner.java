package com.google.code.laserswarm.util;

import java.util.Map;
import java.util.Set;

import com.google.code.laserswarm.conf.Configuration;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Maps;
import com.lyndir.lhunath.lib.system.logging.Logger;

public class ThreadRunner<T extends Runnable> extends Thread {

	private Map<T, Thread>		waiting		= Maps.newLinkedHashMap();
	private Map<T, Thread>		running		= Maps.newLinkedHashMap();
	private Map<T, Thread>		completed	= Maps.newLinkedHashMap();
	private boolean				complete;

	private int					MaxThreads	= Configuration.getInstance().demThreads;

	private static final Logger	logger		= Logger.get(ThreadRunner.class);

	public ThreadRunner() {
	}

	public ThreadRunner(Set<T> threads) {
		addThreads(threads);
	}

	public void addThread(T t) {
		synchronized (waiting) {
			try {
				Thread tr = Thread.class.cast(t);
				waiting.put(t, tr);
			} catch (ClassCastException e) {
				waiting.put(t, new Thread(t));
			}
		}
	}

	public void addThreads(Set<T> threads) {
		for (T t : threads)
			addThread(t);
	}

	public Map<T, Thread> getCompleted() {
		synchronized (completed) {
			return completed;
		}
	}

	public ImmutableMap<T, Thread> getRunning() {
		synchronized (running) {
			return ImmutableMap.copyOf(running);
		}
	}

	public ImmutableMap<T, Thread> getWaiting() {
		synchronized (waiting) {
			return ImmutableMap.copyOf(waiting);
		}
	}

	public boolean isComplete() {
		return complete;
	}

	@Override
	public void run() {
		complete = false;
		while (running.size() > 0 || waiting.size() > 0) {
			if (waiting.keySet().iterator().hasNext()) {
				synchronized (waiting) {
					synchronized (running) {
						T tr = waiting.keySet().iterator().next();
						running.put(tr, waiting.get(tr));
						waiting.remove(tr);
						running.get(tr).start();

						logger.inf("Starting %s (waiting: %s, running: %s)",
								tr, waiting.size(), running.size());
					}
				}
			}

			do {
				ImmutableMap<T, Thread> runningNow;
				synchronized (running) {
					runningNow = ImmutableMap.copyOf(running);
				}
				for (T run : runningNow.keySet()) {
					Thread thread = runningNow.get(run);
					if (!thread.isAlive()) {
						synchronized (completed) {
							synchronized (running) {
								completed.put(run, thread);
								running.remove(thread);
							}
						}
					}
				}

				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
					logger.wrn(e, "Interrupted while sleeping");
				}
			} while (running.size() >= MaxThreads);
		}
		complete = true;
	}

	public void setMaxThreads(int maxThreads) {
		MaxThreads = maxThreads;
	}

	public void waitForMerge() {
		while (!isComplete())
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				logger.wrn(e, "Interrupted while sleeping");
				break;
			}
	}

}
