package com.lingx.jt808.msg;

import com.lingx.jt808.netty.MyByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;


public class Msg0805 extends AbstrctMsgHandler implements IJT808MsgHandler {
	
	@Override
	public int getMsgId() {
		return 0x0805;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx, boolean isVersion,byte[] bytes)throws Exception {
		MyByteBuf mbuff = new MyByteBuf(data);
		int resMsgSn = mbuff.readShort();
		int ret = mbuff.readByte();
		int len = mbuff.readShort();
		StringBuilder sb=new StringBuilder();
		for(int i=0;i<len;i++) {
			sb.append(mbuff.readInt()).append(",");
		}
		if(sb.length()>0) {sb.deleteCharAt(sb.length()-1);}
		
		
		mbuff.release();
		mbuff=null;
		return sb.toString();
	}

}
