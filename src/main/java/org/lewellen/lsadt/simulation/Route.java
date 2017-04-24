package org.lewellen.lsadt.simulation;

import java.io.Serializable;

public class Route implements Serializable {
	private static final long serialVersionUID = -546332414481516216L;

	public String Airline;
    public Airport Origin;
    public Airport Destination;

    public double getDistanceMiles() {
    	return Origin.Coordinate.DistancToMi(Destination.Coordinate);
    	
//        return Math.sqrt(Math.pow(Origin.Longitude - Destination.Longitude, 2) + Math.pow(Origin.Latitude - Destination.Latitude, 2)) * 69.0 /*mi/deg on avg*/;
    }
}
