package com.lingx.jt808.netty;

import io.netty.buffer.ByteBuf;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.channel.ChannelHandlerContext;

import java.util.List;

/**
 * JT808 协议解码器。
 * <p>
 * 优化要点：
 * <ul>
 *   <li>使用 ByteBuf.indexOf() 替代逐字节扫描，提升高吞吐场景性能</li>
 *   <li>添加最大帧长度保护，防止恶意/异常数据导致 OOM</li>
 *   <li>正确跳过孤立的 0x7E 标记</li>
 * </ul>
 */
public class ProtocolDecoder extends ByteToMessageDecoder {

	private static final int MIN_FRAME_SIZE = 10;
	/** 最大帧长度 2MB，防止 OOM */
	private static final int MAX_FRAME_SIZE = 2 * 1024 * 1024;

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		while (in.readableBytes() >= MIN_FRAME_SIZE) {
			// 查找起始标记 0x7E
			int startIndex = in.indexOf(in.readerIndex(), in.writerIndex(), (byte) 0x7E);
			if (startIndex == -1) {
				// 没有 0x7E，丢弃所有数据
				in.skipBytes(in.readableBytes());
				return;
			}

			// 跳过起始标记之前的数据（垃圾数据）
			if (startIndex > in.readerIndex()) {
				in.skipBytes(startIndex - in.readerIndex());
			}

			// 从 0x7E 之后查找结束标记 0x7E
			if (in.readableBytes() < 2) {
				return;
			}
			int endIndex = in.indexOf(in.readerIndex() + 1, in.writerIndex(), (byte) 0x7E);
			if (endIndex == -1) {
				// 没有找到结束标记，等待更多数据
				// 但如果已超过最大帧长度，丢弃防止 OOM
				if (in.readableBytes() > MAX_FRAME_SIZE) {
					in.skipBytes(in.readableBytes());
				}
				return;
			}

			// 计算完整帧长度（包含首尾 0x7E）
			int frameLength = endIndex - in.readerIndex() + 1;
			if (frameLength <= 2) {
				// 空 0x7E...0x7E 帧，跳过
				in.skipBytes(2);
				continue;
			}

			// 超过最大帧长度，跳过此帧
			if (frameLength > MAX_FRAME_SIZE) {
				in.skipBytes(frameLength);
				continue;
			}

			// 读取完整帧（包含首尾 0x7E）
			byte[] frame = new byte[frameLength];
			in.readBytes(frame);
			out.add(frame);
		}
	}
}
