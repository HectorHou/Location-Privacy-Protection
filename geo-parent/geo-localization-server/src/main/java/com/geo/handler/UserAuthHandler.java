package com.geo.handler;

import java.sql.SQLException;
import java.util.concurrent.Callable;

import com.geo.db.DBOperation;
import com.geo.proto.LocationReqProto.LocationReq;
import com.geo.proto.LocationRespProto.LocationResp;
import com.geo.proto.LocationRespProto.State;

import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 创建时间：2017年3月12日 功能描述：认证Handler，检测是否存在当前用户，当前用户的状态是否已经登陆
 */
@Sharable
public class UserAuthHandler extends SimpleChannelInboundHandler<LocationReq> {
	private final DBOperation operation;

	public UserAuthHandler(DBOperation operation) {
		this.operation = operation;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, LocationReq msg) throws Exception {

		System.out.println("in authhandler ");

		String userId = msg.getUserId();

		ctx.executor().submit(new Callable<Integer>() {

			@Override
			public Integer call() throws SQLException {
				return operation.setAndGetStateByID(userId, 1);
			}
		}).addListener(new GenericFutureListener<Future<Integer>>() {

			@Override
			public void operationComplete(Future<Integer> future) throws Exception {
				if (future.isSuccess()) {
					int oldState = future.get();
					if (oldState != 0)
						ctx.writeAndFlush(buildBadResp(userId, oldState)).addListener(ChannelFutureListener.CLOSE);
					else {
						ctx.fireChannelRead(msg);
						ctx.pipeline().remove(UserAuthHandler.class);
					}
				} else
					ctx.close();

			}

		});
	}

	private Object buildBadResp(String userId, int oldState) {
		if (oldState == -1)
			return LocationResp.newBuilder().setUserId(userId).setState(State.AUTH_FAILED).setMessage("auth failed")
					.build();
		else if (oldState == 1)
			return LocationResp.newBuilder().setUserId(userId).setState(State.AUTH_FAILED).setMessage("already login")
					.build();
		else
			return null;
	}

}
