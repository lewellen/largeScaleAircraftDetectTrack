package org.lewellen.lsadt.tracking;

import java.util.TreeMap;

import org.lewellen.lsadt.simulation.Coordinate;

public class Trail {
	public int FlightId;
	public TreeMap<Integer, Coordinate> Coordinates;

	public Trail(){
		Coordinates = new TreeMap<Integer, Coordinate>();
	}
}
