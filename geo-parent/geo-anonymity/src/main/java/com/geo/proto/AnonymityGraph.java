package com.geo.proto;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：AP图实现类，存储图结构，提供访问方法
 */
public class AnonymityGraph implements Graph {
	private final Map<String, Set<String>> graph;
	private final int edge;
	private final int vertex;

	public AnonymityGraph(String filePath) throws FileNotFoundException {
		this(new File(filePath));
	}

	public AnonymityGraph(File file) throws FileNotFoundException {
		this.graph = createAnonymityGraph(file);
		this.vertex = this.graph.size();
		this.edge = countEdge();
	}

	private Map<String, Set<String>> createAnonymityGraph(File file) throws FileNotFoundException {
		if (!file.isFile()) {
			throw new FileNotFoundException("这不是一个文件");
		}
		InputStream is = new FileInputStream(file);
		InputStreamReader isr = new InputStreamReader(is);
		BufferedReader br = new BufferedReader(isr);
		Map<String, Set<String>> graph = new HashMap<>();
		String line = null;
		try {
			while ((line = br.readLine()) != null) {
				String[] points = line.split(" ");
				Set<String> set = new HashSet<>();
				for (int i = 1; i < points.length; i++)
					set.add(points[i]);
				graph.put(points[0], set);
			}
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)
					br.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return graph;
	}

	private int countEdge() {
		int count = 0;
		for (Set<String> set : this.graph.values()) {
			count += set.size();
		}
		return count / 2;
	}

	@Override
	public Set<String> getNeighbors(String accessPoint) {
		return graph.get(accessPoint);
	}

	@Override
	public Set<String> getAccessPointPointsSet() {
		return graph.keySet();
	}

	@Override
	public Map<String, Set<String>> getGraph() {
		return graph;
	}

	public int getEdge() {
		return edge;
	}

	public int getVertex() {
		return vertex;
	}

	@Override
	public String toString() {
		return "AnonymityGraph [graph=" + graph + ", edge=" + edge + ", vertex=" + vertex + "]";
	}
}
