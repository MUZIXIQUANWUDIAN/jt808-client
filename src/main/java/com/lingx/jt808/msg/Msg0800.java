package com.lingx.jt808.msg;

import java.util.HashMap;
import java.util.Map;

import com.lingx.jt808.netty.MyByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class Msg0800 extends AbstrctMsgHandler implements IJT808MsgHandler {

	@Override
	public int getMsgId() {
		return 0x0800;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx, boolean isVersion,byte[] bytes) throws Exception{
		MyByteBuf mbuff = new MyByteBuf(data);
		int p1 = mbuff.readInt();
		byte p2 = mbuff.readByte();
		byte p3 = mbuff.readByte();
		byte p4 = mbuff.readByte();
		byte p5 = mbuff.readByte();

		Map<String, Object> map = new HashMap<>();
		map.put("tid", tid);
		map.put("p1", p1);
		map.put("p2", p2);
		map.put("p3", p3);
		map.put("p4", p4);
		map.put("p5", p5);

		return sb.toString();
	}

}
