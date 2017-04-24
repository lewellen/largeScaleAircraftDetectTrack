package org.lewellen.lsadt;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.TreeMap;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.spark.SparkConf;
import org.apache.spark.api.java.JavaRDD;
import org.apache.spark.api.java.JavaSparkContext;
import org.lewellen.lsadt.detection.ComplexMatrix;
import org.lewellen.lsadt.detection.Detector;
import org.lewellen.lsadt.generation.Generator;
import org.lewellen.lsadt.simulation.Airport;
import org.lewellen.lsadt.simulation.Coordinate;
import org.lewellen.lsadt.simulation.Flight;
import org.lewellen.lsadt.simulation.OpenFlightsFormat;
import org.lewellen.lsadt.simulation.Route;
import org.lewellen.lsadt.simulation.Simulator;
import org.lewellen.lsadt.tracking.LabeledTrail;
import org.lewellen.lsadt.tracking.Tracker;
import org.lewellen.lsadt.tracking.TrailLabeler;

public class App {
	public static void main( String[] args ) {
		Logger logger = LogManager.getLogger(App.class);

		CommandLineArguments arguments = null;
		try {
			arguments = new CommandLineArguments(args);
		} catch (Exception e) {
			logger.error(e.getMessage());
			logger.info("Usage: *.jar --numFlights <number> --sparkMaster <spark://url>");
			return;
		}
		
//		CommandLineArguments arguments = null;
//		try {
//			arguments = new CommandLineArguments(new String[] { "--numFlights", "10000", "--sparkMaster", "auto" } );
//		} catch (Exception e) {
//			e.printStackTrace();
//			return;
//		}

		long start = System.currentTimeMillis();

		logger.info(String.format("Limiting number of flights to at most %d", arguments.NumFlights));
		if(arguments.Mode.equals(CommandLineArguments.SINGLE_MODE)) {
			logger.info("Running in singleton mode.");
		} else {
			logger.info(String.format("Running in cluster mode with sparkMaster: %s.", arguments.SparkMaster));
		}

		List<Flight> flights = getFlights(logger, arguments.NumFlights);

		Tracker tracking = null;
		if(arguments.Mode.equals(CommandLineArguments.SINGLE_MODE))
			tracking = getTrackingResults(logger, flights);
		else
			tracking = getTrackingResults(logger, flights, arguments.SparkMaster);

		TrailLabeler trailLabeler = new TrailLabeler();
		List<LabeledTrail> labeledTrails = trailLabeler.getLabeledTrails(flights, tracking);
		
        logger.warn(String.format("Labeled %d trails out of %d.", labeledTrails.size(), flights.size() ));

        logger.info(String.format("Mean MOE: %.8f m", trailLabeler.meanError(labeledTrails) ));
        
        long stop = System.currentTimeMillis();

        long seconds = (stop - start) / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        
        seconds %= 60;
        minutes %= 60;
        
        logger.info(String.format("%d Hrs %d Minutes %d seconds", hours, minutes, seconds));
        
        logger.info("... exiting.");
	}

