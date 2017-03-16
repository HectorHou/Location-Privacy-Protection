package com.geo.server;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Properties;

import com.geo.db.DBCPOperation;
import com.geo.db.DBOperation;
import com.geo.fingerprint.Fingerprint;
import com.geo.handler.LocalizationHandler;
import com.geo.handler.UserAuthHandler;
import com.geo.localization.Localization;
import com.geo.localization.LocalizationImpl;
import com.geo.proto.LocationReqProto;

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
import io.netty.handler.timeout.ReadTimeoutHandler;
/**
 * 
 * @author hm
 * @version 1.0.0
 * @since 
 * 创建时间：2017年3月12日
 * 功能描述：开启定位服务器
 */
public class LocalizationServer {
	private final Fingerprint fingerprint;
	private final DBOperation operation;
	private final int localPort;

	private final Localization localization;

	public LocalizationServer() throws NumberFormatException, FileNotFoundException {
		Properties prop = new Properties();
		try {
			prop.load(this.getClass().getResourceAsStream("/config.properties"));
		} catch (IOException e) {
			e.printStackTrace();
		}
		localPort = Integer.parseInt(prop.getProperty("localPort"));
		fingerprint = new Fingerprint(prop.getProperty("fpFile"), Integer.parseInt(prop.getProperty("minMatch")));
		localization = new LocalizationImpl(fingerprint);
		operation = new DBCPOperation();
	}

	public void bind() throws Exception {
		EventLoopGroup bossgroup = new NioEventLoopGroup();
		EventLoopGroup workgroup = new NioEventLoopGroup();
		try {
			ServerBootstrap b = new ServerBootstrap();
			b.group(bossgroup, workgroup).channel(NioServerSocketChannel.class).localAddress(localPort)
					.option(ChannelOption.SO_BACKLOG, 5000).option(ChannelOption.TCP_NODELAY, true)
					.handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ChannelInitializer<SocketChannel>() {
						@Override
						public void initChannel(SocketChannel ch) {
							ChannelPipeline pipeline = ch.pipeline();
							pipeline.addLast(new LoggingHandler(LogLevel.INFO));
							pipeline.addLast(new ProtobufVarint32FrameDecoder());
							pipeline.addLast(new ProtobufDecoder(LocationReqProto.LocationReq.getDefaultInstance()));
							pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
							pipeline.addLast(new ProtobufEncoder());
							pipeline.addLast(new ReadTimeoutHandler(10));
							pipeline.addLast(new UserAuthHandler(operation));
							pipeline.addLast(new LocalizationHandler(localization, operation));

						}
					});

			// 绑定端口，同步等待成功
			ChannelFuture f = b.bind().sync();
			// 等待服务端监听端口关闭
			f.channel().closeFuture().sync();
		} finally {
			// 优雅退出，释放线程池资源
			bossgroup.shutdownGracefully();
			workgroup.shutdownGracefully();
		}
	}

	public static void main(String[] args) throws Exception {
		new LocalizationServer().bind();
	}
}
