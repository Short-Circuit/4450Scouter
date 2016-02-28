package com.orf4450.frcscouter.master;

import java.util.LinkedList;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author ShortCircuit908
 *         Created on 2/22/2016
 */
public class UploadScheduler implements Runnable {
	private static final ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
	private static final LinkedList<UploadTask> queue = new LinkedList<>();
	static{
		new UploadScheduler();
	}

	public static void scheduleTask(UploadTask task) {
		synchronized (queue) {
			System.out.println("========UPLOAD TASK QUEUED========");
			queue.add(task);
		}
	}

	private UploadScheduler() {
		executor.scheduleAtFixedRate(
				this,
				0,
				500,
				TimeUnit.MILLISECONDS
		);
	}

	@Override
	public void run() {
		synchronized (queue) {
			if (!queue.isEmpty()) {
				System.out.println("========EXECUTING UPLOAD TASK========");
				UploadTask task = queue.pop();
				new Thread(task).start();
			}
		}
	}
}
