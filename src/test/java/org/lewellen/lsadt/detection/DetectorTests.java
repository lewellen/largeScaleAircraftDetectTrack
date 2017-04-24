package org.lewellen.lsadt.detection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.lewellen.lsadt.Tuple;
import org.lewellen.lsadt.generation.Generator;
import org.lewellen.lsadt.simulation.Coordinate;
import org.lewellen.lsadt.simulation.Flight;

import junit.framework.TestCase;

public class DetectorTests extends TestCase {
	public void testDetect() { 
		ComplexMatrix needle = new ComplexMatrix(128, 128);
		ComplexMatrix haystack = new ComplexMatrix(128, 128);
		
		needle.set(0,  0,  Complex.PositiveRealUnit);
		needle.set(0,  1,  Complex.PositiveRealUnit);
		needle.set(1,  0,  Complex.PositiveRealUnit);
		needle.set(1,  1,  Complex.PositiveRealUnit);

		haystack.set(63 + 0, 93 + 0,  Complex.PositiveRealUnit);
		haystack.set(63 + 0, 93 + 1,  Complex.PositiveRealUnit);
		haystack.set(63 + 1, 93 + 0,  Complex.PositiveRealUnit);
		haystack.set(63 + 1, 93 + 1,  Complex.PositiveRealUnit);

		haystack.set(13 + 0, 32 + 0,  Complex.PositiveRealUnit);
		haystack.set(13 + 0, 32 + 1,  Complex.PositiveRealUnit);
		haystack.set(13 + 1, 32 + 0,  Complex.PositiveRealUnit);
		haystack.set(13 + 1, 32 + 1,  Complex.PositiveRealUnit);

		Detector detector = new Detector(needle);
		
		Coordinate topLeft = new Coordinate();
		topLeft.setLatitude(0);
		topLeft.setLongitude(0);

		Coordinate bottomRight = new Coordinate();
		bottomRight.setLatitude(-1);
		bottomRight.setLongitude(-1);
		
		Tuple<Coordinate, Coordinate> window = new Tuple<Coordinate, Coordinate>(topLeft, bottomRight);
		Tuple<ComplexMatrix, Tuple<Coordinate, Coordinate>> input = new Tuple<ComplexMatrix, Tuple<Coordinate,Coordinate>>(haystack, window);

		List<Coordinate> coordinates = detector.Detect(input);
		
		assertEquals(2, coordinates.size());
		
		double epsilon = 1e-12;
		
		Coordinate coordinateA = coordinates.get(0);
		assert( Math.abs(coordinateA.getLongitude() - 0.10) <= epsilon  );
		assert( Math.abs(coordinateA.getLatitude() - 0.25) <= epsilon );

		Coordinate coordinateB = coordinates.get(1);
		assert( Math.abs(coordinateB.getLongitude() - 0.5) <= epsilon );
		assert( Math.abs(coordinateB.getLatitude() - 0.73) <= epsilon );
	}

	public void testGeneratorDetectorHandoff() {
		// Boulder, CO Latitude and Longitude
		Coordinate expected = new Coordinate(40.023499, -105.270416);
		
		Flight flight = new Flight();
		flight.Latitude = expected.getLatitude();
		flight.Longitude = expected.getLongitude();

		List<Flight> flights = new ArrayList<Flight>();
		flights.add(flight);
		
		int imageSize = 512;
		ComplexMatrix airplane = ComplexMatrix.fromImage("Data\\airplane.png");
		Generator generator = new Generator(airplane, imageSize);
		Detector detector = new Detector(airplane.resize(imageSize, imageSize));


		Iterator<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> bucketIterator = generator.groupTogether(flights);
		
		List<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> buckets = new ArrayList<>();
		while(bucketIterator.hasNext())
			buckets.add(bucketIterator.next());
		
		assertEquals(1, buckets.size());
		
		Tuple<ArrayList<Flight>, Tuple<Integer, Integer>> bucket = buckets.get(0);
		assertEquals(bucket.Item2.Item1.intValue(), 2889);
		assertEquals(bucket.Item2.Item2.intValue(), 1660);

		
		Tuple<ComplexMatrix, Tuple<Coordinate, Coordinate>> imageFrame = generator.generate(bucket);
		List<Coordinate> coordinates = detector.Detect(imageFrame);
		Coordinate actual = coordinates.get(0);
					
		double epsilon = 1e-3;
		assertTrue(Math.abs(actual.getLatitude() - flight.Latitude) < epsilon && Math.abs(actual.getLongitude() - flight.Longitude) < epsilon);
	}
}
