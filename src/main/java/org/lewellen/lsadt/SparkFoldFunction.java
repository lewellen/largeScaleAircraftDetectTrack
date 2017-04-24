package org.lewellen.lsadt;

import java.util.ArrayList;
import java.util.List;


import org.apache.spark.api.java.function.Function2;
import org.lewellen.lsadt.simulation.Coordinate;

public class SparkFoldFunction implements Function2<List<Coordinate>, List<Coordinate>, List<Coordinate>> {
	private static final long serialVersionUID = 1375444318054721739L;
	
	public SparkFoldFunction(){

	}
	
	public List<Coordinate> call(List<Coordinate> v1, List<Coordinate> v2) throws Exception {
		System.out.println(String.format("Concatenating %d with %d.", v1.size(), v2.size()));
		
		ArrayList<Coordinate> output = new ArrayList<Coordinate>();
		output.addAll(v1);
		output.addAll(v2);
		
		return output;
	}
}
