package com.lingx.gps.netty.nojt808;

import java.text.SimpleDateFormat;
import java.util.Date;

import com.lingx.jt808.cmd.Cmd8001;
import com.lingx.jt808.cmd.Cmd8100;
import com.lingx.jt808.netty.MyByteBuf;
import com.lingx.jt808.utils.JT808Utils;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.NoJT808ServerPanel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
@Sharable
public class NoJt808ChannelHandler extends SimpleChannelInboundHandler<byte[]>{
	public static String addMessageRes(String aaa) {
		SimpleDateFormat sdf=new SimpleDateFormat("HH:mm:ss");
		//System.out.println(sdf.format(new Date())+" RES"+"-> "+aaa);
		String temp=(sdf.format(new Date())+" "+"-> "+aaa+"\r\n");
		NoJT808ServerPanel.textArea.insert(temp, 0);
		return "";//aardio(aaa);
	}
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] data) throws Exception {
		//System.out.println("收到字节数:"+data.length);
		if(NoJT808ServerPanel.hex.isSelected()) {
			String hexstring=Utils.bytesToHex(data);
			addMessageRes(hexstring);
		}else {
			String hexstring=new String(data);
			addMessageRes(hexstring);
		}
		
		
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
