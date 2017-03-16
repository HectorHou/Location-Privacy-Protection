package com.geo.anonymity;

import com.geo.proto.AnonymityResult;
import com.geo.proto.LocationReqProto.AccessPoints;

/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 创建时间：2017年3月12日 功能描述：匿名操作接口
 */
public interface Anonymity {
	public static int ANONYMITY_NUM = 3;
	public static float MIN_PROBABILITY = 0.85f;

	public AnonymityResult anonymityResult(AccessPoints accessPoints);

	public AnonymityResult anonymityResult(AccessPoints accessPoints, AnonymityResult lastResult);
}
