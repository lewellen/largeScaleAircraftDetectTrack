package org.lewellen.lsadt.detection;

import junit.framework.TestCase;

public class FFTTransform1DTests extends TestCase {
	public void testTransformSin() {
		Complex[] X = new Complex[32];
		for(int i = 0; i < X.length; i++)
			X[i] = new Complex(Math.sin(i / (double)X.length * 2.0 * Math.PI));

		FFTTransform1D fft = new FFTTransform1D();
		
		Complex[] F = fft.Transform(X);
		
		for(int i = 0; i < F.length; i++)
			if(i != 1 && i != F.length - 1)
				assertEquals(Complex.Zero, F[i]);
		
		assertEquals(new Complex(0, -16.0), F[1]);
		assertEquals(new Complex(0, +16.0), F[F.length - 1]);	
	}

	public void testTransformCos() {
		Complex[] X = new Complex[32];
		for(int i = 0; i < X.length; i++)
			X[i] = new Complex(Math.cos(i / (double)X.length * 2.0 * Math.PI));

		FFTTransform1D fft = new FFTTransform1D();
		
		Complex[] F = fft.Transform(X);
		
		for(int i = 0; i < F.length; i++)
			if(i != 1 && i != F.length - 1)
				assertEquals(Complex.Zero, F[i]);
		
		assertEquals(new Complex(+16.0, 0), F[1]);
		assertEquals(new Complex(+16.0, 0), F[F.length - 1]);	
	}
}