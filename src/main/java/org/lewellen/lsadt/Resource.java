package org.lewellen.lsadt;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

public class Resource {
	private final Logger logger;
	
	public Resource() {
		logger = LogManager.getLogger(Resource.class);
	}
	
	public InputStream getFile(String filePath) throws FileNotFoundException {
		logger.info(String.format("Looking for %s", filePath));
		
		String[] possiblePaths = new String[] {
				filePath,
				"./" + filePath,
				"~/" + filePath,
				filePath.replace("\\", "/"),
				"./" + filePath.replace("\\", "/"),
				"~/" + filePath.replace("\\", "/"),
		};

		for(String possiblePath : possiblePaths) {
			// First check the file system.
			File file = new File(possiblePath);
			if(file.exists()) {
				logger.info(String.format("Found %s on local disk.", possiblePath));
				return new FileInputStream(file);
			} else {
				logger.info(String.format("Did not find %s on local disk.", possiblePath));
			}
		}

		String fileName = getFileNameWithExtension(filePath);
		String jarPath = "/" + fileName;

		// Try relative to the position of Resource in the jar
		InputStream relativeClassStream = getClass().getResourceAsStream(jarPath);
		if(relativeClassStream != null) {
			logger.info(String.format("Found %s in jar.", jarPath));
			return relativeClassStream;
		} else {
			logger.info(String.format("Did not find %s in jar.", jarPath));
		}

		// Try relative to the root of Resource in the jar (in theory should be 
		// the same, but Java is Java.
		InputStream relativeRootStream = getClass().getClassLoader().getResourceAsStream(jarPath);
		if(relativeRootStream != null) {
			logger.info(String.format("Found %s in jar.", jarPath));
			return relativeRootStream;
		} else {
			logger.info(String.format("Did not find %s in jar.", jarPath));
		}

		// Do a deep search
		ClassLoader[] classLoaders = new ClassLoader[] {
			getClass().getClassLoader(),
			ClassLoader.getSystemClassLoader(),
		};

		for(ClassLoader classLoader : classLoaders){
			List<URL> matches = new ArrayList<URL>();
			matches.addAll(getMatches(fileName, classLoader, ""));
			matches.addAll(getMatches(fileName, classLoader, getFileExtension(filePath)));
			matches.addAll(getMatches(fileName, classLoader, fileName));
			
			int n = matches.size();
			logger.info(String.format("Identified %d matches", matches.size()));
			
			for(int i = 0; i < n; i++) {
				URL match = matches.get(i);
				logger.info(String.format("Trying to load %s", match.toString()));
				
				InputStream stream = null;
				try {
					stream = match.openStream();
				} catch (IOException e) {
					
				}

				if(stream != null) {
					logger.info(String.format("Loaded %s", match.toString()));
					return stream;
				} else {
					logger.info(String.format("Failed to load %s", match.toString()));
				}
			}
		}

		logger.info("Exhausted all options. Giving up!");
		
		return null;
	}

	private String getFileNameWithExtension(String filePath) {
		int index = filePath.lastIndexOf('/');
		if(index < 0) {
			index = filePath.lastIndexOf('\\');
			if(index < 0)
				return "";
		}
		
		return filePath.substring(index + 1, filePath.length());
	}
	
	private String getFileExtension(String filePath) {
		int index = filePath.lastIndexOf('.');
		if(index < 0)
			return "";
		
		return filePath.substring(index + 1, filePath.length());
	}
	
	private List<URL> getMatches(String fileName, ClassLoader classLoader, String searchString) {
		List<URL> matches = new ArrayList<URL>();
		try {
			logger.info(String.format("ClassLoader found following matches for '%s':", searchString));
			Enumeration<URL> urls = classLoader.getResources(searchString);
			while(urls.hasMoreElements()) {
				URL next = urls.nextElement();
				String asString = next.toString();
				
				if(asString.contains( fileName )) {
					logger.info(String.format("Possible match: %s", asString));
					matches.add(next);
				} else {
					logger.info(next);
				}
			}
		} catch (IOException e) {
			logger.info(String.format("Encountered exception while trying to find file. %s.", e.getMessage()));
			e.printStackTrace();
		}
		return matches;
	}
}