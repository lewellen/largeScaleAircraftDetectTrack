package org.lewellen.lsadt.detection;

import java.util.ArrayList;
import java.util.List;

import org.lewellen.lsadt.Tuple;
import org.lewellen.lsadt.simulation.Coordinate;

public class Detector {
    private final FFTTransform2D fft;
    private final FFTInverseTransform2D ifft;

    private final ComplexMatrix needle;
    private final ComplexMatrix fNeedle;
    
    public Detector(ComplexMatrix needle) {
        this.fft = new FFTTransform2D();
        this.ifft = new FFTInverseTransform2D();

        this.needle = needle;
        this.fNeedle = fft.Transform(needle);
    }

    public List<Coordinate> Detect(Tuple<ComplexMatrix, Tuple<Coordinate, Coordinate>> s) {
    	List<Tuple<Double, Double>> pixels = detect(needle, s.Item1);

    	ComplexMatrix image = s.Item1;
    	Tuple<Coordinate, Coordinate> frame = s.Item2;
    	Coordinate topLeft = frame.Item1;
    	Coordinate bottomRight = frame.Item2;
    	
    	int rows = image.getRows();
    	int columns = image.getColumns();
    	
    	double latitudeSpan = topLeft.getLatitude() - bottomRight.getLatitude();
    	double longitudeSpan = bottomRight.getLongitude() - topLeft.getLongitude();
    	
    	double minLatitude = bottomRight.getLatitude();
    	double minLongitude = topLeft.getLongitude();
    	
    	// map to world coordinates
    	List<Coordinate> output = new ArrayList<Coordinate>();
    	for(Tuple<Double, Double> pixel : pixels) {
    		double row = pixel.Item2;
    		double column = pixel.Item1;
    		
    		Coordinate c = new Coordinate();
    		c.setLatitude((row / rows) * latitudeSpan + minLatitude);
    		c.setLongitude((column / columns) * longitudeSpan + minLongitude);
    		output.add(c);
    	}

    	return output;
    }

    private List<Tuple<Double, Double>> detect(ComplexMatrix needle, ComplexMatrix haystack) {
        ComplexMatrix correlation = correlate(needle, haystack);

        List<Tuple<Integer, Integer>> survivingPixels = threshold(correlation, getThreshold(correlation));
        
        return getCoordinates(correlation, survivingPixels);
    }

    private ComplexMatrix correlate(ComplexMatrix needle, ComplexMatrix haystack) {   
    	return ifft.Transform(fft.Transform(haystack).hadamardProduct(fNeedle));
    }

    private List<Tuple<Double, Double>> getCoordinates(ComplexMatrix recovered, List<Tuple<Integer, Integer>> survivingPixels) {
    	ComponentLabeler labeler = new ComponentLabeler();
        List<List<Tuple<Integer, Integer>>> components = labeler.getComponents(recovered, survivingPixels);

        // Remove noise
        double avg = 0.0;
        for(int i = 0; i < components.size(); i++)
        	avg += components.get(i).size();
        
        avg /= (double)components.size();

        for(int i = 0; i < components.size(); i++)
        	if(components.get(i).size() * 2 < avg)
        		components.remove(i--);

        // Compute the average coordinate of each component
        List<Tuple<Double, Double>> output = new ArrayList<Tuple<Double,Double>>();
        for(List<Tuple<Integer, Integer>> component : components) {
        	Tuple<Double, Double> centroid = new Tuple<Double, Double>(0.0, 0.0);
        	
        	for(Tuple<Integer, Integer> x : component) { 
        		centroid.Item1 += x.Item1;
        		centroid.Item2 += x.Item2;
        	}
        	
        	centroid.Item1 /= (double)component.size();
        	centroid.Item2 /= (double)component.size();

        	output.add(centroid);
        }

        return output;
    }

    private double getThreshold(ComplexMatrix recovered) {
        double max = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < recovered.getRows(); i++)
        	for(int j = 0; j < recovered.getColumns(); j++)
        		max = Math.max(max, recovered.get(i,j).Real);

        return 0.9 * max;
    }

    private List<Tuple<Integer, Integer>> threshold(ComplexMatrix recovered, double max) {
    	List<Tuple<Integer, Integer>> indices = new ArrayList<Tuple<Integer, Integer>>();
    	
        for (int i = 0; i < recovered.getRows(); i++)
            for (int j = 0; j < recovered.getColumns(); j++)
                if (recovered.get(i, j).Real <= max) {
                    recovered.set(i,j, Complex.Zero);
                } else {
                	indices.add(new Tuple<Integer, Integer>(i, j));
                }

        return indices;
    }
}
