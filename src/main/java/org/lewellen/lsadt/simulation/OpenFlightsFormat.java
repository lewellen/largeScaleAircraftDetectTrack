package org.lewellen.lsadt.simulation;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class OpenFlightsFormat {
	public HashMap<String, Airport> GetAirports(String filePath) {
		CSVFormat format = new CSVFormat();
		format.UsesQuotes = true;
		
		List<List<String>> parsedLines = format.readAllLines(filePath);
		
		HashMap<String, Airport> airports = new HashMap<String, Airport>();
		for (List<String> parsedLine : parsedLines) {
			Airport airport = toAirport(parsedLine);
			if (!airports.containsKey(airport.Id))
				airports.put(airport.Id, airport);
		}

		return airports;
	}

	public List<Route> GetRoutes(String filePath, HashMap<String, Airport> airports) {
		CSVFormat format = new CSVFormat();
		format.UsesQuotes = false;

		List<List<String>> parsedLines = format.readAllLines(filePath);
		if (parsedLines == null)
			return null;

		List<Route> routes = new ArrayList<Route>();
		for (List<String> line : parsedLines) {
			Route route = toRoute(line, airports);
			if(route != null)
				routes.add(route);
		}

		return routes;
	}

	private Airport toAirport(List<String> x) {
		Airport airport = new Airport();
		airport.Id = x.get(0);
		airport.Name = x.get(1);
		airport.City = x.get(2);
		airport.Country = x.get(3);
		airport.IataFAA = x.get(4);
		airport.Icao = x.get(5);
		airport.Coordinate = new Coordinate(Double.parseDouble(x.get(6)), Double.parseDouble(x.get(7)));
		airport.Altitude = x.get(8);
		airport.TimeZone = x.get(9);
		airport.Dst = x.get(10);
		airport.TzDbTimeZone = x.size() > 11 ? x.get(11) : null;

		return airport;
	}

	private Route toRoute(List<String> x, HashMap<String, Airport> airports){
		if (x.get(3) != "\\N" && x.get(5) != "\\N") {
			Route route = new Route();
			route.Airline = x.get(0);
			route.Origin = airports.get(x.get(3));
			route.Destination = airports.get(x.get(5));

			if(route.Origin == null || route.Destination == null)
				return null;
			
			return route;
		}
		
		return null;
	}
}
