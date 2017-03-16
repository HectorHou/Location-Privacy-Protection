package com.geo.localization;

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
import java.util.Set;

import com.geo.fingerprint.Fingerprint;
import com.geo.proto.LocationReqProto.AccessPoints;
import com.geo.proto.LocationReqProto.LocationReq;
import com.geo.proto.LocationRespProto.Location;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：定位实现类
 */
public class LocalizationImpl implements Localization {
	private final Fingerprint fingerprint;

	public LocalizationImpl(Fingerprint fingerprint) {
		this.fingerprint = fingerprint;
	}
/**
 * 
     * 
     * @param Object
     * @return AccessPoints
     * 
     * 从请求中对象中提取出AccessPoints
 */
	public AccessPoints convertReq(Object o) {
		if (o instanceof AccessPoints)
			return (AccessPoints) o;
		else if (o instanceof LocationReq)
			return ((LocationReq) o).getAccessPoints();
		else if (o instanceof File)
			return getAccessPointsFromFile((File) o);
		else
			throw new java.lang.UnsupportedOperationException("unsupported type");
	}

	/**
	 * 
	     * 
	     * @param File
	     * @return AccessPoints
	     * 
	     * 从文件中获取AccessPoints
	 */
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

	@Override
	public Location getLocation(Object request) {
		AccessPoints accessPoints = convertReq(request);
		if (accessPoints == null)
			return null;

		return getLocation(fingerprint.getFingerprint(), fingerprint.getMinMatch(), accessPoints.getPointsMap());
	}

	private Location getLocation(Map<Location, HashMap<String, Float>> fingerprint, int minMatch,
			Map<String, Float> pointsMap) {

		Location[] aidLocations = getAidLocations();
		double[] aidDistances = getDistances();

		// 当前最大距离索引
		int indexOfMaxValue = 0;

		Set<String> pointsMapKeySet = pointsMap.keySet();

		for (Entry<Location, HashMap<String, Float>> fingerprintEntry : fingerprint.entrySet()) {

			int countOfMatch = 0;
			double sumOfQuadratic = 0;

			Map<String, Float> entryMap = fingerprintEntry.getValue();

			for (String mac : pointsMapKeySet) {
				if (entryMap.containsKey(mac)) {
					sumOfQuadratic += Math.pow((pointsMap.get(mac) - entryMap.get(mac)), 2);
					countOfMatch += 1;
				}
			}

			// 在匹配数大于最小匹配数并且距离小于当前数组中最大距离时，进行替换
			if ((countOfMatch != 0) && (countOfMatch >= minMatch)
					&& (aidDistances[indexOfMaxValue] > sumOfQuadratic / countOfMatch)) {

				aidDistances[indexOfMaxValue] = sumOfQuadratic / countOfMatch;
				aidLocations[indexOfMaxValue] = fingerprintEntry.getKey();

				// 找到新的最大距离索引
				indexOfMaxValue = getIndexOfMaxValue(aidDistances);
			}

		}

		return getlocation(aidLocations);
	}

	private Location getlocation(Location[] aidLocations) {
		double sumOflongitude = 0.0;
		double sumOflatitude = 0.0;
		int count = 0;
		for (int i = 0; i < PRECISION; i++) {
			if (aidLocations[i] == null)
				continue;
			sumOflongitude += aidLocations[i].getLongitude();
			sumOflatitude += aidLocations[i].getLatitude();
			count++;
		}

		if (count == 0)
			return null;

		return Location.newBuilder().setLongitude(sumOflongitude / count).setLatitude(sumOflatitude / count).build();
	}

	private int getIndexOfMaxValue(double[] aidDistances) {
		int indexOfMaxValue = 0;
		double maxValue = aidDistances[0];

		for (int i = 1; i < PRECISION; i++) {
			if (aidDistances[i] > maxValue) {
				maxValue = aidDistances[i];
				indexOfMaxValue = i;
			}
		}

		return indexOfMaxValue;
	}

	private double[] getDistances() {
		double[] aidDistances = new double[PRECISION];
		for (int i = 0; i < PRECISION; i++)
			aidDistances[i] = Double.MAX_VALUE;
		return aidDistances;
	}

	private Location[] getAidLocations() {
		Location[] aidLocations = new Location[PRECISION];
		for (int i = 0; i < PRECISION; i++)
			aidLocations[i] = Location.newBuilder().build();
		return aidLocations;
	}

}
