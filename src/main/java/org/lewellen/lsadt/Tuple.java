package org.lewellen.lsadt;

import java.io.Serializable;

// 2015-03-25 GEL Serializable is required to use Spark.
public class Tuple<T1, T2> implements Serializable {
	private static final long serialVersionUID = -2401650195440249145L;

	public T1 Item1;
	public T2 Item2;
	
	public Tuple() {
		
	}
	
	public Tuple(T1 t1, T2 t2) {
		Item1 = t1;
		Item2 = t2;
	}
}