package com.lingx.jt808;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;

import com.lingx.jt808.cmd.Cmd0100;
import com.lingx.jt808.cmd.Cmd0102;
import com.lingx.jt808.cmd.Cmd0200;
import com.lingx.jt808.cmd.Cmd0704;
import com.lingx.jt808.msg.JT808MessageHandler;
import com.lingx.jt808.thread.Send0x0200Thread;
import com.lingx.jt808.thread.StartTcpClientThread;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.PropUtils;
import com.lingx.jtools.ui.TcpClientTabBridge;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;

/**
 * JT808 client session: connection, logging, and outbound messages for one {@link com.lingx.jtools.ui.JT808ClientPanel}.
 */
public class JT808ClientContext {

	private final TcpClientTabBridge ui;
	private final JT808MessageHandler jt808MessageHandler = new JT808MessageHandler();
	private volatile Channel channel;
	private volatile Send0x0200Thread send0x0200Thread;
	private volatile Thread send0x0200Worker;
	private volatile String tid;
	private volatile boolean trafficLogEnabled = true;
	private volatile boolean statusLogEnabled = true;
	private final AtomicLong sentCounter = new AtomicLong();
	private int bjid;
	/** 批量模式共享的 EventLoopGroup，null 时由 TcpClient 自建 */
	private EventLoopGroup sharedGroup;

	public JT808ClientContext(TcpClientTabBridge ui) {
		this.ui = ui;
	}

	/** 设置共享 EventLoopGroup（批量模式使用） */
	public void setSharedGroup(EventLoopGroup group) {
		this.sharedGroup = group;
	}

	public TcpClientTabBridge getUi() {
		return ui;
	}

	public JT808MessageHandler getMessageHandler() {
		return jt808MessageHandler;
	}

	public Channel getChannel() {
		return channel;
	}

	public void setChannel(Channel channel) {
		this.channel = channel;
	}

	public void tcp(String ip, String port) {
		callAardio("正在连接服务器" + ip + ":" + port);
		new Thread(new StartTcpClientThread(ip, Integer.parseInt(port), this, sharedGroup)).start();
	}

	public void tcpClose() {
		Channel ch = channel;
		if (ch != null) {
			callAardio("正在关闭服务器连接");
			ch.close();
			channel = null;
		} else {
			callAardio("当前未连接服务器");
		}
		if (send0x0200Thread != null) {
			send0x0200Thread.setRun(false);
		}
		Thread worker = send0x0200Worker;
		send0x0200Worker = null;
		send0x0200Thread = null;
		if (worker != null) {
			worker.interrupt();
		}
	}

	public void sendMessage(String hexstring) {
		Channel ch = channel;
		if (ch != null) {
			if (trafficLogEnabled) {
				addMessageReq(hexstring);
			}
			ch.writeAndFlush(Unpooled.wrappedBuffer(Utils.hexToBytes(hexstring)));
			sentCounter.incrementAndGet();
		}
	}

	public void sendMessage(byte[] bytes) {
		Channel ch = channel;
		if (ch != null && bytes != null) {
			if (trafficLogEnabled) {
				addMessageReq(Utils.bytesToHex(bytes));
			}
			ch.writeAndFlush(Unpooled.wrappedBuffer(bytes));
			sentCounter.incrementAndGet();
		}
	}

	public void clear() {
		ui.clearLog();
	}

	public void addText(String msg) {
		ui.appendLog(msg);
	}

