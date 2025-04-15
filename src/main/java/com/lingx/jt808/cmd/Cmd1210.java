package com.lingx.jt808.cmd;

import java.io.File;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Cmd1210  extends AbstractJT808Command  {

	public Cmd1210( String tid, String bjbsh,String bjbh,String filenames[],String dirpath) {
		super(0x1210, tid, getBody(bjbsh,bjbh,filenames, dirpath));
	}
	public static byte[] getBody(String bjbsh,String bjbh,String filenames[],String dirpath) {
		ByteBuf buff=Unpooled.buffer();
		buff.writeBytes("1234567".getBytes());
		buff.writeBytes(Utils.hexToBytes(bjbsh));
		buff.writeBytes(bjbh.getBytes());
		buff.writeByte(0);
		
		buff.writeByte(filenames.length);
		for(String temp:filenames) {
			buff.writeByte(temp.length());
			buff.writeBytes(temp.getBytes());
			long len=new File(dirpath+temp).length();
			buff.writeInt(new Long(len).intValue());
		}
		return returnByteBuf(buff); 
	}
}
