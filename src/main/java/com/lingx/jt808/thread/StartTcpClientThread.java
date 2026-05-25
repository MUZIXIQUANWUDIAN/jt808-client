package com.lingx.jt808.thread;

import com.lingx.jt808.JT808ClientContext;
import com.lingx.jt808.netty.TcpClient;

import io.netty.channel.EventLoopGroup;

public class StartTcpClientThread implements Runnable {
	private final String ip;
	private final int port;
	private final JT808ClientContext ctx;
	private final EventLoopGroup sharedGroup;

	public StartTcpClientThread(String ip, int port, JT808ClientContext ctx) {
		this(ip, port, ctx, null);
	}

	public StartTcpClientThread(String ip, int port, JT808ClientContext ctx, EventLoopGroup sharedGroup) {
		this.ip = ip;
		this.port = port;
		this.ctx = ctx;
		this.sharedGroup = sharedGroup;
	}

	@Override
	public void run() {
		TcpClient.runBlocking(ip, port, ctx, sharedGroup);
	}
}
