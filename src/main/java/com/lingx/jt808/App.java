package com.lingx.jt808;

import com.lingx.jtools.ui.TcpClientTabBridge;

public class App {

	public static void main(String[] args) {
		String ip = "47.100.112.218", port = "8808", tid = "012345678912";
		if (args.length > 0) {
			ip = args[0];
		}
		if (args.length > 1) {
			port = args[1];
		}
		if (args.length > 2) {
			tid = args[2];
		}
		try {
			TcpClientTabBridge ui = new TcpClientTabBridge();
			JT808ClientContext ctx = new JT808ClientContext(ui);
			ctx.setTid(tid, "");
			ctx.tcp(ip, port);
			Thread.sleep(1000);
			ctx.start0x0200();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
