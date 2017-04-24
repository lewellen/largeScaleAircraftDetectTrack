package org.lewellen.lsadt.simulation;

import java.util.ArrayList;
import java.util.List;

import org.lewellen.lsadt.Action2;

public class Simulator implements Runnable {
	private Thread thread;

	private int minuteInterval;
	private List<Flight> flights;

	private List<Action2<Integer, List<Flight>>> stepListeners;

	public Simulator() {
		thread = new Thread(this);
		stepListeners = new ArrayList<Action2<Integer, List<Flight>>>();
	}

	public void addStepListener(Action2<Integer, List<Flight>> listener) {
		stepListeners.add(listener);
	}

	public void simulateAsync(int minuteInterval, List<Flight> flights) {
		this.minuteInterval = minuteInterval;
		this.flights = flights;

		thread.start();
	}

	public void run() {
		int elapsedMinutes = 0;

		for (int time = 0; flights.size() > 0; time += minuteInterval) {
			for (int i = 0; i < flights.size(); i++) {
				Flight flight = flights.get(i);
				if (flight.TimeOffset > time)
					continue;

				if (flight.getAtDestination())
					flights.remove(i--);

				flight.FlyForMinutes(minuteInterval);
			}

			notifyStep(elapsedMinutes, filter(elapsedMinutes));

			elapsedMinutes += minuteInterval;
		}
	}

	public void simulateSync(int minuteInterval, List<Flight> flights) {
		this.minuteInterval = minuteInterval;
		this.flights = new ArrayList<Flight>(flights);

		run();
	}

	private List<Flight> filter(int elapsedMinutes) {
		List<Flight> output = new ArrayList<Flight>();
		for (Flight f : flights) {
			if (Double.isNaN(f.Latitude) || Double.isNaN(f.Longitude))
				continue;

			if (f.TimeOffset > elapsedMinutes)
				continue;

			output.add(f);
		}

		return output;
	}

	private void notifyStep(int elapsedMinutes, List<Flight> filter) {
		for (Action2<Integer, List<Flight>> listener : stepListeners)
			try {
				listener.Do(elapsedMinutes, filter);
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
}