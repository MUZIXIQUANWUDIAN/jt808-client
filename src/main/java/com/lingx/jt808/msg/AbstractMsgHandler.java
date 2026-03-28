package com.lingx.jt808.msg;

import com.lingx.jt808.netty.MyByteBuf;
import com.lingx.jt808.utils.JT808Utils;
import com.lingx.jt808.utils.Utils;

import io.netty.buffer.ByteBuf;

public abstract class AbstractMsgHandler {

	protected StringBuilder sb = new StringBuilder();

	public byte[] readBytes(ByteBuf byteBuf, int length) {
		byte[] temp = new byte[length];
		byteBuf.readBytes(temp);
		return temp;
	}

	public ByteBuf getBody(byte data[]) throws Exception {
		MyByteBuf buff = new MyByteBuf(JT808Utils.decode(data));
		buff.readByte();

		int msgId = buff.readUnsignedShort();
		int length = buff.readUnsignedShort();
		String tid = "";

		boolean isFB = (length & 0b0010000000000000) > 0;
		boolean isVersion = (length & 0b0100000000000000) > 0;
		if (isVersion) {
			buff.readByte();
			tid = buff.readStringBCD(10);
		} else {
			tid = buff.readStringBCD(6);
		}
		int msgSn = buff.readUnsignedShort();

		length = length & 0x3ff;
		ByteBuf content = null;
		if (isFB) {
			throw new Exception("该报文是分包数据，单包不能解析");
		} else {
			content = buff.readByteBuf(length);
		}

		byte check = buff.readByte();
		buff.readByte();
		byte temp[] = new byte[content.readableBytes()];
		content.getBytes(0, temp);

		sb.append("设备号:").append(tid).append("\r\n");
		sb.append("消息ID:").append(String.format("0x%04X", msgId)).append("\r\n");
		sb.append("消息长度:").append(length).append("\r\n");
		sb.append("消息流水号:").append(msgSn).append("\r\n");
		sb.append("消息正文:").append(Utils.bytesToHex(temp)).append("\r\n");
		sb.append("检验码:").append(String.format("0x%02X", check)).append("\r\n");
		return content;
	}

	public void reset() {
		sb = new StringBuilder();
	}
}
