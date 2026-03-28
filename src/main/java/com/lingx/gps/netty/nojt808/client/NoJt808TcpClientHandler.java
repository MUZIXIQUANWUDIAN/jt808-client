package com.lingx.gps.netty.nojt808.client;

import java.util.function.BooleanSupplier;

import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.TcpClientTabBridge;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class NoJt808TcpClientHandler extends SimpleChannelInboundHandler<byte[]> {

	private final NoJt808TcpClient client;
	private final TcpClientTabBridge ui;
	private final BooleanSupplier hexDisplay;

	public NoJt808TcpClientHandler(NoJt808TcpClient client, TcpClientTabBridge ui, BooleanSupplier hexDisplay) {
		this.client = client;
		this.ui = ui;
		this.hexDisplay = hexDisplay;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] arg1) {
		if (hexDisplay.getAsBoolean()) {
			ui.appendArrowLog(Utils.bytesToHex(arg1));
		} else {
			ui.appendArrowLog(new String(arg1));
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		client.setChannel(null);
		ui.appendArrowLog("服务器连接断开");
		ui.notifyDisconnected();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		client.setChannel(ctx.channel());
		ui.appendArrowLog("服务器连接成功");
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
		ui.appendArrowLog("服务器连接异常");
	}
}
