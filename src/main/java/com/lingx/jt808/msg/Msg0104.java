package com.lingx.jt808.msg;

import java.util.HashMap;
import java.util.Map;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
/**
 * 查询终端参数应答
 * @author lingx.com
 *
 */
public class Msg0104 extends AbstrctMsgHandler implements IJT808MsgHandler {
	
 
	@Override
	public int getMsgId() {
		return 0x0104;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes)throws Exception {
		int resMsgSn=data.readShort();
		int len=data.readByte();
		Map<Integer,String> map=new HashMap<>();
		for(int i=0;i<len;i++) {
			int id=data.readInt();
			int l=data.readByte();
			byte temp[]=new byte[l];
			data.readBytes(temp);
			map.put(id, Utils.bytesToHex(temp));

			sb.append("参数"+id+":").append(Utils.bytesToHex(temp)).append("\r\n");
		}
		
		//String key=tid+"_"+resMsgSn;
		//String value=JSON.toJSONString(map);
		return sb.toString();
	}

}
