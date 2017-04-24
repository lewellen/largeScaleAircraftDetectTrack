package org.lewellen.lsadt.threading;

import org.lewellen.lsadt.Tuple;
import org.lewellen.lsadt.threading.ProducerConsumerQueue;

import junit.framework.TestCase;

public class ProducerConsumerQueueTests extends TestCase {
	public void testQueueDequeue() {
		ProducerConsumerQueue<Integer> Q = new ProducerConsumerQueue<Integer>();
		
		int expected = 1;		
		Q.enqueue(expected);
		
		int received = Q.dequeue();

		assertEquals(expected, received);
	}
	
	public void testDoneAdding() throws InterruptedException {
		final Integer N = 10000;
		final Tuple<Integer, Integer> stats = new Tuple<Integer, Integer>(0, 0);

		final ProducerConsumerQueue<Integer> Q = new ProducerConsumerQueue<Integer>();
		
		Thread P = new Thread(new Runnable() {
			public void run() {
				for(int i = 0; i <= N; i++)
					Q.enqueue(i);
			}
		});

		Thread C = new Thread(new Runnable() {
			public void run() {
				Integer value;
				while((value = Q.dequeue()) != null) {
					stats.Item1 ++;
					stats.Item2 += value;
				}				
			}
		});
		
		C.start();

		P.start();
		P.join();
		Q.doneAdding();
		
		C.join();
		
		assertEquals(N + 1, stats.Item1.longValue());
		assertEquals(N * (N + 1) / 2, stats.Item2.intValue());
	}
}
