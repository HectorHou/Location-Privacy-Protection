package com.geo.handler;

import java.sql.SQLException;

import com.geo.db.DBOperation;
import com.geo.localization.Localization;
import com.geo.proto.LocationReqProto.LocationReq;
import com.geo.proto.LocationRespProto.Location;
import com.geo.proto.LocationRespProto.LocationResp;
import com.geo.proto.LocationRespProto.State;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.ReadTimeoutException;

/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 创建时间：2017年3月12日 功能描述：定位Handler，根据请求计算位置并写入ctx
 */
public class LocalizationHandler extends SimpleChannelInboundHandler<LocationReq> {

	private final Localization ll;
	private final DBOperation operation;
	private String userId;
	private int i = 0;

	public LocalizationHandler(Localization localization, DBOperation operation) {
		this.ll = localization;
		this.operation = operation;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, LocationReq msg) throws Exception {
		System.out.println("TestSharable " + i++);
		this.userId = msg.getUserId();
		Location location = ll.getLocation(msg);
		ctx.writeAndFlush(buildResp(location));

		System.out.println("in localizationhandler " + location);

	}

	private LocationResp buildResp(Location location) {
		LocationResp.Builder builder = LocationResp.newBuilder();
		if (location == null)
			return builder.setState(State.NOT_FOUND).setMessage("localization failed").build();
		else
			return builder.setState(State.SUCCESS).setLocation(location).setMessage("localization succeed").build();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		ctx.executor().submit(new Runnable() {
			@Override
			public void run() {
				try {
					operation.setAndGetStateByID(userId, 0);
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
		});
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		if (cause instanceof ReadTimeoutException)
			ctx.close();
	}

}
