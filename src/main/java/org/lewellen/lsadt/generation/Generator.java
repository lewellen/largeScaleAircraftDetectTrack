package org.lewellen.lsadt.generation;

import org.lewellen.lsadt.detection.Complex;
import org.lewellen.lsadt.detection.ComplexMatrix;
import org.lewellen.lsadt.simulation.Coordinate;
import org.lewellen.lsadt.simulation.Flight;
import org.lewellen.lsadt.Tuple;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Generator {
	static class FilterNullsIterator implements Iterator<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> {
		private ArrayList[][] buckets;
		private int rows, columns;
		private int i, j;

		public FilterNullsIterator(ArrayList[][] buckets, int rows, int columns) {
			this.buckets = buckets;
			this.rows = rows;
			this.columns = columns;

			i = 0;
			j = 0;
		}
		
		public boolean hasNext() {
			for( ; i < rows; i++, j = 0)
				for( ; j < columns; j++)
					if(buckets[i][j] != null) {
						return true;
					}

			return false;
		}

		public Tuple<ArrayList<Flight>, Tuple<Integer, Integer>> next() {
			return new Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>(buckets[i][j], new Tuple<Integer, Integer>(i, j++));
		}
	}

	private final ComplexMatrix airplane;
    private final int imageSize;

    // 1 degree approx 69 mi (111 km)
    // 1 image = 9.99 km (1024 x 1024 pixel)
    // 0.09 deg x 0.09 deg

    static private final double minLatitude = -90;
    static private final double maxLatitude = 90;
    static private final double spanLatitude = maxLatitude - minLatitude;
    
    static private final int latitudeBuckets = 4000;
    static private final double dLatitude = spanLatitude / (double)latitudeBuckets;

    static private final double minLongitude = -180;
    static private final double maxLongitude = 180;
    static private final double spanLongitude = maxLongitude - minLongitude;

    static private final int longitudeBuckets = 8000;
    static private final double dLongitude = spanLongitude / (double)longitudeBuckets;

    
    public Generator(ComplexMatrix airplane, int imageSize) {
        this.airplane = airplane;
        this.imageSize = imageSize;
    }

    static public Iterator<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> groupTogether(List<Flight> flights) {
        return new FilterNullsIterator(groupByBucket(flights), latitudeBuckets, longitudeBuckets);
    }
    
    public Tuple<ComplexMatrix, Tuple<Coordinate, Coordinate>> generate(Tuple<ArrayList<Flight>, Tuple<Integer, Integer>> value) {
		ArrayList<Flight> flights = value.Item1;
		int i = value.Item2.Item1;
		int j = value.Item2.Item2;
		
        Coordinate topLeft = new Coordinate(dLatitude * (i + 1) + minLatitude, dLongitude * j + minLongitude);
        Coordinate bottomRight = new Coordinate(dLatitude * (i) + minLatitude, dLongitude * (j + 1) + minLongitude);
        Tuple<Coordinate, Coordinate> frame = new Tuple<Coordinate, Coordinate>(topLeft, bottomRight);
        ComplexMatrix image = makeImage(flights, frame);
        
        return new Tuple<ComplexMatrix, Tuple<Coordinate, Coordinate>>(image, frame);
    }

    private ComplexMatrix makeImage(List<Flight> list, Tuple<Coordinate, Coordinate> frame) {
    	Coordinate topLeft = frame.Item1;
    	Coordinate bottomRight = frame.Item2;
    	
        ComplexMatrix image = new ComplexMatrix(imageSize, imageSize);

        for (Flight f : list) {
            int x = (int)Math.floor((f.Longitude - topLeft.getLongitude()) / dLongitude * image.getColumns());
            int y = (int)Math.floor((f.Latitude - bottomRight.getLatitude()) / dLatitude * image.getRows());
            
            overlay(image, airplane, x, y);
        }

        return image;
    }
    
    public void overlay(ComplexMatrix target, ComplexMatrix source, int x0, int y0) {   	
    	x0 -= source.getColumns() - 1;
    	y0 -= source.getRows() - 1;
    	
    	for(int column = 0; column < source.getColumns(); column++)
    		for(int row = 0; row < source.getRows(); row++) {
    			int x = column + x0;
    			int y = row + y0;
    			
    			if(x < 0 || x >= target.getColumns())
    				continue;
    			
    			if(y < 0 || y >= target.getRows())
    				continue;

    			Complex add = target.get(x, y).add(source.get(row, column));
    			if(add.Real > 0) {
    				target.set(x, y, new Complex(Math.max(0.0, Math.min(1.0, add.Real)), 0.0));
    			}
    		}
    }

    static private ArrayList[][] groupByBucket(List<Flight> flights) {
        ArrayList[][] buckets = new ArrayList[latitudeBuckets][longitudeBuckets];

        for (Flight flight : flights) {
            int i = (int)Math.floor((flight.Latitude - minLatitude) / spanLatitude * latitudeBuckets);
            int j = (int)Math.floor((flight.Longitude - minLongitude) / spanLongitude * longitudeBuckets);

            if (buckets[i][j] == null)
                buckets[i][j] = new ArrayList<Flight>();

            buckets[i][j].add(flight);
        }
        
		return buckets;
	}
}