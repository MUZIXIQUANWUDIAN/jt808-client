package com.lingx.jt808.msg;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.lingx.jt808.netty.MyByteBuf;
import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class Msg0801 extends AbstrctMsgHandler implements IJT808MsgHandler {
	@Override
	public int getMsgId() {
		return 0x0801;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx, boolean isVersion,byte[] bytes1)throws Exception {
		MyByteBuf mbuff = new MyByteBuf(data);
		int p1 = mbuff.readInt();
		byte p2 = mbuff.readByte();
		byte p3 = mbuff.readByte();
		byte p4 = mbuff.readByte();
		byte p5 = mbuff.readByte();

		
		Map<String, Object> map = new HashMap<>();
		map.put("tid", tid);
		map.put("p1", p1);
		map.put("p2", p2);
		map.put("p3", p3);
		map.put("p4", p4);
		map.put("p5", p5);
		
		long bj=data.readUnsignedInt();
		long zt=data.readUnsignedInt();
		double lat=data.readUnsignedInt()/1000000f;
		double lng=data.readUnsignedInt()/1000000f;
		int height = data.readUnsignedShort();
		int speed = data.readUnsignedShort();
		int fx = data.readUnsignedShort();
		byte[] bytes=new byte[6];
		data.readBytes(bytes);
		String gpstime = "20" +Utils.bytesToHex(bytes);
		map.put("alarm", bj);
		map.put("status", zt);
		map.put("acc", (zt&0b01)>0?"1":"0");
		map.put("lat", lat);
		map.put("lng", lng);
		map.put("height", height);
		map.put("speed", speed/10f);
		map.put("direction", fx);
		map.put("gpstime", gpstime);
		Date date=new Date();
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy/MM/dd");
		String filepath="/photo/"+tid+"/"+sdf.format(date)+"/"+System.currentTimeMillis()+".jpg";
		map.put("filepath", filepath);
		byte[] filedata=mbuff.readBytes(mbuff.readableBytes());
		
		filedata=null;mbuff.release();
		map.clear();map=null;
		return sb.toString();
	}

}