	private static List<Flight> getFlights(final Logger logger, int numFlights) {
		logger.info("Loading OpenFlights data from Data\\airports.dat and Data\\routes.dat.");
		
		OpenFlightsFormat format = new OpenFlightsFormat();
        HashMap<String, Airport> airports = format.GetAirports("Data\\airports.dat");
        List<Route> routes = format.GetRoutes("Data\\routes.dat", airports);

        logger.info(String.format("... loaded %d airports, %d routes.", airports.size(), routes.size() ));
        
        // Select all national flights grouped by origin.
        HashMap<String, List<Route>> routesByOrigin = groupBy(where(routes, 
	    		new Predicate<Route>() {
					public boolean Allow(Route r) { return 
						r.Destination.Country.equals("United States") 
//						&& r.Origin.City.equals("Denver")
						&& r.Origin.Country.equals("United States"); 
					}
				}
        	), 
			new Func1<Route, String>() {
				public String Eval(Route r) { return r.Origin.Id; }
			}, 
			new Func1<Route, Route>() {
				public Route Eval(Route r) { return r; }
			} );

        // Schedule the flights on hour intervals from each airport
        TreeMap<Integer, List<Flight>> byLaunch = new TreeMap<Integer, List<Flight>>();
        for(List<Route> originRoute : routesByOrigin.values()) {
        	int i = 0;
        	for(Route route : originRoute) { 
        		Flight flight = new Flight();
        		flight.Latitude = route.Origin.Coordinate.getLatitude();
        		flight.Longitude = route.Origin.Coordinate.getLongitude();
        		flight.Route = route;
        		flight.TimeMin = 0;
        		flight.TimeOffset = i * 60;

        		if(!byLaunch.containsKey(flight.TimeOffset))
        			byLaunch.put(flight.TimeOffset, new ArrayList<Flight>());
        		
        		byLaunch.get(flight.TimeOffset).add(flight);
        		
        		i++;
        	}
        }

        // Shuffle each hour block and only build up to as many flights that are requested.
        List<Flight> flights = new ArrayList<Flight>();
        Iterator<Integer> iterator = byLaunch.navigableKeySet().iterator();
        Random random = new Random();
        
        while(iterator.hasNext()) {
        	List<Flight> list = byLaunch.get(iterator.next());

            for(int i = 0; i < list.size(); i++) {
            	int j = random.nextInt(list.size() - i) + i;
            	Flight f = list.get(i);
            	list.set(i, list.get(j));
            	list.set(j, f);
            }

            if(list.size() > numFlights) {
            	flights.addAll( list.subList(0, numFlights) );
            	break;
            } else {
            	flights.addAll( list );
            	numFlights -= list.size();
            }
        }

        return flights;
	}

	private static Tracker getTrackingResults(final Logger logger, List<Flight> flights) {
		logger.warn(String.format("... chose %d flights to simulate.", flights.size()));

        ComplexMatrix airplaneToDraw = ComplexMatrix.fromImage("Data//airplane.png");

        int imageSize = 512;
        final Generator generator = new Generator(airplaneToDraw, imageSize);
        final Detector detector = new Detector(airplaneToDraw.resize(imageSize, imageSize));
        final Tracker tracking = new Tracker();
        final Simulator simulator = new Simulator();

        final Tuple<Integer, Tuple<Integer, Integer>> totals = new Tuple<Integer, Tuple<Integer, Integer>>(0, new Tuple<Integer, Integer>(0, 0));

        simulator.addStepListener(new Action2<Integer, List<Flight>>() {
			public void Do(Integer time, List<Flight> F) {
				if(time > 1440)
					return;

				logger.info(String.format("Starting time %d", time));
				
				int numImagesProcessed = 0;
				Iterator<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> data = Generator.groupTogether(F);

				List<Coordinate> coordinates = new ArrayList<Coordinate>();
				while(data.hasNext()) {
					coordinates.addAll(detector.Detect(generator.generate(data.next())));
                    numImagesProcessed++;
				}

				for(Coordinate c : coordinates)
					tracking.Track(time, c);

				totals.Item1 = Math.max(totals.Item1, time);
				totals.Item2.Item1 += numImagesProcessed;
				totals.Item2.Item2 += coordinates.size();

				logger.info(String.format("Time: %d\tActual flights: %d\tGenerated %d images\tDetected %d coordinates", time, F.size(), numImagesProcessed , coordinates.size() ));
			}
        });

        simulator.simulateSync(15, flights);
        
		logger.info(String.format("Total simulated time: %d\tTotal images %d\tTotal coordinates %d.", totals.Item1, totals.Item2.Item1, totals.Item2.Item2 ));

		return tracking;
	}

