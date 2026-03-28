package com.lingx.jt808.thread;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import com.lingx.jt808.JT808ClientContext;
import com.lingx.jt808.cmd.Cmd0801;
import com.lingx.jt808.netty.MyByteBuf;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SendImageThread implements Runnable {
	public static byte[] WZ_DATA=new byte[28];
	private final JT808ClientContext session;

	public SendImageThread(String tid, JT808ClientContext session) {
		this.tid=tid;
		this.session = session;
	}
	private String tid;
	@Override
	public void run() {
		FileInputStream fis=null;
		String basepath=System.getProperty("user.dir");
		ByteBuf byteBuf=Unpooled.buffer();
		try {
			File file=new File(basepath+"/plugin/123456.jpg");
			fis=new FileInputStream(file);
			byte buff[]=new byte[900];
			int max = new Long(file.length()/900 + 1).intValue();
			int ind = 1,len;
			for(int i=1;i<=max;i++) {
				if (ind == 1) {
					byteBuf.writeInt(01);
					byteBuf.writeByte(00);
					byteBuf.writeByte(00);
					byteBuf.writeByte(00);
					byteBuf.writeByte(01);
					byteBuf.writeBytes(WZ_DATA);//写入位置信息
				}
				
				len=fis.read(buff);
				byteBuf.writeBytes(buff, 0, len);
				MyByteBuf mbb=new MyByteBuf(byteBuf);
				Cmd0801 cmd=new Cmd0801(tid,mbb.readBytes(mbb.readableBytes()),max,ind);
				session.sendMessage(cmd.toMessageBytes());
				ind++;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			try {
				if(fis!=null)
					fis.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

}
