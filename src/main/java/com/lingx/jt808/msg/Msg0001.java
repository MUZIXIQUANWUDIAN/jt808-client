package com.lingx.jt808.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
public class Msg0001 extends AbstractMsgHandler implements IJT808MsgHandler{
	@Override
	public int getMsgId() {
		return 0x0001;
	}
	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes) throws Exception {
		if(data.readableBytes()<5)return "内容小于5字节属于异常的回复报文";//内容小于5字节属于异常的回复报文
		int resMsgSn=data.readShort();
		int resMsgId=data.readShort();
		int ret=data.readByte();//0：成功/确认；1：失败；2：消息有误；3：不支持
		
		return sb.toString();
	}


}
