package org.lewellen.lsadt.detection;

import java.util.ArrayList;
import java.util.List;

import org.lewellen.lsadt.Tuple;

import scala.Array;
import junit.framework.TestCase;

public class ComponentLabelerTests extends TestCase {
	public void testGetComponents(){
		ComplexMatrix A = new ComplexMatrix(5, 5);
		A.set(0, 1, Complex.PositiveRealUnit);
		A.set(1, 1, Complex.PositiveRealUnit);
		A.set(1, 2, Complex.PositiveRealUnit);
		A.set(4, 1, Complex.PositiveRealUnit);
		
		List<Tuple<Integer, Integer>> B = new ArrayList<Tuple<Integer,Integer>>();
		B.add(new Tuple<Integer, Integer>(0, 1));
		B.add(new Tuple<Integer, Integer>(1, 1));
		B.add(new Tuple<Integer, Integer>(1, 2));
		B.add(new Tuple<Integer, Integer>(4, 1));
		
		ComponentLabeler labler = new ComponentLabeler();
		List<List<Tuple<Integer, Integer>>> C = labler.getComponents(A, B);
		assertEquals(2, C.size());
		
		List<Tuple<Integer, Integer>> c0 = C.get(0);
		assertEquals(3, c0.size());
		
		List<Tuple<Integer, Integer>> c1 = C.get(1);
		assertEquals(1, c1.size());
	}
}
