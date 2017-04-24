package org.lewellen.lsadt.detection;

import junit.framework.TestCase;

public class FFTInverseTransform1DTests extends TestCase {
	public void testTransform() {
		Complex[] X = new Complex[32];
		for(int i = 0; i < X.length; i++)
			X[i] = new Complex(Math.sin(i / (double)X.length * 2.0 * Math.PI));

		FFTTransform1D fft = new FFTTransform1D();		
		Complex[] F = fft.Transform(X);

		FFTInverseTransform1D ifft = new FFTInverseTransform1D();		
		Complex[] Y = ifft.Transform(F);
		
		for(int i = 0; i < Y.length; i++)
			assertEquals(X[i],  Y[i]);
	}
}