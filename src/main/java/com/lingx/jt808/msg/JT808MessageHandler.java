package com.lingx.jt808.msg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;

public class JT808MessageHandler {
  private List<IJT808MsgHandler> handlers=new ArrayList<>();
  public JT808MessageHandler() {
	  handlers.add(new Msg0001());
	  handlers.add(new Msg0002());
	  handlers.add(new Msg0003());
	  handlers.add(new Msg0100());
	  handlers.add(new Msg0102());
	  handlers.add(new Msg0104());
	  handlers.add(new Msg0107());
	  handlers.add(new Msg0108());
	  handlers.add(new Msg0200());
	  handlers.add(new Msg0201());
	  handlers.add(new Msg0500());
	  handlers.add(new Msg0700());
	  handlers.add(new Msg0701());
	  handlers.add(new Msg0702());
	  handlers.add(new Msg0704());
	  handlers.add(new Msg0705());
	  handlers.add(new Msg0800());
	  handlers.add(new Msg0801());
	  handlers.add(new Msg0805());
	  handlers.add(new Msg8103());
	  handlers.add(new Msg1205());
	  handlers.add(new Msg1206());
  }
  
  public String handler(String hexstring)throws Exception {
	  byte data[]=Utils.hexToBytes(hexstring);
	  String ret="";
	  int msgId=Utils.byteArrayToInt(Arrays.copyOfRange(data, 1, 3));
	  for(IJT808MsgHandler handler:this.handlers) {
		  if(msgId==handler.getMsgId()) {
			 ByteBuf bytebuf= handler.getBody(data);
			 ret=handler.handle(bytebuf, "", 0, 0, null, false, data);
			 handler.reset();
		  }
	  }
	  if(Utils.isNull(ret)) {
		  Msg0002 msg=new Msg0002();
		  ret= msg.handle(msg.getBody(data), ret, msgId, msgId, null, false, data);
	  }
	  return ret;
  }
}
