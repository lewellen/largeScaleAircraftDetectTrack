package org.lewellen.lsadt.tracking;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lewellen.lsadt.simulation.Coordinate;
import org.lewellen.lsadt.simulation.Flight;
import org.lewellen.lsadt.simulation.Route;

public class TrailLabeler {
	public List<LabeledTrail> getLabeledTrails(List<Flight> flights, Tracker tracking) {
		HashMap<Integer, Trail> trails = tracking.getTrails();
        List<LabeledTrail> labeledTrails = new ArrayList<LabeledTrail>();

        for (Trail trail : trails.values()) {
        	Coordinate start = trail.Coordinates.firstEntry().getValue();
        	Coordinate end = trail.Coordinates.lastEntry().getValue();
        	
        	Route opt = getBestRouteForTrail(flights, start, end);

        	// 2015-04-06 GEL Think about this. Today you were thinking that 
        	// if the mean error was more than 25 meters, then the trail 
        	// *must* be an incomplete trail, but do you really want to reject 
        	// incomplete trails?
        	double meanError = meanOrthogonalErrorMeters(opt, trail);
        	if(meanError > 25.0) {
        		// Incomplete trail
        		continue;
        	}

        	LabeledTrail labeledTrail = new LabeledTrail();
        	labeledTrail.Trail = trail;
        	labeledTrail.Route = opt;
        	labeledTrail.MeanOrthogonalError = meanError;
        	
        	labeledTrails.add(labeledTrail);

//        	logger.debug(String.format("Matched trailId %d with ", trail.FlightId));
//        	logger.debug(String.format("\tFrom: %s\tTo: %s", opt.Origin.toString(), opt.Destination.toString() ));
//        	logger.debug(String.format("\tMOE: %.3f m", meanError));
//        	
//        	for(Coordinate coordinate : trail.Coordinates.values())
//        		logger.debug(String.format("\t%s", coordinate.toString()));

        }
        
		return labeledTrails;
	}

	public double meanError(List<LabeledTrail> labeledTrails){
        double error = 0.0;
        double errorCount = 0.0;
        
        for(LabeledTrail labeledTrail : labeledTrails){
        	error += labeledTrail.MeanOrthogonalError;
        	errorCount++;
        }

        return error / errorCount;
	}
	
	private Route getBestRouteForTrail(List<Flight> flights, Coordinate start, Coordinate end) {
		double minError = Double.POSITIVE_INFINITY;
		Route minRoute = null;
		
		for(Flight flight : flights) {
			Route route = flight.Route;
			
			double errorStart = route.Origin.Coordinate.DistancToMi(start);
			double errorEnd = route.Destination.Coordinate.DistancToMi(end);

			double error = errorStart + errorEnd;
			if(error < minError) {
				minError = error;
				minRoute = route;
			}
		}
		
		return minRoute;
	}
	
	private double meanOrthogonalErrorMeters(Route route, Trail trail) {
        double error = 0.0;
        double errorCount = 0.0;
        
    	double dLong = route.Destination.Coordinate.getLongitude() - route.Origin.Coordinate.getLongitude();
    	double dLat = route.Destination.Coordinate.getLatitude() - route.Origin.Coordinate.getLatitude();
    	
    	double m = dLat / dLong;
    	double b = route.Origin.Coordinate.getLatitude() - m * route.Origin.Coordinate.getLongitude();
    	
    	for(Coordinate x : trail.Coordinates.values()) {
    		error += Math.abs(x.getLatitude() - m * x.getLongitude() - b) / Math.sqrt(1 + m * m);
    		errorCount ++;
    	}

    	return error = error / errorCount * 111 /*km/deg*/ * 1000 /*m/km*/;
	}
}
