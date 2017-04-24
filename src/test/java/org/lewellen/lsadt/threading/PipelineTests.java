package org.lewellen.lsadt.threading;

import org.lewellen.lsadt.Action1;
import org.lewellen.lsadt.Func1;
import org.lewellen.lsadt.Tuple;
import org.lewellen.lsadt.threading.Pipeline;
import org.lewellen.lsadt.threading.ProducerConsumerQueue;

import junit.framework.TestCase;

public class PipelineTests extends TestCase {
	public void testChainDoneAdding() throws InterruptedException {
		final Long N = 10000L;
		final Tuple<Long, Long> stats = new Tuple<Long, Long>(0L, 0L);

		Pipeline<Long, Long> pipeline = new Pipeline<Long, Long>(
			new Action1<ProducerConsumerQueue<Long>>() {
				public void Do(ProducerConsumerQueue<Long> queue) {
					for(long i = 0; i <= N; i++)
						queue.enqueue(i);
				}
			},
			new Func1<Long, Long>() {
				public Long Eval(Long value) {
					return value * value;
				}
			},
			new Action1<Long>() {
				public void Do(Long value) {
					stats.Item1 ++;
					stats.Item2 += value;
				}
			}
		);
		
		pipeline.DoPipeline();

		assertEquals(N + 1, stats.Item1.longValue());
		assertEquals(N * (N + 1) * (2 * N + 1) / 6, stats.Item2.longValue());
	}

}
