package com.lingx.jt808.thread;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.net.Socket;
import java.util.Arrays;

import com.lingx.jt808.cmd.Cmd1210;
import com.lingx.jt808.cmd.Cmd1211;
import com.lingx.jt808.cmd.Cmd1212;
import com.lingx.jt808.utils.JT808Utils;
import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public class SendAdasFileThread implements Runnable {
	public SendAdasFileThread(String tid,String ip,int port,String bjbsh,String bjbh) {
		this.tid=tid;
		this.ip=ip;
		this.port=port;
		this.bjbsh=bjbsh;
		this.bjbh=bjbh;
	}
	private String tid;
	private String ip="";
	private int port=0;
	private String bjbsh;
	private String bjbh;
	@Override
	public void run() {
		String filenames[] =null;
		String basepath=System.getProperty("user.dir");
		//String basepath="D:\\aardio\\project\\jt808";
		String type=bjbsh.endsWith("A")?"adas":"dsm";
		String path=basepath+"/plugin/"+type+"/";
		if("adas".equals(type)) {
			filenames= new String[]{"02_64_6403_00_219236b4045348c8bd1e2807ad1e3e64.mp4", "00_64_6403_00_219236b4045348c8bd1e2807ad1e3e64.jpg", "00_64_6403_01_219236b4045348c8bd1e2807ad1e3e64.jpg", "00_64_6403_02_219236b4045348c8bd1e2807ad1e3e64.jpg"};
		}else {
			filenames= new String[]{"02_65_6503_00_94c782f4d93540fe9c45e601254b1249.mp4", "00_65_6503_00_94c782f4d93540fe9c45e601254b1249.jpg", "00_65_6503_01_94c782f4d93540fe9c45e601254b1249.jpg", "00_65_6503_02_94c782f4d93540fe9c45e601254b1249.jpg"};
		}
		Cmd1210 cmd=new Cmd1210(tid,bjbsh,bjbh,filenames,path);
		
		BufferedOutputStream bos=null;
		OutputStream output=null;
		InputStream input=null;
		Socket socket=null;
		byte data[]=new byte[1024];
		try {
			System.out.println("连接主动安全附件服务器:"+ip+":"+port);
			socket=new Socket(ip,port);
			output=socket.getOutputStream();
			input=socket.getInputStream();
			bos=new BufferedOutputStream(output);
			bos.write(cmd.toMessageBytes());
			
			for(String filename:filenames) {
				System.out.println(path+filename);
				File file=new File(path+filename);
				Cmd1211 cmd2=new Cmd1211(tid,filename,Integer.parseInt(filename.substring(0,2)),file.length());
				bos.write(cmd2.toMessageBytes());
				bos.flush();
				input.read(data);
				System.out.println(Utils.bytesToHex(data));
				sendFile(bos,file,filename);

				Cmd1212 cmd3=new Cmd1212(tid,filename,Integer.parseInt(filename.substring(0,2)),file.length());
				bos.write(cmd3.toMessageBytes());
				bos.flush();
				input.read(data);
				System.out.println(Utils.bytesToHex(data));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}finally {
			JT808Utils.close(bos);
			JT808Utils.close(input);
			JT808Utils.close(output);
			JT808Utils.close(socket);
			
		}
	}

	public void sendFile(BufferedOutputStream bos,File file,String filename) {
		RandomAccessFile raf=null;
		byte data[]=new byte[50000];
		try {
		raf=new RandomAccessFile(file,"r");
		for(int pack=0;pack<file.length();pack+=50000) {
			byte bytes[]=new byte[50];
			byte filenamebytes[]=filename.getBytes();
			for(int i=0;i<filenamebytes.length;i++) {
				bytes[i]=filenamebytes[i];
			}
			ByteBuf buff=Unpooled.buffer();
			buff.writeBytes(new byte[] {0x30, 0x31, 0x63, 0x64});
			buff.writeBytes(bytes);//写入文件名
			buff.writeInt(pack);//写入偏移量
			int len=raf.read(data);
			buff.writeInt(len);//数据长度
			buff.writeBytes(data, 0, len);//写入文件数据
			
			//发送文件报文
			byte[] cmddata=new byte[buff.readableBytes()];
			buff.readBytes(cmddata);
			System.out.println("发送报文长度:"+cmddata.length);
			System.out.println(Utils.bytesToHex(Arrays.copyOfRange(cmddata, 0, 100)));
			bos.write(cmddata);
			bos.flush();
			//释放
			buff.release();
			cmddata=null;
		}
		}catch(Exception e) {
			e.printStackTrace();
		}finally {
			JT808Utils.close(raf);
		}
	}
}
