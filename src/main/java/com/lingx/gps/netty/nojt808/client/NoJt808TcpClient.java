package com.lingx.gps.netty.nojt808.client;

import java.util.function.BooleanSupplier;

import com.lingx.jtools.ui.TcpClientTabBridge;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

public class NoJt808TcpClient implements Runnable {

	private final String ip;
	private final int port;
	private final TcpClientTabBridge ui;
	private final BooleanSupplier hexDisplay;
	private volatile Channel channel;

	public NoJt808TcpClient(String ip, int port, TcpClientTabBridge ui, BooleanSupplier hexDisplay) {
		this.ip = ip;
		this.port = port;
		this.ui = ui;
		this.hexDisplay = hexDisplay;
	}

	public Channel getChannel() {
		return channel;
	}

	void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void runBlocking() {
		NioEventLoopGroup workerGroup = new NioEventLoopGroup();
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workerGroup);
			bootstrap.channel(NioSocketChannel.class);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) {
					ch.pipeline().addLast("decoder", new ByteArrayDecoder());
					ch.pipeline().addLast("encoder", new ByteArrayEncoder());
					ch.pipeline().addLast(new NoJt808TcpClientHandler(NoJt808TcpClient.this, ui, hexDisplay));
				}
			});

			ChannelFuture future = bootstrap.connect(ip, port).sync();
			channel = future.channel();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			ui.notifyDisconnected();
			ui.appendArrowLog("服务器连接失败" + ip + ":" + port);
		} finally {
			workerGroup.shutdownGracefully();
		}
	}

	@Override
	public void run() {
		runBlocking();
	}
}
