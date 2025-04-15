package com.lingx.jt808;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import com.lingx.jt808.cmd.Cmd0100;
import com.lingx.jt808.cmd.Cmd0102;
import com.lingx.jt808.cmd.Cmd0200;
import com.lingx.jt808.cmd.Cmd0704;
import com.lingx.jt808.msg.JT808MessageHandler;
import com.lingx.jt808.netty.TcpClient;
import com.lingx.jt808.thread.Send0x0200Thread;
import com.lingx.jt808.thread.StartTcpClientThread;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.JT808ClientPanel;
import com.lingx.jtools.ui.MJTextField;
import com.lingx.jtools.ui.PropUtils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class JT808Tools {
	public static JT808MessageHandler jt808MessageHandler=new JT808MessageHandler();
	public static Send0x0200Thread send0x0200Thread=null;

	public static void tcp(String ip,String port) {
		callAardio("正在连接服务器"+ip+":"+port);
		new Thread(new StartTcpClientThread(ip,Integer.parseInt(port))).start();
		//start0x0200();
		
	}
	
	public static void tcpClose() {
		if(TcpClient.channel!=null) {
			callAardio("正在关闭服务器连接");
			TcpClient.channel.close();
			TcpClient.channel=null;
		}else {
			callAardio("当前未连接服务器");
		}
		if(send0x0200Thread!=null) {
			send0x0200Thread.setRun(false);
			send0x0200Thread=null;
		}
	}
	
	public static void sendMessage(String hexstring) {
		if(TcpClient.channel!=null) {
			addMessageReq(hexstring);
			TcpClient.channel.writeAndFlush(Utils.hexToBytes(hexstring));
		}
	}
	public static void sendMessage(byte[] bytes) {
		if(TcpClient.channel!=null) {
			TcpClient.channel.writeAndFlush(bytes);
		}
	}
	public static void clear() {
		JT808ClientPanel.textArea.setText("");
	}
	public static void addText(String msg) {
		//sb.append(msg+"\r\n");
		JT808ClientPanel.textArea.insert(msg+"\r\n", 0);
	}
	public static String addMessageRes(String aaa) {
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(new Date())+" RES"+"-> "+aaa);
		addText(sdf.format(new Date())+" RES"+"-> "+aaa);
		return "";//aardio(aaa);
	}
	public static String addMessageReq(String aaa) {
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
		System.out.println(sdf.format(new Date())+" REQ"+"-> "+aaa);
		addText(sdf.format(new Date())+" REQ"+"-> "+aaa);
		return "";//aardio(aaa);
	}
	public static String callAardio(String aaa) {
		System.out.println(aaa);
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
		
		addText(sdf.format(new Date())+"-> "+aaa);
		return "";//aardio(aaa);
	}
	public static String callAardio(String aaa,boolean cmd) {
		//addText(aaa);
		return "";
	}
	 static native String aardio(String code);
	 
	 public static String jt808hexstring(String hexstring) {
		 if(hexstring==null)return "";
		 hexstring=hexstring.toUpperCase();
		 if(hexstring.startsWith("7E")&&hexstring.endsWith("7E")) {
			 try {
				 String str=jt808MessageHandler.handler(hexstring.trim());
				return str;
			} catch (Exception e) {
				return e.getMessage();
			}
		 }else {
			 callAardio("发送失败,非JT808报文");
			 return "非JT808报文";
		 }
	 }
	 
	 public static String test() {
		 File file=new File("aaa.txt");
		 try {
			file.createNewFile();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		 return System.getProperty("user.dir");
	 }
	 
	 
	 public static String send0x0100(String tid,String id1,String id2,String zzcid,String zdxh,String zdid,String color,String carno) {
		 Cmd0100 cmd=new Cmd0100(tid,Integer.parseInt(id1),Integer.parseInt(id2),zzcid,zdxh,zdid,Integer.parseInt(color),carno);
		 String hexstring=cmd.toMessageHexstring();
		 sendMessage(hexstring);
		 return hexstring;
	 }
	 public static String send0x0100() {
		 String text1 = (PropUtils.getProp("jt808.0x0100.p1"));
		 String text2 =(PropUtils.getProp("jt808.0x0100.p2"));
		 String text3 = (PropUtils.getProp("jt808.0x0100.p3"));
		 String text4 = (PropUtils.getProp("jt808.0x0100.p4"));
		 String text5 =(PropUtils.getProp("jt808.0x0100.p5"));
		 String text6 = (PropUtils.getProp("jt808.0x0100.p6"));
		 String text7 = (PropUtils.getProp("jt808.0x0100.p7"));
	        
		 Cmd0100 cmd=new Cmd0100(tid,Integer.parseInt(text1),Integer.parseInt(text2),text3,text4,text5,Integer.parseInt(text6),text7);
		 String hexstring=cmd.toMessageHexstring();
		 sendMessage(hexstring);
		 return hexstring;
	 }
	 
	 public static String send0x0102(String tid,String code) {
		 Cmd0102 cmd=new Cmd0102(tid,code);
		 String hexstring=cmd.toMessageHexstring();
		 sendMessage(hexstring);
		 return hexstring;
	 }
	 public static String send0x0102() {
		 String code=(PropUtils.getProp("jt808.0x0102.p1"));
		 Cmd0102 cmd=new Cmd0102(tid,code);
		 String hexstring=cmd.toMessageHexstring();
		 sendMessage(hexstring);
		 return hexstring;
	 }
//public Cmd0200(String tid,double lat,double lng,float speed,int height,int fx,float meilage,float oil,int bj,int zt) {
			 
	 public static String send0x0200(String tid,String lat,String lng,String speed,String height,String fx,String meilage,String oil,String time,int bj,int zt) {
		// int bj=0,zt=0;
		 
		 if(send0x0200Thread==null) {
			 send0x0200Thread=new Send0x0200Thread(tid,Double.parseDouble(lat),Double.parseDouble(lng),Float.parseFloat(speed),Integer.parseInt(height),Integer.parseInt(fx),
					 Float.parseFloat(meilage),Float.parseFloat(oil),bj,zt,Integer.parseInt(time));
			 new Thread(send0x0200Thread).start();
		 }else {
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
	 public static String send0x0200() { 
		 if(send0x0200Thread==null) {
		 send0x0200Thread=new Send0x0200Thread(tid);
		 new Thread(send0x0200Thread).start();
	 }
		
		 return "";
	 }
	 public static String send0x0704(String tid,String lat,String lng,String speed,String height,String fx,String meilage,String oil,String time,int bj,int zt) {
		 Cmd0704 cmd = new Cmd0704(tid,Double.parseDouble(lat),Double.parseDouble(lng),Float.parseFloat(speed),Integer.parseInt(height),Integer.parseInt(fx),
				 Float.parseFloat(meilage),Float.parseFloat(oil),bj,zt);
		 String hexstring=cmd.toMessageHexstring();
		 sendMessage(hexstring);
		 return hexstring;
		 }
	 private static int bjid=0;
	 public static void sendAdas() {
		 if(send0x0200Thread==null)return;
		 bjid++;
		 String time=Utils.getTime();
		 String tempBjbsh=getRandomNumber(31)+"A";
		 ByteBuf buff=Unpooled.buffer();
		 buff.writeInt(bjid);
		 buff.writeByte(0);
		 buff.writeByte(3); //报警
		 buff.writeByte(1); //报警级别
		 buff.writeByte(0);
		 buff.writeByte(0);
		 buff.writeByte(0);
		 buff.writeByte(0);
		 buff.writeByte(0);
		 buff.writeByte(0);

		 buff.writeShort(send0x0200Thread.getHeight());
		 buff.writeInt(new Double(send0x0200Thread.getLat()*1000000f).intValue());
		 buff.writeInt(new Double(send0x0200Thread.getLng()*1000000f).intValue());
		 buff.writeBytes(Utils.hexToBytes(time.substring(2)));
		 buff.writeShort(0); //车辆状态
		 buff.writeBytes(Utils.hexToBytes(tempBjbsh));
		 byte bytes[]=new byte[buff.readableBytes()];
		 buff.readBytes(bytes);
		 Cmd0200 cmd = new Cmd0200(tid, send0x0200Thread.getLat(), send0x0200Thread.getLng(), send0x0200Thread.getSpeed(), send0x0200Thread.getHeight(), send0x0200Thread.getFx(), send0x0200Thread.getMeilage(), send0x0200Thread.getOil(), send0x0200Thread.getBj(), send0x0200Thread.getZt(),
				 0x64,bytes);
			String hexstring = cmd.toMessageHexstring();
			JT808Tools.sendMessage(hexstring);
	 }
	 public static void sendDsm() {
		 if(send0x0200Thread==null)return;
		 bjid++;
		 String time=Utils.getTime();
		 String tempBjbsh=getRandomNumber(31)+"D";
		 ByteBuf buff=Unpooled.buffer();
		 buff.writeInt(bjid);
		 buff.writeByte(0);
			buff.writeByte(3); //报警
			buff.writeByte(1); //报警级别
			buff.writeByte(0); //疲劳程度
			buff.writeByte(0);
			buff.writeByte(0);
			buff.writeByte(0);
			buff.writeByte(0);
			buff.writeByte(0);

		 buff.writeShort(send0x0200Thread.getHeight());
		 buff.writeInt(new Double(send0x0200Thread.getLat()*1000000f).intValue());
		 buff.writeInt(new Double(send0x0200Thread.getLng()*1000000f).intValue());
		 buff.writeBytes(Utils.hexToBytes(time.substring(2)));
		 buff.writeShort(0); //车辆状态
		 buff.writeBytes(Utils.hexToBytes(tempBjbsh));
		 byte bytes[]=new byte[buff.readableBytes()];
		 buff.readBytes(bytes);
		 Cmd0200 cmd = new Cmd0200(tid, send0x0200Thread.getLat(), send0x0200Thread.getLng(), send0x0200Thread.getSpeed(), send0x0200Thread.getHeight(), send0x0200Thread.getFx(), send0x0200Thread.getMeilage(), send0x0200Thread.getOil(), send0x0200Thread.getBj(), send0x0200Thread.getZt(),
				 0x65,bytes);
			String hexstring = cmd.toMessageHexstring();
			JT808Tools.sendMessage(hexstring);
	 }

		public static String getRandomNumber(int bit){
			Random random=new Random();
			StringBuilder sb=new StringBuilder();
			String temp="1234567890ABCDEF";
			int max=10;
			for(int i=0;i<bit;i++){
				int t=random.nextInt(max);
				sb.append(temp.charAt(t));
			}
			return sb.toString();
		}
		static String tid;
		public static String setTid(String tid1,String version) {
			if(version.contains("2019")) {
				tid=Utils.leftAdd0(tid1, 20);
			}else {
				tid=Utils.leftAdd0(tid1, 12);
			}
			return tid;
		}
		public static void start0x0200() {
			send0x0200(tid,String.valueOf(39.916385),String.valueOf(116.396621),"66","88","0","123","0","15",0,3);
		}
}
