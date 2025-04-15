package com.lingx.gps.netty.nojt808.client;

import com.lingx.jt808.JT808Tools;
import com.lingx.jt808.cmd.Cmd0001;
import com.lingx.jt808.cmd.Cmd0104;
import com.lingx.jt808.cmd.Cmd0805;
import com.lingx.jt808.cmd.Cmd1205;
import com.lingx.jt808.thread.SendAdasFileThread;
import com.lingx.jt808.thread.SendImageThread;
import com.lingx.jt808.thread.SendVideoThread;
import com.lingx.jt808.utils.JT808Utils;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.JT808ClientPanel;
import com.lingx.jtools.ui.NoJT808ClientPanel;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NoJt808TcpClientHandler  extends SimpleChannelInboundHandler<byte[]>  {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] arg1) throws Exception {
		if(NoJT808ClientPanel.hex.isSelected()) {
			String hexstring=Utils.bytesToHex(arg1);
			NoJT808ClientPanel.addMessage(hexstring);
		}else {
			String hexstring=new String(arg1);
			NoJT808ClientPanel.addMessage(hexstring);
		}
		
		
	}
	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		//System.out.println("==============长连接失效===============");
		super.channelInactive(ctx);
		NoJT808ClientPanel.addMessage("服务器连接断开");
		NoJT808ClientPanel.setButtunZt1();
		NoJt808TcpClient.channel=null;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		//System.out.println("==============长连接接入===============");
		super.channelActive(ctx);
		NoJt808TcpClient.channel=ctx.channel();
		NoJT808ClientPanel.addMessage("服务器连接成功");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx,Throwable t) {
		NoJT808ClientPanel.addMessage("服务器连接异常");
	}
	
}
