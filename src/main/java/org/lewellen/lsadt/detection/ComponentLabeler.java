package org.lewellen.lsadt.detection;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.lewellen.lsadt.Tuple;

public class ComponentLabeler {
	public List<List<Tuple<Integer, Integer>>> getComponents(ComplexMatrix A, List<Tuple<Integer, Integer>> survivingPixels) {
		int c = -1;
//		int[][] labels = new int[A.getRows()][A.getColumns()];
//		for (int i = 0; i < A.getRows(); i++)
//			for (int j = 0; j < A.getColumns(); j++)
//				labels[i][j] = -1;

		HashMap<Integer, Integer> labels = new HashMap<Integer, Integer>();
		for (Tuple<Integer, Integer> x : survivingPixels)
			labels.put(hash(A, x), -1);

		List<List<Tuple<Integer, Integer>>> p = new ArrayList<List<Tuple<Integer, Integer>>>();

		for(Tuple<Integer, Integer> pixel : survivingPixels) {
			int px = pixel.Item1;
			int py = pixel.Item2;

			if (A.get(px, py).Real <= 0)
				continue;

			int window = 2;
			int cNeighbor = -1;
			for (int u = -window; u <= window && cNeighbor < 0; u++) {
				if (u + px < 0 || u + px >= A.getColumns())
					continue;

				for (int v = -window; v <= window && cNeighbor < 0; v++) {
					if (v + py < 0 || v + py >= A.getRows())
						continue;

					if (u == 0 && v == 0)
						continue;

					Tuple<Integer, Integer> y = new Tuple<Integer, Integer>(u + px, v + py);
					int hashY = hash(A, y);
					
					if (A.get(u + px, v + py).Real > 0 && labels.containsKey(hashY) && labels.get(hashY) >= 0) {
						cNeighbor = labels.get(hashY);
					}
				}
			}

			if (cNeighbor < 0) {
				cNeighbor = ++c;
				p.add(new ArrayList<Tuple<Integer, Integer>>());
			}

			labels.put(hash(A, pixel), cNeighbor);
			p.get(cNeighbor).add(new Tuple<Integer, Integer>(px, py));
		}

		return p;
	}

	private Integer hash(ComplexMatrix A, Tuple<Integer, Integer> pt) {
		return pt.Item2 * A.getRows() + pt.Item1;
	}
}
