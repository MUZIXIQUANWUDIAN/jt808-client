package com.lingx.jt808.thread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;

import com.lingx.jt808.utils.JT808Utils;
import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SendVideoThread implements Runnable {

	public SendVideoThread(String ip,int port,String tid,int tdh) {
		this.ip=ip;
		this.port=port;
		this.tid=tid;
		this.tdh=tdh;
	}
	private String ip;
	private int port;
	private String tid;
	private int tdh;
	
	public void readAndSend() {
		BufferedOutputStream bos=null;
		OutputStream output=null;
		Socket socket=null;
		File file=null;
		RandomAccessFile raf=null;
		if(tid.length()>12) {
			tid=tid.substring(tid.length()-12,tid.length());
		}
		String basepath=System.getProperty("user.dir");
		try {
			socket=new Socket(ip,port);
			//socket.setSendBufferSize(1024*1000);
			output=socket.getOutputStream();
			bos=new BufferedOutputStream(output);
			long readLength=0;
			file=new File(basepath+"/plugin/tcpdump.bin");
			//file=new File("D:/aardio/project/jt808/java/tcpdump.bin");
			raf=new RandomAccessFile(file,"r");
			while(raf.length()>readLength) {
				ByteBuf byteBuf=Unpooled.buffer();//
			int head=raf.readInt();
			int num;
			if(head==0x30316364) {
				int hlen=30;
				byteBuf.writeInt(head);
				num=raf.readByte();//4
				byteBuf.writeByte(num);
				int type=raf.readByte();//5
				byteBuf.writeByte(type);
				//System.out.println("负载类型:"+(type&0b01111111));
				int index=raf.readUnsignedShort();
				byteBuf.writeShort(index);
				byte temp[]=new byte[6];
				raf.read(temp);
				byteBuf.writeBytes(Utils.hexToBytes(this.tid));
				byte tdh=raf.readByte();
				byteBuf.writeByte(this.tdh);
				//System.out.println("包序号:"+index+",SIM卡号:"+Utils.bytesToHex(temp)+",通道号:"+tdh);
				num=raf.readByte();//15
				int dataType=num>>4;
				byteBuf.writeByte(num);
				if(dataType!=0b0100) {
					temp=new byte[8];
					raf.read(temp);//时间戳
					byteBuf.writeBytes(temp);
				}else {
					hlen=hlen-8;
				}
				
				if(dataType!=0b0011&&dataType!=0b0100) {
					num=raf.readUnsignedShort();
					byteBuf.writeShort(num);
					num=raf.readUnsignedShort();
					byteBuf.writeShort(num);
				}else {
					hlen=hlen-4;
				}
				int length=raf.readUnsignedShort();
				byteBuf.writeShort(length);
				temp=new byte[length];
				raf.read(temp);
				byteBuf.writeBytes(temp);
				
				temp=new byte[byteBuf.readableBytes()];
				byteBuf.readBytes(temp);
				bos.write(temp);
				//bos.flush();
				
				temp=null;
				byteBuf.release();
				readLength=readLength+length+hlen;
				//System.out.println(raf.length()+" vs "+readLength);
				Thread.currentThread().sleep(10);
			}else {
				//System.out.println("头字节不对");
				readLength=readLength+1;
				raf.seek(readLength);
			}
			
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			JT808Utils.close(raf);
			JT808Utils.close(bos);
			JT808Utils.close(output);
			JT808Utils.close(socket);
			
		}
	}
	
	@Override
	public void run() {
		readAndSend();
	}

	
}