	private static Tracker getTrackingResults(final Logger logger, List<Flight> flights, String sparkMaster) {
		logger.info(String.format("... chose %d flights to simulate.", flights.size()));

		final SparkConf sparkConf = new SparkConf()
			.setMaster(sparkMaster)
			.setAppName("lsadt")
			.setJars(new String[] { System.getProperty("user.dir") + "/lsadt-0.0.1-SNAPSHOT-worker.jar" });

		logger.info(String.format("SparkConf debug string: %s", sparkConf.toDebugString()));
		
		final JavaSparkContext javaSparkContext = new JavaSparkContext(sparkConf);

		logger.info(String.format("JavaSparkContext debug string: AppName: %s, Master: %s, SparkUser: %s, StartTime: %d, Version: %s", 
			javaSparkContext.appName(),
			javaSparkContext.master(),
			javaSparkContext.sparkUser(),
			javaSparkContext.startTime(),
			javaSparkContext.version()
			));

        final ComplexMatrix airplaneToDraw = ComplexMatrix.fromImage("Data//airplane.png");
        final Tracker tracking = new Tracker();
        final Simulator simulator = new Simulator();

        final Tuple<Integer, Tuple<Integer, Integer>> totals = new Tuple<Integer, Tuple<Integer, Integer>>(0, new Tuple<Integer, Integer>(0,0));
        
        simulator.addStepListener(new Action2<Integer, List<Flight>>() {
			public void Do(Integer time, List<Flight> F) {
				if(time > 1440)
					return;

				int numImagesProcessed = 0;

				Iterator<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> data = Generator.groupTogether(F);

				List<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> forSpark = new ArrayList<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>>();
				while(data.hasNext())
					forSpark.add(data.next());

				if(forSpark.size() <= 0)
					return;

				numImagesProcessed = forSpark.size();

				JavaRDD<Tuple<ArrayList<Flight>, Tuple<Integer, Integer>>> workDescs = javaSparkContext.parallelize(forSpark);
				
				// 2015-03-25 GEL Was having problems with "Task not serializable" 
				// errors from Spark. Putting the anonymous functions in their 
				// own classes seems to fix the issue.
				
				// Split up the work to each node.
				JavaRDD<List<Coordinate>> mapped = workDescs.map(new SparkMapFunction(airplaneToDraw));
				
				List<List<Coordinate>> coordinates = mapped.collect();

				for(List<Coordinate> C : coordinates)
					for(Coordinate c : C)
						tracking.Track(time, c);
				
				logger.info(String.format("Time: %d\tActual flights: %d\tGenerated %d images\tDetected %d coordinates", time, F.size(), numImagesProcessed, coordinates.size() ));
				
				totals.Item1 = Math.max(totals.Item1, time);
				totals.Item2.Item1 += numImagesProcessed;
				totals.Item2.Item2 += coordinates.size();
			}
        });

        simulator.simulateSync(15, flights);

		javaSparkContext.stop();
		javaSparkContext.close();

		logger.info(String.format("Total simulated time: %d\tTotal images %d\tTotal coordinates %d.", totals.Item1, totals.Item2.Item1, totals.Item2.Item2 ));
		
		return tracking;
	}

	private static <T> List<T> where(List<T> list, Predicate<T> p) {
		List<T> output = new ArrayList<T>();
		for(T t : list)
			if(p.Allow(t))
				output.add(t);
		return output;
	}
	
	private static <T, K, V> HashMap<K, List<V>> groupBy(List<T> list, Func1<T, K> keySelect, Func1<T, V> valueSelect) {
		HashMap<K, List<V>> map = new HashMap<K, List<V>>();

		for(T t : list) {
			K k = keySelect.Eval(t);
			V v = valueSelect.Eval(t);
			
			if(!map.containsKey(k)) {
				map.put(k, new ArrayList<V>());
			}
			
			map.get(k).add(v);
		}
		
		return map;
	}
}
