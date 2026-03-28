package com.lingx.jt808.msg;

import java.util.HashMap;
import java.util.Map;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
/**
 * 终端升级结果应答
 * @author lingx.com
 *
 */
public class Msg0108 extends AbstractMsgHandler implements IJT808MsgHandler {
	
 
	@Override
	public int getMsgId() {
		return 0x0108;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes)throws Exception {
		byte type=data.readByte();
		byte ret=data.readByte();
		
		Map<String, Object> map = new HashMap<>();
		map.put("tid", tid);
		map.put("type", type);
		map.put("ret", ret);
		return sb.toString();
		
	}

}
