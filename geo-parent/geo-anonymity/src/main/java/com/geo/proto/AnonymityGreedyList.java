package com.geo.proto;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：AP贪心表实现类，存储表结构，提供相应的访问方法
 */
public class AnonymityGreedyList implements GreedyList {

	private final Map<String, GreedyInfo> greedyList;

	public AnonymityGreedyList(Graph graph) {
		this.greedyList = setGreedyList(graph);
	}

	private Map<String, GreedyInfo> setGreedyList(Graph graph) {
		Map<String, GreedyInfo> greedyList = new HashMap<>();
		for (Entry<String, Set<String>> entry : graph.getGraph().entrySet()) {
			int countNeighbor = 0;
			int countEdgeOfNeighbor = 0;
			for (String mac : entry.getValue()) {
				countNeighbor += 1;
				Set<String> helper = new HashSet<>(graph.getGraph().get(mac));
				helper.retainAll(entry.getValue());
				countEdgeOfNeighbor += helper.size();
			}
			greedyList.put(entry.getKey(), new GreedyInfo(countNeighbor, countEdgeOfNeighbor / 2));
		}
		return greedyList;
	}

	@Override
	public GreedyInfo getInfo(String accessPoint) {
		return greedyList.get(accessPoint);
	}

	@Override
	public String toString() {
		return "AnonymityGreedyList [greedyList=" + greedyList + "]";
	}

	public Map<String, GreedyInfo> getGreedyList() {
		return greedyList;
	}

}
