package org.lewellen.lsadt.detection;

import junit.framework.TestCase;

public class ComplexMatrixTests extends TestCase {
	public void testMultiply() { 
		ComplexMatrix A = new ComplexMatrix(2, 2);
		A.set(0, 0, new Complex(1.0));
		A.set(0, 1, new Complex(2.0));
		A.set(1, 0, new Complex(3.0));
		A.set(1, 1, new Complex(4.0));
		
		ComplexMatrix B = A.multiply(A);
		
		assertEquals(new Complex(7.0), B.get(0,0));
		assertEquals(new Complex(10.0), B.get(0,1));
		assertEquals(new Complex(15.0), B.get(1,0));
		assertEquals(new Complex(22.0), B.get(1,1));
	}
	
	public void testHadamardProduct() {
		ComplexMatrix A = new ComplexMatrix(2, 2);
		A.set(0, 0, new Complex(1.0));
		A.set(0, 1, new Complex(2.0));
		A.set(1, 0, new Complex(3.0));
		A.set(1, 1, new Complex(4.0));

		ComplexMatrix B = A.hadamardProduct(A);
		assertEquals(new Complex(1.0), B.get(0,0));
		assertEquals(new Complex(4.0), B.get(0,1));
		assertEquals(new Complex(9.0), B.get(1,0));
		assertEquals(new Complex(16.0), B.get(1,1));
	}
}
