package com.lingx.jt808.cmd;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class Cmd1205  extends AbstractJT808Command{

	public Cmd1205(String tid,int msgSn,int tdh,String stime,String etime,int type,int mltype,int ccqtype) {
		super(0x1205, tid,getBody(msgSn,tdh,stime,etime,type,mltype,ccqtype));
	}
	
	public static byte[] getBody(int msgSn,int tdh,String stime,String etime,int type,int mltype,int ccqtype) {
		ByteBuf buff=Unpooled.buffer();
		//MyByteBuf mbb3=new MyByteBuf(buff);
		buff.writeShort(msgSn);
		buff.writeInt(1);
		if(tdh==0)tdh=1;
		buff.writeByte(tdh);
		buff.writeBytes(Utils.hexToBytes(stime));
		buff.writeBytes(Utils.hexToBytes(etime));
		buff.writeLong(0);
		buff.writeByte(type);
		buff.writeByte(mltype);
		buff.writeByte(ccqtype);

		buff.writeInt(512*1024*1024);

		return returnByteBuf(buff); 
	}
}
