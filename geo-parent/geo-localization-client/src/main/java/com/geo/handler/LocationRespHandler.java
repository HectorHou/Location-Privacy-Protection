package com.geo.handler;

import java.util.logging.Level;
import java.util.logging.Logger;

import com.geo.proto.LocationRespProto.LocationResp;
import com.geo.proto.LocationRespProto.State;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：接收服务器信息，判断Response类型，并记录日志
 */
public class LocationRespHandler extends SimpleChannelInboundHandler<LocationResp> {
	private static final Logger logger = Logger.getLogger(LocationRespHandler.class.getName());

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, LocationResp msg) throws Exception {
		if (msg.getState() != State.SUCCESS)
			logger.log(Level.INFO, msg.getMessage());
		else
			logger.log(Level.INFO, msg.getLocation().toString());
	}

}
