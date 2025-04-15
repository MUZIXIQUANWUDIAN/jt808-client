package com.lingx.jt808.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
/**
 * JT808消息解析接口
 * @author lingx
 *
 */
public interface IJT808MsgHandler {
	public int getMsgId();
	public void reset();
	public  ByteBuf getBody(byte data[])throws Exception;
	public String handle(ByteBuf data,String tid,int msgId,int msgSn,ChannelHandlerContext ctx,boolean isVersion,byte[] bytes)throws Exception;
}
