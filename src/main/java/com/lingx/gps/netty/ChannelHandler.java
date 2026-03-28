package com.lingx.gps.netty;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.lingx.jt808.cmd.Cmd8001;
import com.lingx.jt808.cmd.Cmd8100;
import com.lingx.jt808.netty.MyByteBuf;
import com.lingx.jt808.utils.JT808Utils;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.JT808ServerPanel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
@Sharable
public class ChannelHandler extends SimpleChannelInboundHandler<byte[]>{
	public static String addMessageRes(String aaa) {
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
		//System.out.println(sdf.format(new Date())+" RES"+"-> "+aaa);
		String temp=(sdf.format(new Date())+" "+"-> "+aaa+"\r\n");
		JT808ServerPanel.textArea.insert(temp, 0);
		return "";//aardio(aaa);
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] data) throws Exception {
		//System.out.println("收到字节数:"+data.length);
		String hexstring=Utils.bytesToHex(data);
		addMessageRes(hexstring);
		MyByteBuf buff = new MyByteBuf(JT808Utils.decode(data));
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
		case 0x0100:
			Cmd8100 cmd8100=new Cmd8100(tid,msgSn,(byte)0,"123456");
			ctx.write(cmd8100.toMessageByteBuf());
			break;
		default:
			Cmd8001 cmd00011=new Cmd8001(tid,msgSn,msgId);
			ctx.writeAndFlush(cmd00011.toMessageByteBuf());
		}
		content.release();
		buff.release();
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//System.out.println("==============长连接失效===============");
		super.channelInactive(ctx);
		addMessageRes("客户端断开:"+ctx.channel().remoteAddress().toString());
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//System.out.println("==============长连接接入===============");
		super.channelActive(ctx);
		addMessageRes("客户端接入:"+ctx.channel().remoteAddress().toString());
	}
}
