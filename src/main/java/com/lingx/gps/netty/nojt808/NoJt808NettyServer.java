package com.lingx.gps.netty.nojt808;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class NoJt808NettyServer implements Runnable{
	public NoJt808NettyServer() {
	}
	public NoJt808NettyServer(int port) {
		this.port=port;
	}
	private int port=8888;
	private Channel channel;
	public void run() {
		// 配置服务器参数
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
		    ServerBootstrap b = new ServerBootstrap();
		    b.group(bossGroup, workerGroup)
		     .channel(NioServerSocketChannel.class)
		     .childHandler(new ChannelInitializer<SocketChannel>() {
		         @Override
		         public void initChannel(SocketChannel ch) throws Exception {
		        	 ch.pipeline().addLast(new ByteArrayEncoder());
		        	 ch.pipeline().addLast(new ByteArrayDecoder());
		             ch.pipeline().addLast(new NoJt808ChannelHandler());
		         }
		     })
		     .option(ChannelOption.SO_BACKLOG, 128)
		     .childOption(ChannelOption.SO_KEEPALIVE, true);
		 
		    // 绑定端口，开始接收连接
		    ChannelFuture f = b.bind(port).sync();
		    this.channel= f.channel();
		    // 等待服务器关闭的命令
		    f.channel().closeFuture().sync();
		}catch(Exception e) {
			e.printStackTrace();
		} finally {
		    // 释放资源
		    bossGroup.shutdownGracefully();
		    workerGroup.shutdownGracefully();
		    this.channel=null;
		}
	}
	public Channel getChannel() {
		return channel;
	}
	public void setPort(int port) {
		this.port = port;
	}
}
