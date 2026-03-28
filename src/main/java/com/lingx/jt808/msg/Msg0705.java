package com.lingx.jt808.msg;

import java.util.HashMap;
import java.util.Map;

import com.lingx.jt808.netty.MyByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
public class Msg0705 extends AbstractMsgHandler implements IJT808MsgHandler{
	

	@Override
	public int getMsgId() {
		return 0x0705;
	}
	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes) throws Exception{
		MyByteBuf mbuff=new MyByteBuf(data);
		Map<String,String> items=new HashMap<>();
		int p1=mbuff.readShort();
		String p2=mbuff.readStringBCD(5);
		while(mbuff.readableBytes()>0) {
			String k=mbuff.readStringBCD(4);
			String v=mbuff.readStringBCD(8);
			items.put(k, v);
		}

			Map<String, Object> map = new HashMap<>();
			map.put("tid", tid);
			map.put("p1", p1);
			map.put("p2", p2);
			map.put("items", items);
			
return sb.toString();		
	}


}
