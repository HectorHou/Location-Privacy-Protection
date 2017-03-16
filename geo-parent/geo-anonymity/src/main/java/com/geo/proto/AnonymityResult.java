package com.geo.proto;

import java.util.Arrays;

import com.geo.proto.LocationReqProto.AccessPoints;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：匿名结构类，存储匿名结构及真实数据插入位置
 */
public class AnonymityResult {
	private final AccessPoints[] accessPointsArray;
	private final int ran;
	private String userId;

	public AnonymityResult(AccessPoints[] accessPointsArray, int ran) {
		this.accessPointsArray = accessPointsArray;
		this.ran = ran;
	}

	public AccessPoints[] getAccessPointsArray() {
		return accessPointsArray;
	}

	public int getRan() {
		return ran;
	}

	public String getUserId() {
		return userId;
	}

	public void setUserId(String userId) {
		this.userId = userId;
	}

	@Override
	public String toString() {
		return "AnonymityResult [accessPointsArray=" + Arrays.toString(accessPointsArray) + ", ran=" + ran + ", userId="
				+ userId + "]";
	}
}
