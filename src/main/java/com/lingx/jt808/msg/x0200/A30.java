package com.lingx.jt808.msg.x0200;

import com.lingx.jt808.msg.IJT808MsgAttached;
import com.lingx.jt808.utils.Utils;

import io.netty.channel.ChannelHandlerContext;
public class A30 implements IJT808MsgAttached {

	@Override
	public int getAttachedId() {
		return 0x30;//通信信号
	}

	@Override
	public Object getValue(byte[] bytes,String tid,ChannelHandlerContext ctx) {
		int num=Utils.byteArrayToInt(bytes);
		return num>100?100:num;
	}

}
