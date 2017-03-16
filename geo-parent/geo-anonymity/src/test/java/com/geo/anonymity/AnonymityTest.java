package com.geo.anonymity;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.junit.Test;

import com.geo.proto.LocationReqProto.AccessPoints;
import com.geo.proto.AnonymityGraph;
import com.geo.proto.AnonymityGreedyList;
import com.geo.proto.AnonymityResult;
import com.geo.proto.Graph;
import com.geo.proto.GreedyList;

import junit.framework.TestCase;

public class AnonymityTest extends TestCase {
	@Test
	public void testAnonymity() throws Exception {
		Properties prop = new Properties();

		try {
			prop.load(this.getClass().getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Graph graph = new AnonymityGraph(prop.getProperty("graphPath"));
		GreedyList list = new AnonymityGreedyList(graph);
		Anonymity am = AnonymityImpl.newBuilder().setGraph(graph).setGreedyList(list).build();
		AccessPoints points = getAccessPointsFromFile(new File(prop.getProperty("apFile")));
		AnonymityResult result = am.anonymityResult(points);

		assertNotNull(result);

		assertEquals(result.getAccessPointsArray().length, 3);

		System.out.println(result);
	}

	private AccessPoints getAccessPointsFromFile(File file) {
		try {
			return getAccessPointsFromFile(file, 10);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private AccessPoints getAccessPointsFromFile(File file, int size) throws FileNotFoundException {
		if (!file.exists()) {
			throw new FileNotFoundException("没有找到输入文件");
		}
		// Input part
		BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file)));

		Map<String, float[]> helper = new HashMap<>();

		String line;

		try {
			while ((line = br.readLine()) != null) {
				if (!line.isEmpty()) {
					String[] accessPoint = line.split(" ");
					putToHelper(helper, accessPoint);
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return sortByValue(helper, size);
	}

	private AccessPoints sortByValue(Map<String, float[]> helper, int size) {
		List<Map.Entry<String, float[]>> list = new LinkedList<>(helper.entrySet());
		Collections.sort(list, new Comparator<Map.Entry<String, float[]>>() {

			@Override
			public int compare(Entry<String, float[]> o1, Entry<String, float[]> o2) {
				float[] f1 = o1.getValue();
				float[] f2 = o2.getValue();
				return (f1[0] / f1[1] < f2[0] / f2[1]) ? 1 : -1;
			}

		});
		Map<String, Float> result = new LinkedHashMap<>();
		int i = 0;
		for (Map.Entry<String, float[]> entry : list) {
			result.put(entry.getKey(), entry.getValue()[0] / entry.getValue()[1]);
			if (++i == size)
				break;
		}
		return AccessPoints.newBuilder().putAllPoints(result).build();
	}

	private void putToHelper(Map<String, float[]> helper, String[] accessPoint) {
		if (!helper.containsKey(accessPoint[0])) {
			helper.put(accessPoint[0], new float[] { Float.parseFloat(accessPoint[1]), 1.0f });
		} else {
			float[] last = helper.get(accessPoint[0]);
			helper.put(accessPoint[0], new float[] { last[0] + Float.parseFloat(accessPoint[1]), last[1] + 1 });
		}

	}
}
