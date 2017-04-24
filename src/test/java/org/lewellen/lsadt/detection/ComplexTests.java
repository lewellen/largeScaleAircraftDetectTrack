package org.lewellen.lsadt.detection;

import junit.framework.TestCase;

public class ComplexTests extends TestCase {
	
	public void testAdd() {
		Complex a = new Complex(5.0, 3.0);
		Complex b = new Complex(3.0, 5.0);
		Complex c = a.add(b);
		
		assertEquals(new Complex(8.0, 8.0), c);
	}

	public void testExponential() {
		Complex a = new Complex(2, Math.PI);
		Complex c = a.exp();

		assertEquals(new Complex(- Math.E * Math.E), c);
	}
	
	public void testMultiplyComplex() {
		Complex a = new Complex(3.0, -5.0);
		Complex b = new Complex(3.0, 5.0);
		Complex c = a.multiply(b);
		
		assertEquals(new Complex(34.0), c);
	}

	public void testMultiplyScalar() {
		Complex a = new Complex(3.0, -5.0);
		double b = 3;
		Complex c = a.multiply(b);

		assertEquals(new Complex(9, -15.0), c);
	}
	
	public void testToString(){
		Complex a = new Complex(1.0, 1.0);
		
		assertEquals("1.00 + 1.00i", a.toString());
	}
}
