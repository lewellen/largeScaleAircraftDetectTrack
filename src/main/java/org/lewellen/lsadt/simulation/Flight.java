package org.lewellen.lsadt.simulation;

import java.io.Serializable;

public class Flight implements Serializable {
	private static final long serialVersionUID = 6090141951808274815L;

	public double Latitude;
    public double Longitude;
    public Route Route;

    public int TimeMin;
    public int TimeOffset;

    public double DistanceFlownMiles;

    private boolean atDestination;
    
    public boolean getAtDestination(){
    	return atDestination;
    }
    
    public Flight() {
        atDestination = false;
    }

    public void FlyForMinutes(int minutes) {
        if (atDestination)
            return;

        TimeMin += minutes;

        double milesPerMinute = 575 /*mi/hr*/ * 1 / 60.0 /*hr/min*/;
        double milesFlown = minutes * milesPerMinute;

        double distMi = Route.getDistanceMiles();
        
        DistanceFlownMiles += milesFlown;
        if (DistanceFlownMiles > distMi) {
            DistanceFlownMiles = distMi;
            atDestination = true;
        }

        Longitude = Route.Origin.Coordinate.getLongitude() + (DistanceFlownMiles * ((Route.Destination.Coordinate.getLongitude() - Route.Origin.Coordinate.getLongitude()) / distMi));
        Latitude = Route.Origin.Coordinate.getLatitude() + (DistanceFlownMiles * ((Route.Destination.Coordinate.getLatitude() - Route.Origin.Coordinate.getLatitude()) / distMi));
    }
}

