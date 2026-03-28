package com.lingx.jt808.thread;

import com.lingx.jt808.JT808ClientContext;
import com.lingx.jt808.netty.TcpClient;

public class StartTcpClientThread implements Runnable {
	private final String ip;
	private final int port;
	private final JT808ClientContext ctx;

	public StartTcpClientThread(String ip, int port, JT808ClientContext ctx) {
		this.ip = ip;
		this.port = port;
		this.ctx = ctx;
	}

	@Override
	public void run() {
		TcpClient.runBlocking(ip, port, ctx);
	}
}
