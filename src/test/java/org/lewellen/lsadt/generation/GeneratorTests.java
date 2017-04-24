package org.lewellen.lsadt.generation;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lewellen.lsadt.Tuple;
import org.lewellen.lsadt.detection.ComplexMatrix;
import org.lewellen.lsadt.detection.Detector;
import org.lewellen.lsadt.simulation.Coordinate;
import org.lewellen.lsadt.simulation.Flight;

import junit.framework.TestCase;

public class GeneratorTests extends TestCase {
	public void test2DArrayList() {
		ArrayList[][] A = new ArrayList[5][5];
		for(int i = 0; i < 5; i++)
			for(int j = 0; j < 5; j++)
				A[i][j] = new ArrayList<String>();
		
		A[3][3].add("test");
		
		assertEquals("test", A[3][3].get(0));
	}
}
