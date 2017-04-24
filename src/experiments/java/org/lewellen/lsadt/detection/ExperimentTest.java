package org.lewellen.lsadt.detection;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

import junit.framework.TestCase;

import org.lewellen.lsadt.Tuple;
import org.lewellen.lsadt.generation.Generator;
import org.lewellen.lsadt.simulation.Coordinate;
import org.lewellen.lsadt.simulation.Flight;

public class ExperimentTest {
	public void densityPerformance() {
		Random prng = new Random(System.currentTimeMillis());

		int imageSize = 512;
		ComplexMatrix airplane = ComplexMatrix.fromImage("Data\\airplane.png");
		Generator generator = new Generator(airplane, imageSize);
		Detector detector = new Detector(airplane.resize(imageSize, imageSize));

		System.out.println(airplane.getColumns() * airplane.getRows());

		double[] ratios = new double[101];

		for(int trial = 0; trial < 1; trial++) {
			for(int numFlights = 1; numFlights <= 100; numFlights++) {
	
				List<Flight> flights = new ArrayList<>();
				for(int i = 1; i <= numFlights; i++) {
					Flight flight = new Flight();
					flight.Latitude = prng.nextDouble()*0.045;
					flight.Longitude = prng.nextDouble()*0.045;
					flights.add(flight);
				}
	
				Iterator<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> bucketIterator = Generator.groupTogether(flights);
				
				List<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> buckets = new ArrayList<>();
				while(bucketIterator.hasNext())
					buckets.add(bucketIterator.next());
	
				Tuple<ArrayList<Flight>, Tuple<Integer, Integer>> bucket = buckets.get(0);
	
				Tuple<ComplexMatrix, Tuple<Coordinate, Coordinate>> imageFrame = generator.generate(bucket);
				List<Coordinate> coordinates = detector.Detect(imageFrame);

				ratios[numFlights] += coordinates.size() / (double) flights.size();
			}

			System.out.println(trial);
		}
		
		for(int i = 0; i < ratios.length; i++)
			ratios[i] /= 30.0;
		
		for(int i = 0; i < ratios.length; i++)
			System.out.println(String.format("%.4f", ratios[i]));
		
	}
}
