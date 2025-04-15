package com.lingx.jt808;

public class App {

	public static void main(String[] args) {
		String ip="47.100.112.218",port="8808",tid="012345678912";
		if(args.length>0)ip=args[0];
		if(args.length>1)port=args[1];
		if(args.length>2)tid=args[2];
		try {
			JT808Tools.tcp(ip,port);
			JT808Tools.setTid(tid,"");
			Thread.currentThread().sleep(1000);
			JT808Tools.start0x0200();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
