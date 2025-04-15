package com.lingx.jt808.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

public class Msg0704 extends AbstrctMsgHandler implements IJT808MsgHandler {
	@Resource
	private List<IJT808MsgAttached> listAttached;
	
	@Override
	public int getMsgId() {
		return 0x0704;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes1)throws Exception {
		int len = data.readUnsignedShort();
		byte type = data.readByte();
		List<Map<String,Object>> list=new ArrayList<>();
		//String array[]=new String[len];
		//System.out.println("总记录数:"+len);
		for (int i = 0; i < len&&data.readableBytes()>20; i++) {
			int len2 = data.readUnsignedShort();
			byte[] array2=new byte[len2];
			data.readBytes(array2);
			ByteBuf data2=Unpooled.wrappedBuffer(array2);
			long bj = data2.readUnsignedInt();
			long zt = data2.readUnsignedInt();
			double lat = data2.readUnsignedInt() / 1000000f;
			double lng = data2.readUnsignedInt() / 1000000f;
			int height = data2.readUnsignedShort();
			int speed = data2.readUnsignedShort();
			int fx = data2.readUnsignedShort();
			byte[] bytes = new byte[6];
			data2.readBytes(bytes);
			String gpstime = "20" + Utils.bytesToHex(bytes);

			if(!Utils.isNumber(gpstime))continue;//卫星时间不是全数字，说明此包数据有问题，直接丢了
			Map<String, Object> map = new HashMap<>();
			
			map.put("tid", tid);
			map.put("alarm", bj);
			map.put("status", zt);
			map.put("lat", lat);
			map.put("lng", lng);
			map.put("height", height);
			map.put("speed", speed / 10f);
			map.put("direction", fx);
			map.put("gpstime", gpstime);
			//int aid, alen;
			/*
			 * while (data.readableBytes() > 0) { aid = data.readByte(); alen =
			 * data.readByte(); bytes = new byte[alen]; data.readBytes(bytes); for
			 * (IAttached temp : this.listAttached) { if (temp.getAttachedId() == aid) {
			 * map.put(String.format("A%02x", aid), temp.getValue(bytes,tid)); break; } } }
			 */
			//array[i]=JSON.toJSONString(map);
			list.add(map);
		}
		
		return sb.toString();
	}

}
