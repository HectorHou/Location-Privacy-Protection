package com.geo.anonymity;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import com.geo.proto.LocationReqProto.AccessPoints;
import com.geo.proto.AnonymityResult;
import com.geo.proto.Graph;
import com.geo.proto.GreedyList;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：匿名操作实现类，包括位置匿名和轨迹匿名
 */
public class AnonymityImpl implements Anonymity {
	private final Graph graph;
	private final GreedyList greedyList;
	private int num;
	private float minProbability;
	private final Random random;

	private AnonymityImpl(Graph graph, GreedyList greedyList, int num, float minProbability) {
		this.graph = graph;
		this.greedyList = greedyList;
		this.random = new Random();
		this.num = num;
		this.minProbability = minProbability;
	}

	public static class Builder {
		private Graph graph;
		private GreedyList greedyList;
		private int num;
		private float minProbability;

		public Builder setGraph(Graph graph) {
			this.graph = graph;
			return this;
		}

		public Builder setGreedyList(GreedyList greedyList) {
			this.greedyList = greedyList;
			return this;
		}

		public Builder setNum(int num) {
			this.num = num;
			return this;
		}

		public Builder setMinProbability(float minProbability) {
			this.minProbability = minProbability;
			return this;
		}

		public Anonymity build() throws Exception {
			if (graph == null || greedyList == null)
				throw new Exception("graph and greedyList must be set!");
			num = (num == 0) ? ANONYMITY_NUM : num;
			minProbability = (minProbability == 0) ? MIN_PROBABILITY : minProbability;
			return new AnonymityImpl(graph, greedyList, num, minProbability);
		}

	}

	public static Builder newBuilder() {
		return new Builder();
	}

	@Override
	public AnonymityResult anonymityResult(AccessPoints accessPoints) {
		AccessPoints[] accessPointsArray = new AccessPoints[num];
		Set<String> apMapKeySet = accessPoints.getPointsMap().keySet();
		int setLength = apMapKeySet.size();
		int count = 0;
		while (count < num - 1) {
			// 随机选取一mac地址
			String randomMac = getRandomPoint(random, graph.getAccessPointPointsSet());

			// randomMac在采集中时
			if (apMapKeySet.contains(randomMac))
				continue;

			// randomMac邻居数小于采集中的长度时
			if (greedyList.getInfo(randomMac).getNeighbors() < (setLength - 1))
				continue;

			// randomMac的邻接系数小于阈值时
			if (greedyList.getInfo(randomMac).getProbability() < this.minProbability)
				continue;

			Map<String, Float> localizationData = getlocalizationData(randomMac, setLength);
			accessPointsArray[count++] = AccessPoints.newBuilder().putAllPoints(localizationData).build();
		}
		// 将真实AP随机插入
		int randomIndex = insertRealAPMap(num, accessPointsArray, accessPoints);

		return new AnonymityResult(accessPointsArray, randomIndex);
	}

	@Override
	public AnonymityResult anonymityResult(AccessPoints accessPoints, AnonymityResult lastResult) {
		AccessPoints[] lastAccessPointsArray = lastResult.getAccessPointsArray();
		int lastRealIndex = lastResult.getRan();
		int num = lastAccessPointsArray.length;

		AccessPoints[] accessPointsArray = new AccessPoints[num];
		Set<String> apMapKeySet = accessPoints.getPointsMap().keySet();
		int setLength = apMapKeySet.size();
		int count = 0;

		while (count < num) {
			if (count == lastRealIndex) {
				accessPointsArray[count++] = accessPoints;
				continue;
			}

			String randomMac = getRandomPoint(random, lastAccessPointsArray[count].getPointsMap().keySet());

			// randomMac在采集中时
			if (apMapKeySet.contains(randomMac))
				continue;

			// randomMac邻居数小于采集中的长度时
			if (greedyList.getInfo(randomMac).getNeighbors() < (setLength - 1))
				continue;

			// randomMac的邻接系数小于阈值时
			if (greedyList.getInfo(randomMac).getProbability() < this.minProbability)
				continue;

			Map<String, Float> localizationData = getlocalizationData(randomMac, setLength);
			accessPointsArray[count++] = AccessPoints.newBuilder().putAllPoints(localizationData).build();

		}
		return new AnonymityResult(accessPointsArray, lastRealIndex);
	}

	private Map<String, Float> getlocalizationData(String randomMac, int setLength) {
		Map<String, Float> localizationData = new HashMap<>();

		localizationData.put(randomMac, random.nextInt(200) + 600.0F);
		Set<String> neighborSet = new HashSet<>(graph.getNeighbors(randomMac));
		for (int i = 0; i < setLength - 1; i++) {
			String randomMac0 = getRandomPoint(random, neighborSet);
			localizationData.put(randomMac0, random.nextInt(200) + 600.0F);
			neighborSet.remove(randomMac0);
		}
		return localizationData;
	}

	private int insertRealAPMap(int num, AccessPoints[] accessPointsArray, AccessPoints accessPoints) {
		int randomIndex = random.nextInt(num);
		if (randomIndex != num - 1)
			accessPointsArray[num - 1] = accessPointsArray[randomIndex];

		accessPointsArray[randomIndex] = accessPoints;
		return randomIndex;
	}

	private String getRandomPoint(Random random, Set<String> set) {
		int index = random.nextInt(set.size());
		Iterator<String> iter = set.iterator();
		for (int i = 0; i < index; i++) {
			iter.next();
		}
		return iter.next();
	}

	public int getNum() {
		return num;
	}

	public void setNum(int num) {
		this.num = num;
	}

	public float getMinProbability() {
		return minProbability;
	}

	public void setMinProbability(float minProbability) {
		this.minProbability = minProbability;
	}

	public Graph getGraph() {
		return graph;
	}

	public GreedyList getGreedyList() {
		return greedyList;
	}

	public Random getRandom() {
		return random;
	}

}
