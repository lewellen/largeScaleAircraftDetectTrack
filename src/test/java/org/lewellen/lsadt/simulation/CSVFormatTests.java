package org.lewellen.lsadt.simulation;

import java.util.ArrayList;
import java.util.List;

import junit.framework.TestCase;

public class CSVFormatTests extends TestCase {
	public void testSplit() {
		CSVFormat format = new CSVFormat();
		format.UsesQuotes = true;

		List<String> expected = new ArrayList<String>();
		expected.add("the");
		expected.add("quick brown");
		expected.add("fox");

		List<String> quoted = new ArrayList<String>();
		for(String e : expected)
			quoted.add(String.format("\"%s\"", e));
		
		List<String> output = format.split(String.join(",", quoted));
		for(int i = 0; i < expected.size(); i++)
			assertEquals(expected.get(i), output.get(i));
	}

	public void testSplitIgnoreQuotes() {
		CSVFormat format = new CSVFormat();
		format.UsesQuotes = false;

		List<String> expected = new ArrayList<String>();
		expected.add("the");
		expected.add("quick brown");
		expected.add("fox");
	
		List<String> output = format.split(String.join(",", expected));
		for(int i = 0; i < expected.size(); i++)
			assertEquals(expected.get(i), output.get(i));
	}
}
