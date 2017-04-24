package org.lewellen.lsadt;

import java.util.ArrayList;
import java.util.List;


import org.apache.spark.api.java.function.Function;
import org.lewellen.lsadt.detection.ComplexMatrix;
import org.lewellen.lsadt.detection.Detector;
import org.lewellen.lsadt.generation.Generator;
import org.lewellen.lsadt.simulation.Coordinate;
import org.lewellen.lsadt.simulation.Flight;

public class SparkMapFunction implements Function<Tuple<ArrayList<Flight>,Tuple<Integer,Integer>>, List<Coordinate>> {
	private static final long serialVersionUID = -737083700191094304L;

	private final ComplexMatrix airplaneToDraw;
	
	public SparkMapFunction(ComplexMatrix airplane) {
		this.airplaneToDraw = airplane;
	}
	
	public List<Coordinate>  call(Tuple<ArrayList<Flight>, Tuple<Integer, Integer>> workDesc) throws Exception {
		int imageSize = 512;
        Generator generator = new Generator(airplaneToDraw, imageSize);
        Detector detector = new Detector(airplaneToDraw.resize(imageSize, imageSize));

        System.out.println(String.format("Mapping %d flights from bucket (%d, %d)", workDesc.Item1.size(), workDesc.Item2.Item1, workDesc.Item2.Item2));

        Tuple<ComplexMatrix, Tuple<Coordinate, Coordinate>> image = generator.generate(workDesc);

        System.out.println(String.format("Generated %d x %d image for coordinate frame %s x %s.", image.Item1.getRows(), image.Item1.getColumns(), image.Item2.Item1.toString(), image.Item2.Item2.toString() ));

        List<Coordinate> coordinates = detector.Detect(image);
        
        System.out.println(String.format("Found %d coordinates.", coordinates.size()));
        
        return coordinates;
	}
}
