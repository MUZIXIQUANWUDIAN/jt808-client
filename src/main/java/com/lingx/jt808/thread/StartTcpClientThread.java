package com.lingx.jt808.thread;

import com.lingx.jt808.netty.TcpClient;

public class StartTcpClientThread implements Runnable {
	private String ip;
	private int port;
	public StartTcpClientThread(String ip,int port) {
	this.ip=ip;
	this.port=port;
	}
	@Override
	public void run() {
		TcpClient.doRequest(ip, port);
	}

}
