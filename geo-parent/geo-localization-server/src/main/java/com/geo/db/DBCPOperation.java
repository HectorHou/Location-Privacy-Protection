package com.geo.db;

import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.dbcp.BasicDataSourceFactory;

/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 创建时间：2017年3月12日 功能描述：数据库操作实现类
 */
public class DBCPOperation implements DBOperation {
	private Properties properties = new Properties();
	private DataSource dataSource;
	{
		try {
			properties.load(DBCPOperation.class.getResourceAsStream("/dbcp.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			dataSource = BasicDataSourceFactory.createDataSource(properties);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private Connection getConnection() {
		Connection conn = null;
		try {
			conn = dataSource.getConnection();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return conn;
	}

	private ResultSet getStateByID(Connection conn, String sql, String id) throws SQLException {
		ResultSet resultSet = null;
		PreparedStatement stat = null;

		stat = conn.prepareStatement(sql);
		stat.setString(1, id);
		resultSet = stat.executeQuery();

		return resultSet;

	}

	private int setStateByID(Connection conn, String sql, String id, int state) throws SQLException {
		PreparedStatement stat = null;
		int num = 0;
		stat = conn.prepareStatement(sql);
		stat.setInt(1, state);
		stat.setString(2, id);
		num = stat.executeUpdate();
		return num;
	}

	@Override
	public int setAndGetStateByID(String id, int state) throws SQLException {

		Connection conn = getConnection();
		conn.setAutoCommit(false);
		int currState = -1;
		try {
			ResultSet resultSet = getStateByID(conn, QUERY_BY_ID, id);
			if (!resultSet.next())
				throw new SQLException();

			currState = resultSet.getInt(1);
			if (currState == state)
				throw new SQLException();

			setStateByID(conn, UPDATE_BY_ID, id, state);

			conn.commit();
		} catch (SQLException e) {
			try {
				conn.rollback();
			} catch (SQLException ex) {
				ex.printStackTrace();
			}
		} finally {
			conn.close();
		}
		return currState;

	}

}
