package com.geo.client;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import com.geo.proto.LocationReqProto.AccessPoints;
import com.geo.proto.LocationReqProto.LocationReq;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：客户操作实现类，指定连接定位服务器或代理匿名服务器，并通过向队列写消息进行发送
 */
public class LocalizationOperation implements ClientOperation {
	private final String userId;
	private final BlockingQueue<LocationReq> queue;

	public LocalizationOperation(String userId) {
		this.userId = userId;
		this.queue = new LinkedBlockingQueue<>();
	}

	@Override
	public String getUserId() {
		return userId;
	}

	@Override
	public BlockingQueue<LocationReq> getQueue() {
		return queue;
	}

	@Override
	public void sendReq(AccessPoints accessPoints) {
		try {
			queue.put(buildLocationReq(accessPoints));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	private LocationReq buildLocationReq(AccessPoints accessPoints) {
		return LocationReq.newBuilder().setAccessPoints(accessPoints).setUserId(userId).build();
	}

	

}
