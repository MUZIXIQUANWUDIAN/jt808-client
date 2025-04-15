package com.lingx.jt808.msg;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
public class Msg0500 extends AbstrctMsgHandler implements IJT808MsgHandler{
	@Resource
	private List<IJT808MsgAttached> listAttached;
	

	@Override
	public int getMsgId() {
		return 0x0500;
	}
	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes1)throws Exception {
		Map<String,Object> map=new HashMap<>();
		int resMsgSn=data.readShort();
		//String key=tid+"_"+0x8500+"_"+resMsgSn;
		//JT808Cache.RES_CACHE.put(key, new RetBean(0));
		
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
		map.put("tid", tid);
		map.put("alarm", bj);
		map.put("status", zt);
		map.put("lat", lat);
		map.put("lng", lng);
		map.put("height", height);
		map.put("speed", speed/10f);
		map.put("direction", fx);
		map.put("gpstime", gpstime);
		sb.append("经度:").append(lng).append("\r\n");
		sb.append("纬度:").append(lat).append("\r\n");
		sb.append("速度:").append(speed/10f).append("\r\n");
		sb.append("高程:").append(height).append("\r\n");
		sb.append("方向:").append(fx).append("\r\n");
		sb.append("卫星时间:").append(gpstime).append("\r\n");
		int aid,alen;
		while(data.readableBytes()>0) {
			aid=data.readByte();
			alen=data.readByte();
			bytes=new byte[alen];
			data.readBytes(bytes);
			for(IJT808MsgAttached temp:this.listAttached) {
				if(temp.getAttachedId()==aid) {
					map.put(String.format("A%02x", aid), temp.getValue(bytes,tid,ctx));
					sb.append(String.format("A%02X", aid)).append(":").append(temp.getValue(bytes,tid,ctx)).append("\r\n");
					
					break;
				}
			}
		}
		
		return sb.toString();
	}


}
