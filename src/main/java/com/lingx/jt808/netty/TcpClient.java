package com.lingx.jt808.netty;

import com.lingx.jt808.JT808ClientContext;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.WriteBufferWaterMark;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.bytes.ByteArrayEncoder;

/**
 * JT808 TCP 客户端连接器。
 * <p>
 * 优化要点：
 * <ul>
 *   <li>支持共享 EventLoopGroup（批量模式所有客户端共用一个线程组，避免线程爆炸）</li>
 *   <li>TCP_NODELAY 禁用 Nagle 算法，降低小包延迟</li>
 *   <li>PooledByteBufAllocator 减少高频上报时的 GC 压力</li>
 *   <li>写水位线防止发送过快导致 OOM</li>
 * </ul>
 */
public final class TcpClient {

	/** 共享 EventLoopGroup，线程数 = CPU 核心数，批量模式复用 */
	private static volatile NioEventLoopGroup sharedGroup;

	private TcpClient() {
	}

	/** 获取共享 EventLoopGroup（懒创建，批量模式使用） */
	public static synchronized NioEventLoopGroup getSharedGroup() {
		if (sharedGroup == null || sharedGroup.isShuttingDown()) {
			sharedGroup = new NioEventLoopGroup(Runtime.getRuntime().availableProcessors());
		}
		return sharedGroup;
	}

	/** 关闭共享 EventLoopGroup（应用退出时调用） */
	public static synchronized void shutdownSharedGroup() {
		if (sharedGroup != null && !sharedGroup.isShuttingDown()) {
			sharedGroup.shutdownGracefully();
			sharedGroup = null;
		}
	}

	/** 原有接口保持不变（GUI 单客户端模式，自建 EventLoopGroup） */
	public static void runBlocking(String ip, int port, JT808ClientContext ctx) {
		runBlocking(ip, port, ctx, null);
	}

	/**
	 * 连接服务器并阻塞直到连接断开。
	 *
	 * @param group 若非 null 则复用该组（批量模式）；若为 null 则自建并自行关闭
	 */
	public static void runBlocking(String ip, int port, JT808ClientContext ctx, EventLoopGroup group) {
		boolean ownGroup = (group == null);
		EventLoopGroup workerGroup = ownGroup ? new NioEventLoopGroup() : group;
		try {
			Bootstrap bootstrap = new Bootstrap();
			bootstrap.group(workerGroup);
			bootstrap.channel(NioSocketChannel.class);

			// --- TCP 参数优化 ---
			bootstrap.option(ChannelOption.TCP_NODELAY, true);
			bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
			bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
			bootstrap.option(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT);
			bootstrap.option(ChannelOption.WRITE_BUFFER_WATER_MARK,
					new WriteBufferWaterMark(8 * 1024, 32 * 1024));

			bootstrap.handler(new ChannelInitializer<SocketChannel>() {
				@Override
				protected void initChannel(SocketChannel ch) {
					ch.pipeline().addLast("decoder", new ProtocolDecoder());
					ch.pipeline().addLast("encoder", new ByteArrayEncoder());
					ch.pipeline().addLast(new TcpClientHandler(ctx));
				}
			});

			ChannelFuture future = bootstrap.connect(ip, port).sync();
			future.channel().closeFuture().sync();
		} catch (Exception e) {
			ctx.getUi().notifyDisconnected();
			ctx.callAardio("服务器连接失败" + ip + ":" + port);
		} finally {
			if (ownGroup) {
				workerGroup.shutdownGracefully();
			}
		}
	}
}
