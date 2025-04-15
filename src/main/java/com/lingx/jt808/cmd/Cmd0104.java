package com.lingx.jt808.cmd;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

/**
 * 查询参数回复
 * @author lingx.com
 *
 */
public class Cmd0104 extends AbstractJT808Command {

	public Cmd0104(String tid,int id,String type,String value,int msgSn) {
		super(0x0104, tid,getBody(tid,id,type,value,msgSn));
	}
	
	public static byte[] getBody(String tid,int id,String type,String value,int msgSn) {
		ByteBuf buff=Unpooled.buffer();
		buff.writeShort(msgSn);
		buff.writeByte(1);
		buff.writeInt(id);
		switch(type) {
		case "BYTE":
			buff.writeByte(1);
			buff.writeByte(Integer.parseInt(value));
			break;
		case "WORD":
			buff.writeByte(2);
			buff.writeShort(Integer.parseInt(value));
			break;
		case "DWORD":
			buff.writeByte(4);
			buff.writeInt(Integer.parseInt(value));
			break;
		case "STRING":
			buff.writeByte(value.length());
			buff.writeBytes(value.getBytes());
			break;
		case "HEXSTRING":
				buff.writeByte(Utils.hexToBytes(value).length);
				buff.writeBytes(Utils.hexToBytes(value));
				break;
		}
		return returnByteBuf(buff);
	}
	
	public static void writeString(ByteBuf buff,String str,int length) {
		byte array[]=new byte[length];
		byte temp[]=null;
		try {
			temp=str.getBytes("GBK");
		} catch (Exception e) {
			temp=str.getBytes();
		}
		//System.out.println(Utils.bytesToHex(temp));
		for(int i=0;i<array.length&&i<temp.length;i++) {
			array[i]=temp[i];
		}
		//System.out.println(Utils.bytesToHex(array));
		buff.writeBytes(array);
	}
}
