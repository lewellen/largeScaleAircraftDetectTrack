package org.lewellen.lsadt.tracking;

import org.lewellen.lsadt.simulation.Coordinate;

public class TrackedCoordinate extends Coordinate {
	public boolean isSingleton;
	public int portsUsed;
	public int flightId;
	
	public TrackedCoordinate(){
		isSingleton = false;
		portsUsed = 0;
		flightId = -1;
	}
}
