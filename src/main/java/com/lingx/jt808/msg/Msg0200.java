package com.lingx.jt808.msg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.lingx.jt808.msg.x0200.A01;
import com.lingx.jt808.msg.x0200.A02;
import com.lingx.jt808.msg.x0200.A03;
import com.lingx.jt808.msg.x0200.A04;
import com.lingx.jt808.msg.x0200.A11;
import com.lingx.jt808.msg.x0200.A12;
import com.lingx.jt808.msg.x0200.A13;
import com.lingx.jt808.msg.x0200.A25;
import com.lingx.jt808.msg.x0200.A2A;
import com.lingx.jt808.msg.x0200.A2B;
import com.lingx.jt808.msg.x0200.A30;
import com.lingx.jt808.msg.x0200.A31;
import com.lingx.jt808.msg.x0200.A50;
import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
public class Msg0200 extends AbstractMsgHandler implements IJT808MsgHandler{

	public Msg0200() {
		listAttached=new ArrayList<>();
		listAttached.add(new A01());
		listAttached.add(new A02());
		listAttached.add(new A03());
		listAttached.add(new A04());
		listAttached.add(new A11());
		listAttached.add(new A12());
		listAttached.add(new A13());
		listAttached.add(new A25());
		listAttached.add(new A2A());
		listAttached.add(new A2B());
		listAttached.add(new A30());
		listAttached.add(new A31());
		listAttached.add(new A50());
	}
	private List<IJT808MsgAttached> listAttached;
	@Override
	public int getMsgId() {
		return 0x0200;
	}
	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes1)throws Exception {
		
		Map<String,Object> map=new HashMap<>();
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
		map.put("type", "0x0200");
		map.put("alarm", bj);
		map.put("status", zt);
		map.put("lat", lat);
		map.put("lng", lng);
		map.put("height", height);
		map.put("speed", speed/10f);
		map.put("direction", fx);
		map.put("gpstime", gpstime);
		map.put("systime", Utils.getTime());
		map.put("ts", System.currentTimeMillis());
		//在这里把网关ID传到处理器
		sb.append("报警标志:").append(bj).append("\r\n");
		sb.append("状态标志:").append(zt).append("\r\n");
		sb.append("经度:").append(lng).append("\r\n");
		sb.append("纬度:").append(lat).append("\r\n");
		sb.append("速度:").append(speed/10f).append("\r\n");
		sb.append("高程:").append(height).append("\r\n");
		sb.append("方向:").append(fx).append("\r\n");
		sb.append("卫星时间:").append(gpstime).append("\r\n");
		int aid,alen;
		boolean b=false;
		while(data.readableBytes()>0) {
			b=false;
			aid=data.readUnsignedByte();
			alen=data.readUnsignedByte();
			bytes=new byte[alen];
			if(data.readableBytes()<alen) {break;}
			data.readBytes(bytes);
			for(IJT808MsgAttached temp:this.listAttached) {
				if(temp.getAttachedId()==aid) {
					map.put(String.format("A%02X", aid), temp.getValue(bytes,tid,ctx));
					b=true;
					sb.append(String.format("附加消息0x%02X", aid)).append(":").append(temp.getValue(bytes,tid,ctx)).append("\r\n");
					break;
				}
			}
			if(!b) {
				sb.append(String.format("附加消息0x%02X", aid)).append(":").append(Utils.bytesToHex(bytes)).append("\r\n");
				
			}
		}
		return sb.toString();
		
	}
	public void setListAttached(List<IJT808MsgAttached> listAttached) {
		this.listAttached = listAttached;
	}
	
	
}
