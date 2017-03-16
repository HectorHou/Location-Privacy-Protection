package com.geo.proto;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import org.junit.Test;

import junit.framework.TestCase;

public class ProtoTest extends TestCase {

	@Test
	public void testGraph() throws FileNotFoundException {
		Properties prop = new Properties();

		try {
			prop.load(this.getClass().getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		AnonymityGraph graph = new AnonymityGraph(prop.getProperty("graphPath"));
		AnonymityGreedyList list = new AnonymityGreedyList(graph);

		assertEquals(graph.getVertex(), 54);
		assertEquals(graph.getEdge(), 570);

		assertEquals(list.getGreedyList().size(), 54);
	}
}
