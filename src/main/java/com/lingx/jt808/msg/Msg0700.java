package com.lingx.jt808.msg;

import java.util.HashMap;
import java.util.Map;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class Msg0700 extends AbstractMsgHandler implements IJT808MsgHandler {

	@Override
	public int getMsgId() {
		return 0x0700;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx, boolean isVersion,byte[] bytes)throws Exception {
		int resMsgSn = data.readShort();
		byte cmd = data.readByte();
		byte temp[] = this.readBytes(data, data.readableBytes());
		Map<String, Object> map = new HashMap<>();
		map.put("tid", tid);
		map.put("p1", cmd);
		map.put("p2", Utils.bytesToHex(temp));

		return sb.toString();
	}

}
