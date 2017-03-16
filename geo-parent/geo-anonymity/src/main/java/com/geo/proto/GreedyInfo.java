package com.geo.proto;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：AP贪心表表项对象，存储表项记录，包括邻居数，邻居的边数，聚合系数
 */
public class GreedyInfo {
	private final int neighbors;
	private final int edgesOfNeighbors;
	private final float probability;

	public GreedyInfo(int neighbors, int edgesOfNeighbors) {
		this.neighbors = neighbors;
		this.edgesOfNeighbors = edgesOfNeighbors;
		this.probability = computeProbability();
	}

	private float computeProbability() {
		return (this.neighbors > 1) ? 2.0F * this.edgesOfNeighbors / (this.neighbors * (this.neighbors - 1)) : 0;
	}

	public int getNeighbors() {
		return neighbors;
	}

	public int getEdgesOfNeighbors() {
		return edgesOfNeighbors;
	}

	public float getProbability() {
		return probability;
	}

	@Override
	public String toString() {
		return "GreedyInfo [neighbors=" + neighbors + ", edgesOfNeighbors=" + edgesOfNeighbors + ", probability="
				+ probability + "]";
	}

}
