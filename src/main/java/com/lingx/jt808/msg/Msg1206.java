package com.lingx.jt808.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class Msg1206 extends AbstrctMsgHandler implements IJT808MsgHandler{

	@Override
	public int getMsgId() {
		return 0x1206;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx, boolean isVersion,byte[] bytes)
			throws Exception {
		int resMsgId=0x9206;
		int resMsgSn=data.readUnsignedShort();
		int ret=data.readByte();
		String text="";
		if(ret==0){
			 text="媒体文件上传成功";
		}else{
			 text="媒体文件上传失败";
		}
		return sb.toString();
		}
}
