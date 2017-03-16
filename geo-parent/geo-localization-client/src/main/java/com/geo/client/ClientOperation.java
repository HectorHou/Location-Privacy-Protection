package com.geo.client;

import java.util.concurrent.BlockingQueue;

import com.geo.proto.LocationReqProto.AccessPoints;
import com.geo.proto.LocationReqProto.LocationReq;

/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：客户操作接口
 */
public interface ClientOperation {
	public void sendReq(AccessPoints accessPoints);

	public BlockingQueue<LocationReq> getQueue();

	public String getUserId();
}
