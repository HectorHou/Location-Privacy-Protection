package com.geo.handler;

import com.geo.proto.LocationReqProto.AccessPoints;
import com.geo.proto.LocationReqProto.LocationReq;
import com.geo.proto.LocationRespProto;
import com.geo.proto.AnonymityResult;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 创建时间：2017年3月12日 功能描述：代理handler，建立与位置服务器的连接，并将匿名结果发送
 */
public class ProxyFrontendHandler extends SimpleChannelInboundHandler<AnonymityResult> {
	private final String remoteHost;
	private final int remotePort;

	private Channel outboundChannel;

	private ProxyBackendHandler proxyBackendHandler;

	public ProxyFrontendHandler(String remoteHost, int remotePort) {
		this.remoteHost = remoteHost;
		this.remotePort = remotePort;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {

		System.out.println("in frontendhandler connectiong...");

		Channel inboundChannel = ctx.channel();

		Bootstrap b = new Bootstrap();
		b.group(inboundChannel.eventLoop()).channel(ctx.channel().getClass()).option(ChannelOption.TCP_NODELAY, true)
				.handler(new ChannelInitializer<SocketChannel>() {
					@Override
					public void initChannel(SocketChannel ch) {
						ChannelPipeline pipeline = ch.pipeline();

						pipeline.addLast(new LoggingHandler(LogLevel.INFO));
						pipeline.addLast(new ProtobufVarint32FrameDecoder());
						pipeline.addLast(new ProtobufDecoder(LocationRespProto.LocationResp.getDefaultInstance()));
						pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
						pipeline.addLast(new ProtobufEncoder());
						ch.pipeline().addLast(new ProxyBackendHandler(inboundChannel));
					}
				});

		ChannelFuture f = b.connect(remoteHost, remotePort);

		this.outboundChannel = f.channel();

		f.addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) {
				if (future.isSuccess()) {
					// connection complete start to read first data
					inboundChannel.read();
				} else {
					// Close the it if the connection attempt failed.
					inboundChannel.close();
				}
			}
		});
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, AnonymityResult msg) throws Exception {

		System.out.println(
				"in frontendhandler receive anonymity result" + msg.getRan() + " " + msg.getAccessPointsArray().length);

		this.proxyBackendHandler.setRanIndex(msg.getRan());
		this.proxyBackendHandler.setAnonymityNum(msg.getAccessPointsArray().length);

		String userId = msg.getUserId();

		for (AccessPoints accessPoints : msg.getAccessPointsArray()) {
			if (this.outboundChannel.isActive())
				this.outboundChannel.writeAndFlush(createLocationReq(accessPoints, userId));
		}

	}

	private LocationReq createLocationReq(AccessPoints accessPoints, String userId) {
		return LocationReq.newBuilder().setAccessPoints(accessPoints).setUserId(userId).build();
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		if (outboundChannel != null) {
			outboundChannel.close();
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}

}
