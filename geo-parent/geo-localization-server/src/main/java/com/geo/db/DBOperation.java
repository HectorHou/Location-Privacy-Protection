package com.geo.db;

import java.sql.SQLException;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：数据库操作接口
 */
public interface DBOperation {

	public static String QUERY_BY_ID = "select state from user where id = ? for update;";
	public static String UPDATE_BY_ID = "update user set state = ? where id = ?;";
/**
 * 
     * 
     * @param String
     * @param int
     * @return int
     * 设置id 的state属性，并返回之前属性
 */
	public int setAndGetStateByID(String id, int state) throws SQLException;
}
