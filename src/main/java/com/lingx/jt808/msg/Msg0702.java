package com.lingx.jt808.msg;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.lingx.jt808.netty.MyByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
public class Msg0702 extends AbstrctMsgHandler implements IJT808MsgHandler{
	

	@Override
	public int getMsgId() {
		return 0x0702;
	}
	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes)throws Exception {
		MyByteBuf mbuff=new MyByteBuf(data);

		try {
			byte p1=mbuff.readByte();
			String p2="20"+mbuff.readStringBCD(6);
			int p3=mbuff.readByte();
			int len=mbuff.readByte();
			String p4=mbuff.readStringGBK(len);
			String p5=mbuff.readString(20);
			 len=mbuff.readByte();
			String p6=mbuff.readStringGBK(len);
			String p7=mbuff.readStringBCD(4);
			String p8=mbuff.readString(mbuff.readableBytes());

			Map<String, Object> map = new HashMap<>();
			map.put("tid", tid);
			map.put("p1", p1);
			map.put("p2", p2);
			map.put("p3", p3);
			map.put("p4", p4);
			map.put("p5", p5);
			map.put("p6", p6);
			map.put("p7", p7);
			map.put("p8", p8);
			
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return sb.toString();
	}


}
