package com.geo.server;

import java.io.IOException;
import java.util.Properties;

import com.geo.handler.AnonymityHandler;
import com.geo.handler.ProxyFrontendHandler;
import com.geo.proto.LocationReqProto;
import com.geo.anonymity.Anonymity;
import com.geo.anonymity.AnonymityImpl;
import com.geo.proto.AnonymityGraph;
import com.geo.proto.AnonymityGreedyList;
import com.geo.proto.Graph;
import com.geo.proto.GreedyList;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
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
 * 功能描述：开启匿名服务器
 */
public class AnonymityServer {
	// 服务器端口与被代理服务器socket
	private final int localPort;
	private final String remoteHost;
	private final int remotePort;

	private final Anonymity anonymity;

	public AnonymityServer() throws Exception {
		Properties prop = new Properties();
		try {
			prop.load(this.getClass().getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		this.localPort = Integer.parseInt(prop.getProperty("localPort"));
		this.remoteHost = prop.getProperty("remoteHost");
		this.remotePort = Integer.parseInt(prop.getProperty("remotePort"));

		Graph graph = new AnonymityGraph(prop.getProperty("graphPath"));
		GreedyList greedyList = new AnonymityGreedyList(graph);
		this.anonymity = AnonymityImpl.newBuilder().setGraph(graph).setGreedyList(greedyList).build();
	}

	public void bind() throws Exception {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workGroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossGroup, workGroup).channel(NioServerSocketChannel.class).localAddress(localPort)
					.option(ChannelOption.SO_BACKLOG, 5000).option(ChannelOption.TCP_NODELAY, true)
					.handler(new LoggingHandler(LogLevel.INFO)).childOption(ChannelOption.AUTO_READ, false)
					.childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ChannelPipeline pipeline = ch.pipeline();

							pipeline.addLast(new LoggingHandler(LogLevel.INFO));
							pipeline.addLast(new ProtobufVarint32FrameDecoder());
							pipeline.addLast(new ProtobufDecoder(LocationReqProto.LocationReq.getDefaultInstance()));
							pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
							pipeline.addLast(new ProtobufEncoder());

							pipeline.addLast(new AnonymityHandler(anonymity));
							pipeline.addLast(new ProxyFrontendHandler(remoteHost, remotePort));
						}
					});

			ChannelFuture f = b.bind().sync();
			f.channel().closeFuture().sync();
		} finally {
			// 优雅退出，释放线程池资源
			bossGroup.shutdownGracefully();
			workGroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new AnonymityServer().bind();
	}
}
