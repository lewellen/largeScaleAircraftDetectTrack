package org.lewellen.lsadt.threading;

import org.lewellen.lsadt.Action1;

public class Parallelizer<T> {
	private final ProducerConsumerQueue<T> queue;
	private final Thread[] threads;
		
	public Parallelizer(int threadCount, final Action1<T> action) {
		queue = new ProducerConsumerQueue<T>();
		threads = new Thread[threadCount];
		
		for(int i = 0; i < threads.length; i++) {
			threads[i] = new Thread(new Runnable() {
				public void run() {
					T t = null;
					while((t = queue.dequeue()) != null)
						action.Do(t);
				}
			});
		
			threads[i].setDaemon(true);
			threads[i].start();
		}
	}
	
	public void doInParallel(T t){
		queue.enqueue(t);
	}
	
	public void join() {
		queue.doneAdding();
		
		for(int i = 0; i < threads.length; i++)
			try {
				threads[i].join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
	}
}
