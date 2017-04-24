package org.lewellen.lsadt.threading;

import org.lewellen.lsadt.Action1;
import org.lewellen.lsadt.Func1;

public class Pipeline<T1, T2> {
	private final Action1<ProducerConsumerQueue<T1>> producer;
	private final Func1<T1, T2> producerConsumer;
	private final Action1<T2> consumer;
	
	public Pipeline(Action1<ProducerConsumerQueue<T1>> producer, Func1<T1, T2> producerConsumer, Action1<T2> consumer) {
		this.producer = producer;
		this.producerConsumer = producerConsumer;
		this.consumer = consumer;
	}
	
	public void DoPipeline() throws InterruptedException {
		final ProducerConsumerQueue<T1> PtoPC = new ProducerConsumerQueue<T1>();
		final ProducerConsumerQueue<T2> PCtoC = new ProducerConsumerQueue<T2>();
		
		Thread P = new Thread(new Runnable() {
			public void run() {
				producer.Do(PtoPC);
			}
		});

		Thread PC = new Thread(new Runnable() {
			public void run() {
				T1 value = null;
				while((value = PtoPC.dequeue()) != null)
					PCtoC.enqueue(producerConsumer.Eval(value));
			}
		});

		Thread C = new Thread(new Runnable() {
			public void run() {
				T2 value = null;
				while((value = PCtoC.dequeue()) != null)
					consumer.Do(value);
			}
		});

		C.start();
		PC.start();
		P.start();
		P.join();
		
		PtoPC.doneAdding();
		
		PC.join();
		
		PCtoC.doneAdding();
		
		C.join();
	}
}
