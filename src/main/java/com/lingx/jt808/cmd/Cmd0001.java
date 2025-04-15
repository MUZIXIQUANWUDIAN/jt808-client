package com.lingx.jt808.cmd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 通用应签
 * @author lingx.com
 *
 */
public class Cmd0001 extends AbstractJT808Command {

	public Cmd0001(String tid,int msgSn,int msgId) {
		this(tid,msgSn,msgId,0);
	}
	public Cmd0001(String tid,int msgSn,int msgId,int ret) {
		super(0x0001, tid,getBody(msgSn,msgId,ret));
	}
	
	public static byte[] getBody(int msgSn,int msgId,int ret) {
		ByteBuf buff=Unpooled.buffer();
		buff.writeShort(msgSn);
		buff.writeShort(msgId);
		buff.writeByte(ret);
		return returnByteBuf(buff);
	}
	
}
