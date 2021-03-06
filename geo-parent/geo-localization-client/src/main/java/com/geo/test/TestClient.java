package com.geo.test;

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
import java.util.Map.Entry;

import com.geo.client.ClientBootstrap;
import com.geo.client.ClientOperation;
import com.geo.client.LocalizationOperation;
import com.geo.proto.LocationReqProto.AccessPoints;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：测试类，连接服务器并发送请求
 */
public class TestClient {
	public static void main(String[] args) throws InterruptedException {

		ClientOperation operation = new LocalizationOperation("testtesttesttest");
		new ClientBootstrap("localhost", 12306, operation.getQueue()).start();
		AccessPoints points = getAccessPointsFromFile(
				new File("/home/hm/\u6587\u6863/LocationPrivacy/python/location_data/a.txt"));
		for (int i = 0; i < 5; i++) {
			Thread.sleep(2000);
			operation.sendReq(points);
		}

	}

	private static AccessPoints getAccessPointsFromFile(File file) {
		try {
			return getAccessPointsFromFile(file, 10);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return null;
	}

	private static AccessPoints getAccessPointsFromFile(File file, int size) throws FileNotFoundException {
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

	private static AccessPoints sortByValue(Map<String, float[]> helper, int size) {
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

	private static void putToHelper(Map<String, float[]> helper, String[] accessPoint) {
		if (!helper.containsKey(accessPoint[0])) {
			helper.put(accessPoint[0], new float[] { Float.parseFloat(accessPoint[1]), 1.0f });
		} else {
			float[] last = helper.get(accessPoint[0]);
			helper.put(accessPoint[0], new float[] { last[0] + Float.parseFloat(accessPoint[1]), last[1] + 1 });
		}

	}
}
