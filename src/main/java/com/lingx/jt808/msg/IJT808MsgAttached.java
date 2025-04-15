package com.lingx.jt808.msg;

import io.netty.channel.ChannelHandlerContext;
/**
 * 0x0200附加消息处理接口
 * @author lingx
 *
 */
public interface IJT808MsgAttached {

	public int getAttachedId();
	
	public Object getValue(byte[] bytes,String tid,ChannelHandlerContext ctx);
}
