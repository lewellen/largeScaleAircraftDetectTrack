package org.lewellen.lsadt.simulation;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.lewellen.lsadt.Resource;

public class CSVFormat {
	public boolean UsesQuotes;

	public CSVFormat() {
		UsesQuotes = true;
	}
	
	public List<List<String>> readAllLines(String filePath) {
		List<List<String>> lines = new ArrayList<List<String>>();

		Resource resource = new Resource();
		BufferedReader bufferedReader = null;
		try {
			bufferedReader = new BufferedReader(new InputStreamReader(resource.getFile(filePath)));
			
			String line = null;
			while ((line = bufferedReader.readLine()) != null)
				lines.add(split(line));

		} catch (IOException e) {
			return null;

		} finally {
			if (bufferedReader != null)
				try {
					bufferedReader.close();
				} catch (IOException e) {

				}
		}

		return lines;
	}

	public List<String> split(String line) {
		List<String> parts = new ArrayList<String>();
		if(!UsesQuotes) {
			String[] P = line.split(",");
			for(String p : P)
				parts.add(p.trim());
			
			return parts;
		};

		boolean inQuote = false;
		int k = 0;
		for (int j = 0; j < line.length(); j++) {
			if (line.charAt(j) == '\"') {
				inQuote = !inQuote;
			} else if (line.charAt(j) == ',' && !inQuote) {
				String part = line.substring(k, j).trim();
				if (part.charAt(0) == '\"')
					part = part.substring(1, part.length());
				if (part.charAt(part.length() - 1) == '\"')
					part = part.substring(0, part.length() - 1);

				parts.add(part);

				k = j + 1;
			}
		}

		String lastPart = line.substring(k + 1, line.length() - 1);
		if(lastPart.length() > 0)
			parts.add(lastPart);
		
		return parts;
	}
}
