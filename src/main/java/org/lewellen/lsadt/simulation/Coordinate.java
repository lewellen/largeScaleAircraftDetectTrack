package org.lewellen.lsadt.simulation;

import java.io.Serializable;

public class Coordinate implements Serializable {
	private static final long serialVersionUID = -3168931888113038765L;

	private double latitude, longitude;

    public Coordinate() {
    	
    }

    public Coordinate(double latitude, double longitude) {
    	setLatitude(latitude);
    	setLongitude(longitude);
    }
    
    public double getLatitude() {
        return latitude;
    }
    
    public void setLatitude(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < -90.0 || value > 90.0)
            throw new IllegalArgumentException(String.format("Latitude value %f must be between -90.0 and 90.0", value));

        latitude = value;
    }

    public double getLongitude(){
        return longitude;
    }
    
    public void setLongitude(double value) {
        if (Double.isNaN(value) || Double.isInfinite(value) || value < -180.0 || value > 180.0)
            throw new IllegalArgumentException(String.format("Longitude value %f must be between -180.0 and 180.0", value));

        longitude = value;
    }
    
    public double DistancToMi(Coordinate B) {
        return Math.sqrt(Math.pow(getLongitude() - B.getLongitude(), 2) + Math.pow(getLatitude() - B.getLatitude(), 2)) * 69.0 /*mi/deg on avg*/;
    }
    
    @Override
    public String toString() {
        return String.format("%f, %f", latitude, longitude);
    }
}