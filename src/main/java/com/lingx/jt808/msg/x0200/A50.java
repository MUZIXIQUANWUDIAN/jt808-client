package com.lingx.jt808.msg.x0200;

import com.lingx.jt808.msg.IJT808MsgAttached;
import com.lingx.jt808.utils.Utils;

import io.netty.channel.ChannelHandlerContext;
public class A50 implements IJT808MsgAttached {

	@Override
	public int getAttachedId() {
		return 0x50;
	}

	@Override
	public Object getValue(byte[] bytes,String tid,ChannelHandlerContext ctx) {
		
		return Utils.byteArrayToInt(bytes);
	}

}
