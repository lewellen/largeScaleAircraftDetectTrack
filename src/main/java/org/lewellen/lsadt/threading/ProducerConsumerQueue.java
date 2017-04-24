package org.lewellen.lsadt.threading;

import java.util.concurrent.ArrayBlockingQueue;

import org.lewellen.lsadt.Tuple;

public class ProducerConsumerQueue<T> {
	private static final String PASS_ALONG = "passAlong";
	private static final String DONE_ADDING = "doneAdding";

	private final ArrayBlockingQueue<Tuple<String, T>> queue;
	private boolean isDoneAdding;
	
	public ProducerConsumerQueue(){
		queue = new ArrayBlockingQueue<Tuple<String,T>>(64);
		isDoneAdding = false;
	}
		
	synchronized public T dequeue(){
		if(isDoneAdding && queue.size() <= 0)
			return null;
		
		Tuple<String, T> x = null;
		try {
			x = queue.take();
		} catch (InterruptedException e) {
			
		}
		
		if(x != null && x.Item1.equals(PASS_ALONG))
			return x.Item2;
		
		return null;
	}

	public void doneAdding() {
		if(isDoneAdding)
			return;
		
		enqueue(DONE_ADDING, null);

		isDoneAdding = true;
	}
	
	public void enqueue(T t){
		if(isDoneAdding || t == null)
			return;
		
		enqueue(PASS_ALONG, t);
	}

	public boolean getIsDoneAdding(){
		return isDoneAdding;
	}

	private void enqueue(String type, T t) {
		try {
			queue.put(new Tuple<String, T>(type, t));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
