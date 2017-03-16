package com.geo.handler;

import java.util.concurrent.Callable;

import com.geo.proto.LocationReqProto.AccessPoints;
import com.geo.proto.LocationReqProto.LocationReq;
import com.geo.anonymity.Anonymity;
import com.geo.proto.AnonymityResult;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：匿名Handler，负责根据请求的APmap创建匿名
 */
public class AnonymityHandler extends SimpleChannelInboundHandler<LocationReq> {
	private final Anonymity anonymity;
	private AnonymityResult lastAnonymityResult;

	public AnonymityHandler(Anonymity anonymity) {
		this.anonymity = anonymity;
		this.lastAnonymityResult = null;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, LocationReq msg) throws Exception {

		System.out.println("in anonymityhandler");

		ctx.executor().submit(new Callable<AnonymityResult>() {

			@Override
			public AnonymityResult call() throws Exception {
				AccessPoints accessPoints = msg.getAccessPoints();
				if (lastAnonymityResult == null)
					return anonymity.anonymityResult(accessPoints);
				else
					return anonymity.anonymityResult(accessPoints, lastAnonymityResult);
			}

		}).addListener(new GenericFutureListener<Future<AnonymityResult>>() {

			@Override
			public void operationComplete(Future<AnonymityResult> future) throws Exception {
				if (future.isSuccess()) {
					AnonymityResult result = future.get();
					result.setUserId(msg.getUserId());
					lastAnonymityResult = result;
					ctx.fireChannelRead(result);

				} else
					ctx.close();

			}

		});

	}

}
