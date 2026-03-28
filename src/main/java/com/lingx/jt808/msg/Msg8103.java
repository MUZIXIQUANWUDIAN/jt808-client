package com.lingx.jt808.msg;

import com.lingx.jt808.netty.MyByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class Msg8103 extends AbstractMsgHandler implements IJT808MsgHandler {
	
 
	@Override
	public int getMsgId() {
		return 0x8103;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes12)throws Exception {
		MyByteBuf buff=new MyByteBuf(data);
		int num=data.readByte();
		sb.append("参数个数:").append(num).append("\r\n");
		for(int i=0;i<num;i++) {
			long paramId=data.readInt();
			int length=data.readByte();
			sb.append(i+1).append(".参数ID:").append(paramId).append("=").append(String.format("0x%04X", paramId)).append("\r\n");
			sb.append(i+1).append(".参数长度:").append(length).append("\r\n");
			if(length==1) {
				int val=buff.readByte();
				sb.append(i+1).append(".参数值:").append(val).append("\r\n");
			}else if(length==2) {
				int val=buff.readUnsignedShort();
				sb.append(i+1).append(".参数值:").append(val).append("\r\n");
			}else if(length==4) {
				long val=data.readUnsignedInt();
				sb.append(i+1).append(".参数值:").append(val).append("\r\n");
			}else if(length==5) {
				long val=data.readLong();
				sb.append(i+1).append(".参数值:").append(val).append("\r\n");
			}else {
				String bcdstring=buff.readStringBCD(length);
				sb.append(i+1).append(".参数值:").append(bcdstring).append("\r\n");
				
			}
		}
		return sb.toString();
	}

}
