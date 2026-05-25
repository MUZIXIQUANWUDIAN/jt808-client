package com.lingx.jt808.netty;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.lingx.jt808.JT808ClientContext;
import com.lingx.jt808.cmd.Cmd0001;
import com.lingx.jt808.cmd.Cmd0104;
import com.lingx.jt808.cmd.Cmd0201;
import com.lingx.jt808.cmd.Cmd0805;
import com.lingx.jt808.cmd.Cmd1205;
import com.lingx.jt808.thread.SendAdasFileThread;
import com.lingx.jt808.thread.SendImageThread;
import com.lingx.jt808.thread.SendVideoThread;
import com.lingx.jt808.utils.JT808Utils;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.PropUtils;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class TcpClientHandler extends SimpleChannelInboundHandler<byte[]> {

	/** 共享业务线程池，避免每个视频/图片/附件任务都创建新线程 */
	private static final ExecutorService businessPool = Executors.newFixedThreadPool(
			Runtime.getRuntime().availableProcessors());

	private final JT808ClientContext session;

	public TcpClientHandler(JT808ClientContext session) {
		this.session = session;
	}

	/** 0x8201 应答组包用；空串会触发 {@code For input string: ""}，此处标出配置键名。 */
	private static double propDouble(String key) {
		String v = PropUtils.getProp(key);
		if (v == null || v.trim().isEmpty()) {
			throw new IllegalArgumentException("配置项未设置或为空: " + key + "，请在「位置设置」中填写并保存。");
		}
		try {
			return Double.parseDouble(v.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("配置项无法解析为数字: " + key + "，当前值=[" + v + "]", e);
		}
	}

	private static float propFloat(String key) {
		String v = PropUtils.getProp(key);
		if (v == null || v.trim().isEmpty()) {
			throw new IllegalArgumentException("配置项未设置或为空: " + key + "，请在「位置设置」中填写并保存。");
		}
		try {
			return Float.parseFloat(v.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("配置项无法解析为数字: " + key + "，当前值=[" + v + "]", e);
		}
	}

	private static int propInt(String key) {
		String v = PropUtils.getProp(key);
		if (v == null || v.trim().isEmpty()) {
			throw new IllegalArgumentException("配置项未设置或为空: " + key + "，请在「位置设置」中填写并保存。");
		}
		try {
			return Integer.parseInt(v.trim());
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("配置项无法解析为整数: " + key + "，当前值=[" + v + "]", e);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, byte[] arg1) throws Exception {
		String hexstring = Utils.bytesToHex(arg1);

		ByteBuf decoded = JT808Utils.decode(arg1);
		try {
			parseDownstream(decoded, hexstring);
		} finally {
			decoded.release();
		}
	}

	private void parseDownstream(ByteBuf decoded, String rawHex) throws Exception {
		MyByteBuf buff = new MyByteBuf(decoded);
		buff.readByte();
		int msgId = buff.readUnsignedShort();
		int length = buff.readUnsignedShort();
		String tid = "";
		boolean isFB = (length & 0b0010000000000000) > 0;// 是否分包
		boolean isVersion = (length & 0b0100000000000000) > 0;// 是否版本标识
		if (isVersion) {
			buff.readByte();
			tid = buff.readStringBCD(10);
		} else {
			tid = buff.readStringBCD(6);
		}
		int msgSn = buff.readUnsignedShort();// 消息流水号
		length = length & 0x3ff;
		ByteBuf content = buff.readByteBuf(length);
		byte[] contentBytes = new byte[content.readableBytes()];
		content.getBytes(content.readerIndex(), contentBytes);
		String contentHex = Utils.bytesToHex(contentBytes);
		byte check = buff.readByte();
		buff.readByte();
		if (msgId == 0x8001) {
			log8001Summary(contentBytes);
		} else {
			session.addMessageRes(rawHex);
			session.callAardio(String.format("收到平台指令: 0x%04X,长度:%d", msgId, length));
			String explain = describeDownstreamInstruction(msgId, contentBytes);
			if (explain != null && !explain.isEmpty()) {
				session.callAardio("平台指令说明: " + explain);
			}
		}
		switch(msgId) {
		case 0x8001:
			session.send0x0200();
			break;
		case 0x8100:
			session.send0x0102();
			break;
		case 0x8103:
			Cmd0001 cmd0001=new Cmd0001(tid,msgSn,msgId);
			session.sendMessage(cmd0001.toMessageBytes());
			break;
		case 0x8105:
			session.callAardio("收到网页下发[引擎控制] 0x8105, HEX=" + contentHex + ", " + describe8105(contentBytes));
			Cmd0001 cmd8105 = new Cmd0001(tid, msgSn, msgId);
			session.sendMessage(cmd8105.toMessageBytes());
			break;
		case 0x8106:
			content.readByte();
			int id=content.readInt();
			if(id==0x0075) {
				Cmd0104 cmd=new Cmd0104(tid,0x75,"HEXSTRING","010100640F00000100000500281900000400002F01",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0001) {
				Cmd0104 cmd=new Cmd0104(tid,0x01,"BYTE","30",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0013) {
				Cmd0104 cmd=new Cmd0104(tid,0x0013,"STRING","127.0.0.1",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0017) {
				Cmd0104 cmd=new Cmd0104(tid,0x0017,"STRING","127.0.0.1",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0018) {
				Cmd0104 cmd=new Cmd0104(tid,0x0018,"WORD","8808",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0019) {
				Cmd0104 cmd=new Cmd0104(tid,0x0019,"WORD","8808",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0027) {
				Cmd0104 cmd=new Cmd0104(tid,0x0027,"WORD","30",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0029) {
				Cmd0104 cmd=new Cmd0104(tid,0x0029,"WORD","60",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0055) {
				Cmd0104 cmd=new Cmd0104(tid,0x0055,"WORD","120",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0056) {
				Cmd0104 cmd=new Cmd0104(tid,0x0056,"WORD","10",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}else if(id==0x0057) {
				Cmd0104 cmd=new Cmd0104(tid,0x0057,"WORD","6000",msgSn);
				session.sendMessage(cmd.toMessageBytes());
			}
			break;
		case 0x8201:
		{
			double lat;double lng;float speed;int height;int fx;float meilage;float oil;int bj;int zt;
			lat = propDouble("jt808.0x0200.lat");
			lng = propDouble("jt808.0x0200.lng");
			speed = propFloat("jt808.0x0200.speed");
			height = propInt("jt808.0x0200.height");
			fx = propInt("jt808.0x0200.direction");
			meilage = propFloat("jt808.0x0200.mileage");
			oil = propFloat("jt808.0x0200.oil");
			bj=0;
			if("true".equals(PropUtils.getProp("jt808.0x0200.bj0")))bj+=1;
			if("true".equals(PropUtils.getProp("jt808.0x0200.bj1")))bj+=2;
			if("true".equals(PropUtils.getProp("jt808.0x0200.bj2")))bj+=4;
			zt=0;
			if("true".equals(PropUtils.getProp("jt808.0x0200.zt0")))zt+=1;
			if("true".equals(PropUtils.getProp("jt808.0x0200.zt1")))zt+=2;
			Cmd0201 cmd0201=new Cmd0201(tid,msgSn,lat, lng, speed, height, fx, meilage, oil, bj, zt);
			session.sendMessage(cmd0201.toMessageBytes());
		}
			break;
		case 0x8801:
			SendImageThread sit=new SendImageThread(tid, session);
			businessPool.submit(sit);
			Cmd0805 cmd0805=new Cmd0805(tid,msgSn);
			session.sendMessage(cmd0805.toMessageBytes());
			break;
		case 0x9101:
		{
			int len=content.readByte();
			MyByteBuf mbb=new MyByteBuf(content);
			String ip=mbb.readString(len);
			int port=mbb.readUnsignedShort();
			mbb.readUnsignedShort();
			int tdh=mbb.readByte();

			session.callAardio(String.format("实时视频服务器：%s:%s,tid:%s-%s",ip,port,tid,tdh));
			SendVideoThread svt=new SendVideoThread(ip,port,tid,tdh);
			businessPool.submit(svt);
		}
			break;
		case 0x9201:
		{
			int len=content.readByte();
			MyByteBuf mbb=new MyByteBuf(content);
			String ip=mbb.readString(len);
			int port=mbb.readUnsignedShort();
			mbb.readUnsignedShort();
			int tdh=mbb.readByte();

			session.callAardio(String.format("回放视频服务器：%s:%s,tid:%s-%s",ip,port,tid,tdh));
			SendVideoThread svt=new SendVideoThread(ip,port,tid,tdh);
			businessPool.submit(svt);
		}
			break;
		case 0x9205:
			MyByteBuf mbb3=new MyByteBuf(content);
			int tdh3=mbb3.readByte();
			String stime=mbb3.readStringBCD(6);
			String etime=mbb3.readStringBCD(6);
			long bj=mbb3.getBytebuf().readLong();
			int type=mbb3.readByte();
			int mltype=mbb3.readByte();
			int ccqtype=mbb3.readByte();
			Cmd1205 cmd1205=new Cmd1205(tid,msgSn,tdh3,stime,etime,type,mltype,ccqtype);
			session.sendMessage(cmd1205.toMessageBytes());
			break;
		case 0x9208://上传苏标附件
			int len2=content.readByte();
			MyByteBuf mbb2=new MyByteBuf(content);
			String ip2=mbb2.readString(len2);
			int port2=mbb2.readUnsignedShort();
			int port21=mbb2.readUnsignedShort();
			String bjbsh=mbb2.readStringBCD(16);
			String bjbh=mbb2.readString(32);
			businessPool.submit(new SendAdasFileThread(tid,ip2,port2,bjbsh,bjbh));
			break;
		case 0x8300:
			session.callAardio("收到网页下发[自定义指令] 0x8300, HEX=" + contentHex);
			try {
				session.callAardio("0x8300 文本预览(GBK): " + new String(contentBytes, "GBK"));
			} catch (Exception ignored) {
			}
			Cmd0001 cmd8300 = new Cmd0001(tid, msgSn, msgId);
			session.sendMessage(cmd8300.toMessageBytes());
			break;
		case 0x8301:
			session.callAardio("收到网页下发[文本信息] 0x8301, HEX=" + contentHex);
			try {
				session.callAardio("0x8301 文本预览(GBK): " + new String(contentBytes, "GBK"));
			} catch (Exception ignored) {
			}
			Cmd0001 cmd8301 = new Cmd0001(tid, msgSn, msgId);
			session.sendMessage(cmd8301.toMessageBytes());
			break;
		case 0x0310:
			session.callAardio("收到平台参数下发[0x0310], HEX=" + contentHex);
			log0310Details(contentBytes);
			Cmd0001 cmd0310 = new Cmd0001(tid, msgSn, msgId);
			session.sendMessage(cmd0310.toMessageBytes());
			break;
		default:
			session.callAardio(String.format("收到未专门处理的平台指令: 0x%04X, HEX=%s, 自动回通用应答", msgId, contentHex));
			Cmd0001 cmd00011=new Cmd0001(tid,msgSn,msgId);
			session.sendMessage(cmd00011.toMessageBytes());
		}
	}

	private void log0310Details(byte[] body) {
		if (body == null || body.length == 0) {
			session.callAardio("0x0310 消息体为空");
			return;
		}
		session.callAardio("0x0310 文本预览(UTF-8): " + previewText(body, StandardCharsets.UTF_8.name()));
		try {
			session.callAardio("0x0310 文本预览(GBK): " + previewText(body, "GBK"));
		} catch (Exception ignored) {
		}

		// 多格式自动识别：优先选择“完整解析且剩余最少”的结构
		ParseResult p1 = tryParseKv(body, 1);
		ParseResult p2 = tryParseKv(body, 2);
		ParseResult p4 = tryParseKv(body, 4);
		ParseResult best = pickBest(p1, p2, p4);
		session.callAardio(best.message);
	}

	private ParseResult pickBest(ParseResult... results) {
		ParseResult best = null;
		for (ParseResult r : results) {
			if (r == null) {
				continue;
			}
			if (best == null) {
				best = r;
				continue;
			}
			// 优先：解析条目更多；其次：剩余字节更少
			if (r.parsedItems > best.parsedItems
					|| (r.parsedItems == best.parsedItems && r.remainBytes < best.remainBytes)) {
				best = r;
			}
		}
		return best;
	}

	private ParseResult tryParseKv(byte[] body, int idBytes) {
		int count = body[0] & 0xFF;
		int offset = 1;
		int parsed = 0;
		StringBuilder sb = new StringBuilder();
		sb.append("0x0310 参数项解析(idBytes=").append(idBytes).append("): count=")
				.append(count).append(", bodyLen=").append(body.length);
		for (int i = 0; i < count; i++) {
			int headerLen = idBytes + 1;
			if (offset + headerLen > body.length) {
				sb.append(" | 第").append(i + 1).append("项头部不完整(offset=").append(offset).append(")");
				break;
			}
			long id = 0;
			for (int b = 0; b < idBytes; b++) {
				id = (id << 8) | (body[offset + b] & 0xFF);
			}
			int len = body[offset + idBytes] & 0xFF;
			offset += headerLen;
			if (offset + len > body.length) {
				sb.append(" | 第").append(i + 1).append("项长度异常(id=0x")
						.append(formatId(id, idBytes)).append(",len=").append(len)
						.append(",remain=").append(body.length - offset).append(")");
				break;
			}
			byte[] value = Arrays.copyOfRange(body, offset, offset + len);
			offset += len;
			parsed++;
			sb.append(" | [").append(parsed).append("]id=0x").append(formatId(id, idBytes))
					.append(",len=").append(len)
					.append(",hex=").append(Utils.bytesToHex(value))
					.append(",txt=").append(previewText(value, "GBK"))
					.append(",ascii=").append(previewBytesVisible(value));
			String semantic = describe0310Param(idBytes, id, value);
			if (semantic != null && !semantic.isEmpty()) {
				sb.append(",semantic=").append(semantic);
			}
		}
		int remain = Math.max(0, body.length - offset);
		if (parsed == 0) {
			sb.append(" | 未成功解析参数项");
		}
		if (remain > 0) {
			byte[] tail = Arrays.copyOfRange(body, offset, body.length);
			sb.append(" | 剩余数据HEX=").append(Utils.bytesToHex(tail));
		}
		return new ParseResult(parsed, remain, sb.toString());
	}

	private String formatId(long id, int idBytes) {
		if (idBytes == 1) {
			return String.format("%02X", id);
		}
		if (idBytes == 2) {
			return String.format("%04X", id);
		}
		return String.format("%08X", id);
	}

	private String describe0310Param(int idBytes, long id, byte[] value) {
		// Huabao/JT808: 0x0310 + 1字节参数ID
		if (idBytes != 1) {
			return "";
		}
		int pid = (int) (id & 0xFF);
		switch (pid) {
		case 0x24:
			// 布防/撤防: value[0]=01布防,00撤防; 后续为用户名
			if (value == null || value.length < 1) {
				return "alarmArmDisarm(值为空)";
			}
			int flag = value[0] & 0xFF;
			String op = flag == 1 ? "布防(ARM)" : "撤防(DISARM)";
			String user = "";
			if (value.length > 1) {
				user = new String(Arrays.copyOfRange(value, 1, value.length), StandardCharsets.US_ASCII).trim();
			}
			return "报警设防控制:" + op + (user.isEmpty() ? "" : ",用户=" + user);
		case 0x23:
			// 重启设备: 03
			return "设备重启参数(0x23)";
		case 0x06:
			// 位置上报间隔（秒），通常4字节
			if (value != null && value.length == 4) {
				int sec = ((value[0] & 0xFF) << 24) | ((value[1] & 0xFF) << 16)
						| ((value[2] & 0xFF) << 8) | (value[3] & 0xFF);
				return "位置上报周期:" + sec + "秒";
			}
			return "位置上报周期参数(0x06)";
		default:
			return "";
		}
	}

	private String describeDownstreamInstruction(int msgId, byte[] contentBytes) {
		switch (msgId) {
		case 0x8001:
			return "平台通用应答(ACK)，通常是对终端上报(如0x0200)的确认，不是网页端单次业务指令。";
		case 0x8100:
			return "终端注册应答(登录流程)。";
		case 0x8103:
			return "读取终端参数(常见于网页端参数查询)。";
		case 0x8106:
			return "查询指定终端参数(常见于网页端按参数ID查询)。";
		case 0x8105:
			return "发动机控制(网页端引擎熄火/恢复)。";
		case 0x8201:
			return "位置信息查询(网页端单次定位/刷新位置常见)。";
		case 0x8300:
			return "文本/自定义下发(常见于网页端发送文本或协议透传指令)。";
		case 0x8301:
			return "事件/文本类下发(具体含义依设备协议实现)。";
		case 0x8801:
			return "拍照命令(网页端抓拍)。";
		case 0x9101:
			return "实时视频请求(网页端实时视频)。";
		case 0x9201:
			return "历史视频回放请求(网页端回放)。";
		case 0x9205:
			return "回放资源查询(网页端拉取回放资源列表)。";
		case 0x9208:
			return "报警附件上传请求(网页端查看报警附件)。";
		case 0x0310:
			return describe0310AsWebCommand(contentBytes);
		default:
			return "";
		}
	}

	private String describe0310AsWebCommand(byte[] body) {
		if (body == null || body.length < 4) {
			return "参数设置(0x0310)，消息体过短。";
		}
		int count = body[0] & 0xFF;
		if (count <= 0) {
			return "参数设置(0x0310)，未携带参数项。";
		}
		int pid = body[1] & 0xFF;
		int len = body[2] & 0xFF;
		if (3 + len > body.length) {
			return "参数设置(0x0310)，参数长度异常(pid=0x" + String.format("%02X", pid) + ")";
		}
		byte[] value = Arrays.copyOfRange(body, 3, 3 + len);
		if (pid == 0x24) {
			int flag = value.length > 0 ? (value[0] & 0xFF) : -1;
			String user = value.length > 1
					? new String(Arrays.copyOfRange(value, 1, value.length), StandardCharsets.US_ASCII).trim()
					: "";
			if (flag == 0x00) {
				return "网页端指令=解除警报(撤防 DISARM), 参数ID=0x24, 用户=" + (user.isEmpty() ? "-" : user);
			} else if (flag == 0x01) {
				return "网页端指令=设防(ARM), 参数ID=0x24, 用户=" + (user.isEmpty() ? "-" : user);
			}
			return "网页端报警设防控制, 参数ID=0x24, flag=0x" + String.format("%02X", flag);
		}
		if (pid == 0x23) {
			return "网页端指令=重启设备, 参数ID=0x23";
		}
		if (pid == 0x06) {
			if (value.length == 4) {
				int sec = ((value[0] & 0xFF) << 24) | ((value[1] & 0xFF) << 16)
						| ((value[2] & 0xFF) << 8) | (value[3] & 0xFF);
				return "网页端指令=设置位置上报周期, 参数ID=0x06, 周期=" + sec + "秒";
			}
			return "网页端指令=设置位置上报周期, 参数ID=0x06";
		}
		return "参数设置(0x0310), 参数ID=0x" + String.format("%02X", pid) + ", HEX=" + Utils.bytesToHex(value);
	}

	private String describe8105(byte[] body) {
		if (body == null || body.length == 0) {
			return "控制字节为空";
		}
		int op = body[0] & 0xFF;
		if (op == 0xF0) {
			return "引擎熄火(断油断电)";
		}
		if (op == 0xF1) {
			return "引擎恢复(恢复油电)";
		}
		return "未知控制字节=0x" + String.format("%02X", op);
	}

	private void log8001Summary(byte[] body) {
		// 0x8001: [应答流水号(2)][应答ID(2)][结果(1)]
		if (body == null || body.length < 5) {
			session.callAardio("位置上报平台应答(0x8001): 数据长度异常");
			return;
		}
		int replySn = ((body[0] & 0xFF) << 8) | (body[1] & 0xFF);
		int replyId = ((body[2] & 0xFF) << 8) | (body[3] & 0xFF);
		int result = body[4] & 0xFF;
		session.callAardio(String.format("位置上报平台应答: sn=%d, replyId=0x%04X, result=%d(%s)",
				replySn, replyId, result, decode8001Result(result)));
	}

	private String decode8001Result(int result) {
		switch (result) {
		case 0:
			return "成功";
		case 1:
			return "失败";
		case 2:
			return "消息有误";
		case 3:
			return "不支持";
		case 4:
			return "报警处理确认";
		default:
			return "未知";
		}
	}

	private String previewText(byte[] bytes, String charset) {
		try {
			String text = new String(bytes, charset);
			text = sanitizeTextVisible(text);
			if (text.length() > 120) {
				text = text.substring(0, 120) + "...";
			}
			return text;
		} catch (Exception e) {
			return "(decode-failed:" + charset + ")";
		}
	}

	private String sanitizeTextVisible(String text) {
		if (text == null || text.isEmpty()) {
			return "";
		}
		StringBuilder sb = new StringBuilder(text.length());
		for (int i = 0; i < text.length(); i++) {
			char c = text.charAt(i);
			if (c == '\r' || c == '\n' || c == '\t') {
				sb.append(' ');
			} else if (c < 32 || c == 127) {
				sb.append("\\u").append(String.format("%04X", (int) c));
			} else {
				sb.append(c);
			}
		}
		return sb.toString();
	}

	private String previewBytesVisible(byte[] bytes) {
		if (bytes == null || bytes.length == 0) {
			return "";
		}
		StringBuilder sb = new StringBuilder(bytes.length * 2);
		for (byte b : bytes) {
			int v = b & 0xFF;
			if (v >= 0x20 && v <= 0x7E) {
				sb.append((char) v);
			} else {
				sb.append("\\x").append(String.format("%02X", v));
			}
		}
		return sb.toString();
	}

	private static class ParseResult {
		private final int parsedItems;
		private final int remainBytes;
		private final String message;

		private ParseResult(int parsedItems, int remainBytes, String message) {
			this.parsedItems = parsedItems;
			this.remainBytes = remainBytes;
			this.message = message;
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		super.channelInactive(ctx);
		session.callAardio("服务器连接断开");
		session.setChannel(null);
		session.getUi().notifyDisconnected();
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		super.channelActive(ctx);
		session.setChannel(ctx.channel());
		session.callAardio("服务器连接成功");
		session.send0x0100();
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable t) {
		t.printStackTrace();
		String detail = t.getMessage() != null ? t.getMessage() : t.getClass().getSimpleName();
		session.callAardio("服务器连接异常: " + detail);
		StringWriter sw = new StringWriter();
		t.printStackTrace(new PrintWriter(sw, true));
		session.addText(sw.toString());
		session.callAardio("1004", true);
		ctx.close();
	}
	
}
