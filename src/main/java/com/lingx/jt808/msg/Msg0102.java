package com.lingx.jt808.msg;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
/**
 * 终端鉴权
 * @author lingx.com
 *
 */
public class Msg0102 extends AbstractMsgHandler implements IJT808MsgHandler {
	
 
	@Override
	public int getMsgId() {
		return 0x0102;
	}

	@Override
	public String handle(ByteBuf data, String tid, int msgId, int msgSn, ChannelHandlerContext ctx,boolean isVersion,byte[] bytes1)throws Exception {
		String code="";
		if(isVersion) {
			int len=data.readByte();
			byte bytes[]=new byte[len];
			data.readBytes(bytes);
			code=new String(bytes);
			//还有15字节的IMEI与20字节的软件版本号
		}else {
			byte bytes[]=new byte[data.readableBytes()];
			data.readBytes(bytes);
			code=new String(bytes);
		}

		sb.append("终端鉴权码:").append(code).append("\r\n");
		//System.out.println("0102_CODE:"+code+",tid:"+tid);
		//System.out.println(Utils.bytesToHex(bytes1));
		return sb.toString();
	}

}
