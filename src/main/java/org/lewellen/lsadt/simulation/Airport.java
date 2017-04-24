package org.lewellen.lsadt.simulation;

import java.io.Serializable;

public class Airport implements Serializable {
    private static final long serialVersionUID = 4262149208100532013L;
	
    public String Id;
    public String Name;
    public String City;
    public String Country;
    public String IataFAA;
    public String Icao;
    
    public Coordinate Coordinate;
    
    public String Altitude;
    public String TimeZone;
    public String Dst;
    public String TzDbTimeZone;

    @Override
    public boolean equals(Object obj) {
    	if(obj == null)
    		return super.equals(obj);
    	
    	if(!(obj instanceof Airport))
    		return super.equals(obj);
    	
    	Airport other = (Airport) obj;
    	
    	return Id.equals(other.Id);
    };

    @Override
    public int hashCode() {
    	return Id.hashCode();
    };

    @Override
    public String toString() {
        return String.format("[%s] %s, %s (%s)", IataFAA, City, Country, Coordinate.toString());
    };
}
