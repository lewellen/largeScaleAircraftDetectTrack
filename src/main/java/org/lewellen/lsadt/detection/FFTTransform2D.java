package org.lewellen.lsadt.detection;

import org.lewellen.lsadt.Action1;
import org.lewellen.lsadt.threading.Parallelizer;

public class FFTTransform2D  {
    public ComplexMatrix Transform(final ComplexMatrix A) {
    	final FFTTransform1D fft = new FFTTransform1D();
        final ComplexMatrix B = new ComplexMatrix(A.getRows(), A.getColumns());
        int numThreads = 2;
        
        Parallelizer<Integer> firstStep = new Parallelizer<Integer>(numThreads, new Action1<Integer>() {
			public void Do(Integer i) {
	        	// Create a vector of the i'th row
	        	Complex[] rowBefore = new Complex[A.getColumns()];
	        	for(int j = 0; j < rowBefore.length; j++)
	        		rowBefore[j] = A.get(i, j);
	        	
	        	// Transform and write it back
	        	Complex[] rowAfter = fft.Transform(rowBefore);
	        	for(int j = 0; j < rowBefore.length; j++)
	        		B.set(i, j, rowAfter[j]);
			}
		});
        
        Parallelizer<Integer> secondStep = new Parallelizer<Integer>(numThreads, new Action1<Integer>() {
			public void Do(Integer j) {
	        	// Create a vector of the j'th column
	        	Complex[] columnBefore = new Complex[A.getRows()];
	        	for(int i = 0; i < columnBefore.length; i++)
	        		columnBefore[i] = B.get(i, j);
	        	
	        	// Transform it and write it back
	        	Complex[] columnAfter = fft.Transform(columnBefore);
	        	for(int i = 0; i < columnBefore.length; i++)
	        		B.set(i, j, columnAfter[i]);
			}
		});
        
        for(int i = 0; i < A.getRows(); i++)
        	firstStep.doInParallel(i);
        
        firstStep.join();

        for(int j = 0; j < A.getColumns(); j++) 
        	secondStep.doInParallel(j);

        secondStep.join();
        
        return B;
	}
}