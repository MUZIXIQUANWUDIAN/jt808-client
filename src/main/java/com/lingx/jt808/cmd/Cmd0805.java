package com.lingx.jt808.cmd;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 车辆注册
 * @author lingx.com
 *
 */
public class Cmd0805 extends AbstractJT808Command {
	
	public Cmd0805(String tid,int msgSn) {
		super(0x0805, tid,getBody(msgSn));
	}
	
	public static byte[] getBody(int msgSn) {
		ByteBuf buff=Unpooled.buffer();
		buff.writeShort(msgSn);
		buff.writeByte(0);
		buff.writeShort(1);
		buff.writeInt(1);
		return returnByteBuf(buff);
	}
	
}
