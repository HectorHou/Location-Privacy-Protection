package com.geo.proto;

import java.util.Map;
import java.util.Set;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：AP图操作接口
 */
public interface Graph {

	public Set<String> getNeighbors(String accessPoint);

	public Set<String> getAccessPointPointsSet();

	public Map<String, Set<String>> getGraph();
}
