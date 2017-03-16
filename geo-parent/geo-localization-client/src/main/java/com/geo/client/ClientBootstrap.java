package com.geo.client;

import java.net.InetSocketAddress;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import com.geo.proto.LocationRespProto;
import com.geo.handler.LocationRespHandler;
import com.geo.proto.LocationReqProto.LocationReq;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
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
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：客户引导类，与服务器建立连接，并发送queue中的信息
 */
public class ClientBootstrap extends Thread {
	private final BlockingQueue<LocationReq> queue;
	private final String host;
	private final int port;

	public ClientBootstrap(String host, int port, BlockingQueue<LocationReq> queue) {
		this.host = host;
		this.port = port;
		this.queue = queue;
	}

	@Override
	public void run() {

		EventLoopGroup worker = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(worker).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host, port))
					.option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new LoggingHandler(LogLevel.INFO));
							pipeline.addLast(new ProtobufVarint32FrameDecoder());
							pipeline.addLast(new ProtobufDecoder(LocationRespProto.LocationResp.getDefaultInstance()));
							pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
							pipeline.addLast(new ProtobufEncoder());

							pipeline.addLast(new LocationRespHandler());
						}
					});
			try {
				Channel channel = bootstrap.connect().sync().channel();
				while (true) {
					LocationReq request = queue.poll(5, TimeUnit.SECONDS);

					if (request == null || !channel.isActive()) {
						break;
					} else {
						channel.writeAndFlush(request);
					}
				}

				channel.closeFuture().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} finally

		{
			worker.shutdownGracefully();
		}
	}

}
