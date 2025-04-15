package com.lingx.jt808.netty;

import com.lingx.jt808.JT808Tools;
import com.lingx.jt808.cmd.Cmd0001;
import com.lingx.jt808.cmd.Cmd0104;
import com.lingx.jt808.cmd.Cmd0201;
import com.lingx.jt808.cmd.Cmd0805;
import com.lingx.jt808.cmd.Cmd1205;
import com.lingx.jt808.thread.SendAdasFileThread;
import com.lingx.jt808.thread.SendImageThread;
import com.lingx.jt808.thread.SendVideoThread;
import com.lingx.jt808.utils.JT808Utils;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.JT808ClientPanel;
import com.lingx.jtools.ui.PropUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TcpClientHandler  extends SimpleChannelInboundHandler<byte[]>  {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] arg1) throws Exception {
		String hexstring=Utils.bytesToHex(arg1);
		JT808Tools.addMessageRes(hexstring);
		
		MyByteBuf buff = new MyByteBuf(JT808Utils.decode(arg1));
		buff.readByte();
		int msgId = buff.readUnsignedShort();
		int length = buff.readUnsignedShort();
		String tid = "";
		boolean isFB = (length & 0b0010000000000000) > 0;// 是否分包
		boolean isVersion = (length & 0b0100000000000000) > 0;// 是否版本标识
		if (isVersion) {
			buff.readByte();
			tid = buff.readStringBCD(10);
		} else {
			tid = buff.readStringBCD(6);
		}
		int msgSn = buff.readUnsignedShort();// 消息流水号
		length = length & 0x3ff;
		ByteBuf content = buff.readByteBuf(length);
		byte check = buff.readByte();
		buff.readByte();
		switch(msgId) {
		case 0x8001:
			JT808Tools.send0x0200();
			break;
		case 0x8100:
			JT808Tools.send0x0102();
			break;
		case 0x8103:
			Cmd0001 cmd0001=new Cmd0001(tid,msgSn,msgId);
			ctx.writeAndFlush(cmd0001.toMessageBytes());
			break;
		case 0x8106:
			content.readByte();
			int id=content.readInt();
			if(id==0x0075) {
				Cmd0104 cmd=new Cmd0104(tid,0x75,"HEXSTRING","010100640F00000100000500281900000400002F01",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0001) {
				Cmd0104 cmd=new Cmd0104(tid,0x01,"BYTE","30",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0013) {
				Cmd0104 cmd=new Cmd0104(tid,0x0013,"STRING","127.0.0.1",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0017) {
				Cmd0104 cmd=new Cmd0104(tid,0x0017,"STRING","127.0.0.1",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0018) {
				Cmd0104 cmd=new Cmd0104(tid,0x0018,"WORD","8808",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0019) {
				Cmd0104 cmd=new Cmd0104(tid,0x0019,"WORD","8808",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0027) {
				Cmd0104 cmd=new Cmd0104(tid,0x0027,"WORD","30",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0029) {
				Cmd0104 cmd=new Cmd0104(tid,0x0029,"WORD","60",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0055) {
				Cmd0104 cmd=new Cmd0104(tid,0x0055,"WORD","120",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0056) {
				Cmd0104 cmd=new Cmd0104(tid,0x0056,"WORD","10",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}else if(id==0x0057) {
				Cmd0104 cmd=new Cmd0104(tid,0x0057,"WORD","6000",msgSn);
				ctx.writeAndFlush(cmd.toMessageBytes());
			}
			break;
		case 0x8201:
		{
			double lat;double lng;float speed;int height;int fx;float meilage;float oil;int bj;int zt;
			lat=Double.parseDouble(PropUtils.getProp("jt808.0x0200.lat"));
			lng=Double.parseDouble(PropUtils.getProp("jt808.0x0200.lng"));
			speed=Float.parseFloat(PropUtils.getProp("jt808.0x0200.speed"));
			height=Integer.parseInt(PropUtils.getProp("jt808.0x0200.height"));
			fx=Integer.parseInt(PropUtils.getProp("jt808.0x0200.direction"));
			meilage=Float.parseFloat(PropUtils.getProp("jt808.0x0200.mileage"));
			oil=Float.parseFloat(PropUtils.getProp("jt808.0x0200.oil"));
			bj=0;
			if("true".equals(PropUtils.getProp("jt808.0x0200.bj0")))bj+=1;
			if("true".equals(PropUtils.getProp("jt808.0x0200.bj1")))bj+=2;
			if("true".equals(PropUtils.getProp("jt808.0x0200.bj2")))bj+=4;
			zt=0;
			if("true".equals(PropUtils.getProp("jt808.0x0200.zt0")))zt+=1;
			if("true".equals(PropUtils.getProp("jt808.0x0200.zt1")))zt+=2;
			Cmd0201 cmd0201=new Cmd0201(tid,msgSn,lat, lng, speed, height, fx, meilage, oil, bj, zt);
			ctx.writeAndFlush(cmd0201.toMessageBytes());
		}
			break;
		case 0x8801:
			SendImageThread sit=new SendImageThread(tid);
			new Thread(sit).start();
			Cmd0805 cmd0805=new Cmd0805(tid,msgSn);
			ctx.writeAndFlush(cmd0805.toMessageBytes());
			break;
		case 0x9101:
		{
			int len=content.readByte();
			MyByteBuf mbb=new MyByteBuf(content);
			String ip=mbb.readString(len);
			int port=mbb.readUnsignedShort();
			mbb.readUnsignedShort();
			int tdh=mbb.readByte();

			JT808Tools.callAardio(String.format("实时视频服务器：%s:%s,tid:%s-%s",ip,port,tid,tdh));
			SendVideoThread svt=new SendVideoThread(ip,port,tid,tdh);
			new Thread(svt).start();
		}
			break;
		case 0x9201:
		{
			int len=content.readByte();
			MyByteBuf mbb=new MyByteBuf(content);
			String ip=mbb.readString(len);
			int port=mbb.readUnsignedShort();
			mbb.readUnsignedShort();
			int tdh=mbb.readByte();

			JT808Tools.callAardio(String.format("回放视频服务器：%s:%s,tid:%s-%s",ip,port,tid,tdh));
			SendVideoThread svt=new SendVideoThread(ip,port,tid,tdh);
			new Thread(svt).start();
		}
			break;
		case 0x9205:
			MyByteBuf mbb3=new MyByteBuf(content);
			int tdh3=mbb3.readByte();
			String stime=mbb3.readStringBCD(6);
			String etime=mbb3.readStringBCD(6);
			long bj=mbb3.getBytebuf().readLong();
			int type=mbb3.readByte();
			int mltype=mbb3.readByte();
			int ccqtype=mbb3.readByte();
			Cmd1205 cmd1205=new Cmd1205(tid,msgSn,tdh3,stime,etime,type,mltype,ccqtype);
			ctx.writeAndFlush(cmd1205.toMessageBytes());
			break;
		case 0x9208://上传苏标附件
			int len2=content.readByte();
			MyByteBuf mbb2=new MyByteBuf(content);
			String ip2=mbb2.readString(len2);
			int port2=mbb2.readUnsignedShort();
			int port21=mbb2.readUnsignedShort();
			String bjbsh=mbb2.readStringBCD(16);
			String bjbh=mbb2.readString(32);
			new Thread(new SendAdasFileThread(tid,ip2,port2,bjbsh,bjbh)).start();
			break;
		default:
			Cmd0001 cmd00011=new Cmd0001(tid,msgSn,msgId);
			ctx.writeAndFlush(cmd00011.toMessageBytes());
		}
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//System.out.println("==============长连接失效===============");
		super.channelInactive(ctx);
		JT808Tools.callAardio("服务器连接断开");
		//JT808Tools.callAardio("1002",true);
		JT808ClientPanel.setButtunZt1();
		TcpClient.channel=null;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//System.out.println("==============长连接接入===============");
		super.channelActive(ctx);
		TcpClient.channel=ctx.channel();
		JT808Tools.callAardio("服务器连接成功");
		JT808Tools.send0x0100();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx,Throwable t) {
		JT808Tools.callAardio("服务器连接异常");
		JT808Tools.callAardio("1004",true);
	}
	
}
