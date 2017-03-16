package com.geo.handler;

import com.geo.proto.LocationRespProto.LocationResp;
import com.geo.proto.LocationRespProto.State;

import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：代理客户端handler，将位置服务器的返回的位置发给用户
 */
public class ProxyBackendHandler extends SimpleChannelInboundHandler<LocationResp> {
	private final Channel inboundChannel;
	private int ranIndex;
	private int anonymityNum;
	private int count;

	public ProxyBackendHandler(Channel inboundChannel) {
		this.inboundChannel = inboundChannel;
	}


	@Override
	protected void channelRead0(ChannelHandlerContext ctx, LocationResp msg) throws Exception {
		System.out.println("in backendhandler receive locationResp" + count + " " + msg);

		if (count == 0 && msg.getState() == State.AUTH_FAILED) {
			this.inboundChannel.writeAndFlush(msg).addListener(ChannelFutureListener.CLOSE);
		} else if (count == ranIndex && this.inboundChannel.isActive()) {
			this.inboundChannel.writeAndFlush(msg);
			this.inboundChannel.read();
		}

		count++;

		if (count == anonymityNum)
			count = 0;

	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		this.inboundChannel.close();
	}

	public int getRanIndex() {
		return ranIndex;
	}

	public void setRanIndex(int ranIndex) {
		this.ranIndex = ranIndex;
	}

	public int getAnonymityNum() {
		return anonymityNum;
	}

	public void setAnonymityNum(int anonymityNum) {
		this.anonymityNum = anonymityNum;
	}

}
