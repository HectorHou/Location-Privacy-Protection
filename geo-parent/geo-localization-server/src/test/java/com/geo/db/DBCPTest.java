package com.geo.db;

import junit.framework.TestCase;

import java.sql.SQLException;

import org.junit.Test;

public class DBCPTest extends TestCase {
	@Test
	public void testOperation() throws SQLException {
		DBOperation operation = new DBCPOperation();
		assertEquals(operation.setAndGetStateByID("testtesttesttest", 1), 0);
	}

	@Test
	public void testOperation1() throws SQLException {
		DBOperation operation = new DBCPOperation();
		assertEquals(operation.setAndGetStateByID("testtesttesttest", 0), 1);
	}

	@Test
	public void testOperation2() throws SQLException {
		DBOperation operation = new DBCPOperation();
		assertEquals(operation.setAndGetStateByID("testtesttest", 0), -1);
	}
}
