package com.lingx.jt808.msg;

import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import com.lingx.jt808.netty.MyByteBuf;
import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;

public class Msg0100 extends AbstrctMsgHandler implements IJT808MsgHandler {
 
	@Override
	public int getMsgId() {
		return 0x0100;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes12)throws Exception {
		
		int zzcidLen=5,zdxhLen=8,zdidLen=7;//8或20
		if(isVersion) {
			zzcidLen=11;
			zdxhLen=30;
			zdidLen=30;
		}
		MyByteBuf buff = new MyByteBuf(data);
		int sid1 = buff.readUnsignedShort();
		int sid2 = buff.readUnsignedShort();
		String zzcid = buff.readString(zzcidLen);
		String zdxh = buff.readString(zdxhLen);
		String zdid = buff.readString(zdidLen);
		byte color = buff.readByte();

		String carno = "";
		try {
			carno = buff.readStringGBK(buff.readableBytes());
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Map<String,Object> map=new HashMap<>();
		map.put("tid", tid);
		map.put("p0", isVersion?"2019":"2013");
		map.put("p1", sid1);
		map.put("p2", sid2);
		map.put("p3", zzcid);
		map.put("p4", zdxh);
		map.put("p5", zdid);
		map.put("p6", color);
		map.put("p7", carno);

		sb.append("省域ID:").append(sid1).append("\r\n");
		sb.append("市县域ID:").append(sid1).append("\r\n");
		sb.append("制造商ID:").append(zzcid).append("\r\n");
		sb.append("终端型号:").append(zdxh).append("\r\n");
		sb.append("终端ID:").append(zdid).append("\r\n");
		sb.append("车牌颜色:").append(color).append("\r\n");
		sb.append("车牌号码:").append(carno).append("\r\n");
		
		return sb.toString();
	}

}
