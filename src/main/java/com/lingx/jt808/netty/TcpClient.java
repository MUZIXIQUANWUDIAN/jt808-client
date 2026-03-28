package com.lingx.jt808.netty;

import com.lingx.jt808.JT808ClientContext;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public final class TcpClient {

	private TcpClient() {
	}

	public static void runBlocking(String ip, int port, JT808ClientContext ctx) {
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workerGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) {
					ch.pipeline().addLast("decoder", new ProtocolDecoder());
					ch.pipeline().addLast("encoder", new ByteArrayEncoder());
					ch.pipeline().addLast(new TcpClientHandler(ctx));
				}
			});

			ChannelFuture future = bootstrap.connect(ip, port).sync();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			ctx.getUi().notifyDisconnected();
			ctx.callAardio("服务器连接失败" + ip + ":" + port);
		} finally {
			workerGroup.shutdownGracefully();
		}
	}
}
