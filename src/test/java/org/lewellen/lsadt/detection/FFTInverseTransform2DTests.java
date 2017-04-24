package org.lewellen.lsadt.detection;

import junit.framework.TestCase;

public class FFTInverseTransform2DTests extends TestCase {
	public void testTransform() {
		ComplexMatrix A = new ComplexMatrix(512, 512);
		
		for(int i = 0; i < A.getRows(); i++)
			for(int j = 0; j < A.getColumns(); j++)
				A.set(i, j, new Complex( 
					Math.sin(i / (double)A.getRows() * 2.0 * Math.PI),
					Math.cos(j / (double)A.getColumns() * 2.0 * Math.PI)
				));

		FFTTransform2D fft = new FFTTransform2D();
		ComplexMatrix F = fft.Transform(A);

		FFTInverseTransform2D ifft = new FFTInverseTransform2D();
		ComplexMatrix B = ifft.Transform(F);
		
		for(int i = 0; i < A.getRows(); i++)
			for(int j = 0; j < A.getColumns(); j++)
				assertEquals(A.get(i, j), B.get(i, j));
	}
	
	public void testCentering() {
		double[][] input = new double[4][4];
		input[0][0] = 0;
		input[0][1] = 1;
		input[1][0] = 2;
		input[1][1] = 3;
		
		input[0][2] = 4;
		input[0][3] = 5;
		input[1][2] = 6;
		input[1][3] = 7;
		
		input[2][0] = 8;
		input[2][1] = 9;
		input[3][0] = 10;
		input[3][1] = 11;
		
		input[2][2] = 12;
		input[2][3] = 13;
		input[3][2] = 14;
		input[3][3] = 15;
		
		double[][] expected = new double[4][4];
		expected[0][0] = 12;
		expected[0][1] = 13;
		expected[1][0] = 14;
		expected[1][1] = 15;
		
		expected[0][2] = 8;
		expected[0][3] = 9;
		expected[1][2] = 10;
		expected[1][3] = 11;
		
		expected[2][0] = 4;
		expected[2][1] = 5;
		expected[3][0] = 6;
		expected[3][1] = 7;
		
		expected[2][2] = 0;
		expected[2][3] = 1;
		expected[3][2] = 2;
		expected[3][3] = 3;
		
		double[][] output = new double[4][4];
		
        for(int column = 0; column < 4; column++) {
        	for(int row = 0; row < 4; row++) {
        		int actRow = (row + 2) % 4;
        		int actCol = (column + 2) % 4;

        		output[actRow][actCol] = input[row][column];
        	}
        }
        
        for(int row = 0; row < 4; row++)
        	for(int column = 0; column < 4; column++)
        		assertEquals(expected[row][column], output[row][column]);
	}
}
