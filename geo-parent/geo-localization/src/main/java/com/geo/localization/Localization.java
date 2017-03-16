package com.geo.localization;

import com.geo.proto.LocationRespProto.Location;

/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 创建时间：2017年3月12日 功能描述：定位接口
 */
public interface Localization {
	public static int PRECISION = 3;

	/**
	 * 
	 * 
	 * @param Object
	 * @return Location
	 * 根据请求对象，返回位置信息
	 */
	public Location getLocation(Object request);

}
