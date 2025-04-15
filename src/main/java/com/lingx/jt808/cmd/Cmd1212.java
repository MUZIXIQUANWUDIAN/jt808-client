package com.lingx.jt808.cmd;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Cmd1212  extends AbstractJT808Command  {

	public Cmd1212(String tid,String filename,int type,long length) {
		super(0x1212, tid, getBody(filename,type,length));
	}
	public static byte[] getBody(String filename,int type,long length) {
		ByteBuf buff=Unpooled.buffer();
		
		buff.writeByte(filename.length());
		buff.writeBytes(filename.getBytes());
		buff.writeByte(type);
		buff.writeInt(new Long(length).intValue());
		
		return returnByteBuf(buff); 
	}
}
