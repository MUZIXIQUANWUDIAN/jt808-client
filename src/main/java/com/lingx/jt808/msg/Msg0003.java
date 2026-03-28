package com.lingx.jt808.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class Msg0003 extends AbstractMsgHandler implements IJT808MsgHandler{
	@Override
	public int getMsgId() {
		return 0x0003;
	}
	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes)throws Exception {

		return sb.toString();
	}


}
