package org.lewellen.lsadt.tracking;

import java.util.TreeMap;

public class Bucket {
    private TreeMap<Integer, Sightings> timeslices;

    public Sightings get(int timeMin) {
        if (timeslices.containsKey(timeMin))
            return timeslices.get(timeMin);

        return null;
    }

    public void set(int timeMin, Sightings value) {
        if (timeslices.containsKey(timeMin)) {
            if (value == null)
                timeslices.remove(timeMin);
            else
                timeslices.put(timeMin, value);
        } else if (value != null) {
            timeslices.put(timeMin, value);
        }
    }
    
    public Bucket() {
        timeslices = new TreeMap<Integer, Sightings>();
    }

    public Sightings PreviousTime(Integer time) {
        return get(time-15);
    }
}