	public String addMessageRes(String aaa) {
		if (!trafficLogEnabled) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(new Date()) + " RES" + "-> " + aaa);
		addText(sdf.format(new Date()) + " RES" + "-> " + aaa);
		return "";
	}

	public String addMessageReq(String aaa) {
		if (!trafficLogEnabled) {
			return "";
		}
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(new Date()) + " REQ" + "-> " + aaa);
		addText(sdf.format(new Date()) + " REQ" + "-> " + aaa);
		return "";
	}

	public String callAardio(String aaa) {
		if (!statusLogEnabled) {
			return "";
		}
		System.out.println(aaa);
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
		addText(sdf.format(new Date()) + "-> " + aaa);
		return "";
	}

	public String callAardio(String aaa, boolean cmd) {
		if (!statusLogEnabled) {
			return "";
		}
		return "";
	}

	public void setTrafficLogEnabled(boolean enabled) {
		this.trafficLogEnabled = enabled;
	}

	public void setStatusLogEnabled(boolean enabled) {
		this.statusLogEnabled = enabled;
	}

	public long getSentCount() {
		return sentCounter.get();
	}

	public boolean isConnected() {
		Channel ch = channel;
		return ch != null && ch.isActive();
	}

	public String jt808hexstring(String hexstring) {
		if (hexstring == null) {
			return "";
		}
		hexstring = hexstring.toUpperCase();
		if (hexstring.startsWith("7E") && hexstring.endsWith("7E")) {
			try {
				return jt808MessageHandler.handler(hexstring.trim());
			} catch (Exception e) {
				return e.getMessage();
			}
		} else {
			callAardio("发送失败,非JT808报文");
			return "非JT808报文";
		}
	}

	public String test() {
		File file = new File("aaa.txt");
		try {
			file.createNewFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return System.getProperty("user.dir");
	}

	public String send0x0100(String tidArg, String id1, String id2, String zzcid, String zdxh, String zdid, String color,
			String carno) {
		Cmd0100 cmd = new Cmd0100(tidArg, Integer.parseInt(id1), Integer.parseInt(id2), zzcid, zdxh, zdid,
				Integer.parseInt(color), carno);
		String hexstring = cmd.toMessageHexstring();
		sendMessage(hexstring);
		return hexstring;
	}

	public String send0x0100() {
		String text1 = PropUtils.getProp("jt808.0x0100.p1","0");
		String text2 = PropUtils.getProp("jt808.0x0100.p2","0");
		String text3 = PropUtils.getProp("jt808.0x0100.p3","0");
		String text4 = PropUtils.getProp("jt808.0x0100.p4","0");
		String text5 = PropUtils.getProp("jt808.0x0100.p5","0");
		String text6 = PropUtils.getProp("jt808.0x0100.p6","0");
		String text7 = PropUtils.getProp("jt808.0x0100.p7","0");

		Cmd0100 cmd = new Cmd0100(tid, Integer.parseInt(text1), Integer.parseInt(text2), text3, text4, text5,
				Integer.parseInt(text6), text7);
		String hexstring = cmd.toMessageHexstring();
		sendMessage(hexstring);
		return hexstring;
	}

	public String send0x0102(String tidArg, String code) {
		Cmd0102 cmd = new Cmd0102(tidArg, code);
		String hexstring = cmd.toMessageHexstring();
		sendMessage(hexstring);
		return hexstring;
	}

	public String send0x0102() {
		String code = PropUtils.getProp("jt808.0x0102.p1","0");
		Cmd0102 cmd = new Cmd0102(tid, code);
		String hexstring = cmd.toMessageHexstring();
		sendMessage(hexstring);
		return hexstring;
	}

	public String send0x0200(String tidArg, String lat, String lng, String speed, String height, String fx,
			String meilage, String oil, String time, int bj, int zt) {
		if (send0x0200Thread == null) {
			send0x0200Thread = new Send0x0200Thread(this, tidArg, Double.parseDouble(lat), Double.parseDouble(lng),
					Float.parseFloat(speed), Integer.parseInt(height), Integer.parseInt(fx), Float.parseFloat(meilage),
					Float.parseFloat(oil), bj, zt, Integer.parseInt(time));
			Thread t = new Thread(send0x0200Thread, "jt808-Send0x0200");
			send0x0200Worker = t;
			t.start();
		} else {
			send0x0200Thread.setLat(Double.parseDouble(lat));
			send0x0200Thread.setLng(Double.parseDouble(lng));
			send0x0200Thread.setSpeed(Float.parseFloat(speed));
			send0x0200Thread.setHeight(Integer.parseInt(height));
			send0x0200Thread.setFx(Integer.parseInt(fx));
			send0x0200Thread.setMeilage(Float.parseFloat(meilage));
			send0x0200Thread.setOil(Float.parseFloat(oil));
			send0x0200Thread.setBj(bj);
			send0x0200Thread.setZt(zt);
			send0x0200Thread.setTime(Integer.parseInt(time));
			send0x0200Thread.send();
		}
		return "";
	}

	public String send0x0200() {
		if (send0x0200Thread == null) {
			send0x0200Thread = new Send0x0200Thread(this, tid);
			Thread t = new Thread(send0x0200Thread, "jt808-Send0x0200");
			send0x0200Worker = t;
			t.start();
		}
		return "";
	}

	public String send0x0704(String tidArg, String lat, String lng, String speed, String height, String fx,
			String meilage, String oil, String time, int bj, int zt) {
		Cmd0704 cmd = new Cmd0704(tidArg, Double.parseDouble(lat), Double.parseDouble(lng), Float.parseFloat(speed),
				Integer.parseInt(height), Integer.parseInt(fx), Float.parseFloat(meilage), Float.parseFloat(oil), bj, zt);
		String hexstring = cmd.toMessageHexstring();
		sendMessage(hexstring);
		return hexstring;
	}

	public void sendAdas() {
		if (send0x0200Thread == null) {
			return;
		}
		bjid++;
		String time = Utils.getTime();
		String tempBjbsh = getRandomNumber(31) + "A";
		ByteBuf buff = Unpooled.buffer();
		buff.writeInt(bjid);
		buff.writeByte(0);
		buff.writeByte(3);
		buff.writeByte(1);
		buff.writeByte(0);
		buff.writeByte(0);
		buff.writeByte(0);
		buff.writeByte(0);
		buff.writeByte(0);
		buff.writeByte(0);

		buff.writeShort(send0x0200Thread.getHeight());
		buff.writeInt(new Double(send0x0200Thread.getLat() * 1000000f).intValue());
		buff.writeInt(new Double(send0x0200Thread.getLng() * 1000000f).intValue());
		buff.writeBytes(Utils.hexToBytes(time.substring(2)));
		buff.writeShort(0);
		buff.writeBytes(Utils.hexToBytes(tempBjbsh));
		byte bytes[] = new byte[buff.readableBytes()];
		buff.readBytes(bytes);
		Cmd0200 cmd = new Cmd0200(tid, send0x0200Thread.getLat(), send0x0200Thread.getLng(), send0x0200Thread.getSpeed(),
				send0x0200Thread.getHeight(), send0x0200Thread.getFx(), send0x0200Thread.getMeilage(),
				send0x0200Thread.getOil(), send0x0200Thread.getBj(), send0x0200Thread.getZt(), 0x64, bytes);
		String hexstring = cmd.toMessageHexstring();
		sendMessage(hexstring);
	}

	public void sendDsm() {
		if (send0x0200Thread == null) {
			return;
		}
		bjid++;
		String time = Utils.getTime();
		String tempBjbsh = getRandomNumber(31) + "D";
		ByteBuf buff = Unpooled.buffer();
		buff.writeInt(bjid);
		buff.writeByte(0);
		buff.writeByte(3);
		buff.writeByte(1);
		buff.writeByte(0);
		buff.writeByte(0);
		buff.writeByte(0);
		buff.writeByte(0);
		buff.writeByte(0);
		buff.writeByte(0);

		buff.writeShort(send0x0200Thread.getHeight());
		buff.writeInt(new Double(send0x0200Thread.getLat() * 1000000f).intValue());
		buff.writeInt(new Double(send0x0200Thread.getLng() * 1000000f).intValue());
		buff.writeBytes(Utils.hexToBytes(time.substring(2)));
		buff.writeShort(0);
		buff.writeBytes(Utils.hexToBytes(tempBjbsh));
		byte bytes[] = new byte[buff.readableBytes()];
		buff.readBytes(bytes);
		Cmd0200 cmd = new Cmd0200(tid, send0x0200Thread.getLat(), send0x0200Thread.getLng(), send0x0200Thread.getSpeed(),
				send0x0200Thread.getHeight(), send0x0200Thread.getFx(), send0x0200Thread.getMeilage(),
				send0x0200Thread.getOil(), send0x0200Thread.getBj(), send0x0200Thread.getZt(), 0x65, bytes);
		String hexstring = cmd.toMessageHexstring();
		sendMessage(hexstring);
	}

	public static String getRandomNumber(int bit) {
		Random random = new Random();
		StringBuilder sb = new StringBuilder();
		String temp = "1234567890ABCDEF";
		int max = 10;
		for (int i = 0; i < bit; i++) {
			int t = random.nextInt(max);
			sb.append(temp.charAt(t));
		}
		return sb.toString();
	}

	public String setTid(String tid1, String version) {
		if (version != null && version.contains("2019")) {
			tid = Utils.leftAdd0(tid1, 20);
		} else {
			tid = Utils.leftAdd0(tid1, 12);
		}
		return tid;
	}

	public void start0x0200() {
		send0x0200(tid, String.valueOf(39.916385), String.valueOf(116.396621), "66", "88", "0", "123", "0", "15", 0, 3);
	}
}
