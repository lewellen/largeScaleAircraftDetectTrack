package org.lewellen.lsadt.tracking;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.NavigableSet;

import org.lewellen.lsadt.Tuple;
import org.lewellen.lsadt.simulation.Coordinate;

public class Tracker {
    private int latBuckets, longBuckets;
    private Bucket[][] buckets;

    private int nextId;
    private HashMap<Integer, Trail> trails; 
    
    public Tracker() {
        latBuckets = 181;
        longBuckets = 361;
        
        buckets = new Bucket[latBuckets][longBuckets];
        for (int i = 0; i < latBuckets; i++)
            for (int j = 0; j < longBuckets; j++)
                buckets[i][j] = new Bucket();

        nextId = 0;
        trails = new HashMap<Integer, Trail>();
    }

    public Bucket getBucket(Coordinate c) {
        int i = (int)Math.floor(c.getLatitude()) + 90;
        int j = (int)Math.floor(c.getLongitude()) + 180;

        return buckets[i][j];
    }

    public List<Bucket> getBuckets(Coordinate c) {
        int i = (int)Math.floor(c.getLatitude()) + 90;
        int j = (int)Math.floor(c.getLongitude()) + 180;

        int window = 5;
        
        List<Bucket> list = new ArrayList<Bucket>();
        for (int u = -window; u <= window; u++) {
            if (i + u < 0 || i + u >= latBuckets)
                continue;

            for (int v = -window; v <= window; v++) {
                if (j + v < 0 || j + v >= longBuckets)
                    continue;

                list.add(buckets[i + u][j + v]);
            }
        }

        return list;
    }

    public HashMap<Integer, Trail> getTrails() {
    	return trails;
    }
    
    public void Track(int time, Coordinate c) {
    	// Get a list of buckets that have sightings from the previous timestep
        List<Bucket> buckets = this.getBuckets(c);
        for(int i = 0; i < buckets.size(); i++)
        	if(buckets.get(i).PreviousTime(time) == null)
        		buckets.remove(i--);
        
        // Get all the coordinates that are allowed to have an extra connection
        // -- Which means if the previous point was a singleton (take off point) then it can have one more connection
        // -- or if it is an in between point, it is allowed to have one more connection
        List<Tuple<Double, Tuple<Integer, TrackedCoordinate>>> olderFlights = new ArrayList<Tuple<Double,Tuple<Integer, TrackedCoordinate>>>();
        for(Bucket bucket : buckets){
        	Sightings previous = bucket.PreviousTime(time);
        	for(Tuple<Integer, TrackedCoordinate> obs : previous) {
        		if(obs.Item2.isSingleton && obs.Item2.portsUsed == 1)
        			continue;
        		
        		if(obs.Item2.portsUsed == 2)
        			continue;
        		
        		olderFlights.add(new Tuple<Double, Tuple<Integer, TrackedCoordinate>>(dist(c, obs.Item2), obs));
        	}
        }
        
        // Remove those trails (with two or more points that are not colinear with the observed coordinate)
        
        for(int i = 0; i < olderFlights.size(); i++) {
        	Tuple<Double, Tuple<Integer, TrackedCoordinate>> x = olderFlights.get(i);

        	// These things shouldn't happen, but if they do, ignore and carray on as usual.
        	int f = x.Item2.Item2.flightId;
        	if(f < 0 || !trails.containsKey(f))
        		continue;
        	
        	// Only consider those trails that have atleast two points
        	Trail t = trails.get(f);
        	if(t.Coordinates.size() < 2)
        		continue;

        	if(!isColinear(c, t) || !isInRightDirection(c, t))
        		olderFlights.remove(i--);
        }

        // Calculate the distance between c and all of the history points that qualify and sort from smallest to largest distance.

        Collections.sort(olderFlights, new Comparator<Tuple<Double, Tuple<Integer, TrackedCoordinate>>>() {
			public int compare(Tuple<Double, Tuple<Integer, TrackedCoordinate>> o1, Tuple<Double, Tuple<Integer, TrackedCoordinate>> o2) {
				return o1.Item1.compareTo(o2.Item1);
			}
        });
        
        
        // Get the nearest neighbor (if one exists)
        Tuple<Double, Tuple<Integer, TrackedCoordinate>> nearestNeighbor = null;
        if(olderFlights.size() > 0)
        	nearestNeighbor = olderFlights.get(0);

        // Create the new entry that will be added to the bucket
        TrackedCoordinate tc = new TrackedCoordinate();
        tc.setLatitude(c.getLatitude());
        tc.setLongitude(c.getLongitude());

        // Figure out the flightId to use.
        int flightId = -1;
        if (nearestNeighbor != null) {
            // Use the nearest neighbor's flightId
        	flightId = nearestNeighbor.Item2.Item1;
            
        	// Update both points number of connections used count
            nearestNeighbor.Item2.Item2.portsUsed++;
            tc.portsUsed++;
            tc.flightId = flightId;
        }
        else {
        	// Nearest neighbor doesn't exist, assume that point represents a brand new flight
            flightId = newFlightId();

            // Mark this new point as a singleton.
            tc.isSingleton = true;
            tc.flightId = flightId;

            // Keep track of the trail
            Trail track = new Trail();
            track.FlightId = flightId;
            trails.put(flightId, track);
        }

        // Build the trail and populate the bucket with tc.
        trails.get(flightId).Coordinates.put(time, c);
        
        Bucket b = getBucket(c);
        if (b.get(time) == null)
            b.set(time, new Sightings());

        b.get(time).add(new Tuple<Integer, TrackedCoordinate>(flightId, tc));
    }

	private boolean isColinear(Coordinate c, Trail t) {
    	// t.Coordinates keys on time; get the last two observations
    	NavigableSet<Integer> descKeySet = t.Coordinates.descendingKeySet();
    	Iterator<Integer> descKeySetIterator = descKeySet.iterator();

    	Coordinate cMinusOne = t.Coordinates.get(descKeySetIterator.next());
    	Coordinate cMinusTwo = t.Coordinates.get(descKeySetIterator.next());

    	return Math.abs(cMinusTwo.DistancToMi(cMinusOne) + cMinusOne.DistancToMi(c) - cMinusTwo.DistancToMi(c)) <= 1e-4;
    }

    private boolean isInRightDirection(Coordinate c, Trail t) {
    	NavigableSet<Integer> descKeySet = t.Coordinates.descendingKeySet();
    	Iterator<Integer> descKeySetIterator = descKeySet.iterator();
    	Coordinate cMinusOne = t.Coordinates.get(descKeySetIterator.next());
    	Coordinate cMinusTwo = t.Coordinates.get(descKeySetIterator.next());

    	double cLat = c.getLatitude() - cMinusTwo.getLatitude();
    	double cLong = c.getLongitude() - cMinusTwo.getLongitude();

    	double c1Lat = cMinusOne.getLatitude() - cMinusTwo.getLatitude();
    	double c1Long = cMinusOne.getLongitude() - cMinusTwo.getLongitude();

    	return cLat * c1Lat + cLong * c1Long > 0;
	}

    private int newFlightId() {
        return ++nextId;
    }

    private double dist(Coordinate x, Coordinate y) {
        return Math.sqrt(Math.pow(x.getLatitude() - y.getLatitude(), 2) + Math.pow(x.getLongitude() - y.getLongitude(), 2));
    }
}
