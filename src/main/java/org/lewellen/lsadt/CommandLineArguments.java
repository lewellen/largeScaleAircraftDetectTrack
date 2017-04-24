package org.lewellen.lsadt;

import java.net.Inet4Address;

public class CommandLineArguments {
	public static final String SINGLE_MODE = "singleton";
	public static final String CLUSTER_MODE = "cluster";

	public String Mode;
	public String SparkMaster;
	public int NumFlights;
	
	public CommandLineArguments(String[] args) throws Exception {
		if(args.length != 4)
			throw new Exception("Not enough arguments.");
		
		if(!args[0].equals("--numFlights")) 
			throw new Exception("Expected --numFlights");

		int numFlights = 50;
		try {
			numFlights = Integer.parseInt(args[1]);
		} catch (NumberFormatException nfe) {
			System.out.println(String.format("Expected --numFlights <number>. Got --numFlights %s", args[1]));
			return;
		}
		
		NumFlights = numFlights;

		if(!args[2].equals("--sparkMaster"))
			throw new Exception("Expected --sparkMaster");
		
		String mode = SINGLE_MODE;
		String sparkMaster = null;
		if(args[3].trim().equals("") || args[3].equals("null")) {
			mode = SINGLE_MODE;
			sparkMaster = null;
		} else {
			mode = CLUSTER_MODE;
			sparkMaster = args[3];
			
			if(sparkMaster == "auto" || sparkMaster.equalsIgnoreCase("auto")) {
				String[] options = new String[] {
					System.getProperty("SPARK_MASTER"),
					System.getProperty("MASTER"),
					String.format("spark://%s:7077", Inet4Address.getLocalHost().getHostAddress())
				};

				for(String option : options) {
					if(option == null || option.length() <= 0)
						continue;
					
					sparkMaster = option;
					break;
				}
			}
		}
		
		Mode = mode;
		SparkMaster = sparkMaster;
	}
}
