package com.lingx.jt808;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

import com.lingx.jt808.netty.TcpClient;
import com.lingx.jtools.ui.PropUtils;
import com.lingx.jtools.ui.TcpClientTabBridge;

import io.netty.channel.EventLoopGroup;

public class App {

	/** 批量模式：所有客户端上下文，用于统计和关闭 */
	private static final List<JT808ClientContext> batchContexts = new ArrayList<>();

	public static void main(String[] args) {
		if (args.length > 0 && ("--batch".equalsIgnoreCase(args[0]) || "-b".equalsIgnoreCase(args[0]))) {
			runBatch(args);
			return;
		}
		runSingle(args);
	}

	private static void runSingle(String[] args) {
		String ip = "47.100.112.218", port = "8808", tid = "012345678912";
		if (args.length > 0) {
			ip = args[0];
		}
		if (args.length > 1) {
			port = args[1];
		}
		if (args.length > 2) {
			tid = args[2];
		}
		try {
			TcpClientTabBridge ui = new TcpClientTabBridge();
			JT808ClientContext ctx = new JT808ClientContext(ui);
			ctx.setTid(tid, "");
			ctx.tcp(ip, port);
			Thread.sleep(1000);
			ctx.start0x0200();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private static void runBatch(String[] args) {
		if (args.length < 5) {
			printBatchUsage();
			return;
		}
		try {
			String ip = args[1];
			String port = args[2];
			long startTid = Long.parseLong(args[3]);
			int count = Integer.parseInt(args[4]);
			int intervalSec = args.length > 5 ? Integer.parseInt(args[5]) : 15;
			int connectDelayMs = args.length > 6 ? Integer.parseInt(args[6]) : 30;
			String version = args.length > 7 ? args[7] : "";

			if (count <= 0) {
				throw new IllegalArgumentException("count 必须大于 0");
			}
			if (intervalSec <= 0) {
				throw new IllegalArgumentException("intervalSec 必须大于 0");
			}
			if (connectDelayMs < 0) {
				throw new IllegalArgumentException("connectDelayMs 不能小于 0");
			}

			PropUtils.setProp("jt808.0x0200.interval", String.valueOf(intervalSec));
			System.out.println(String.format(
					"[BATCH] ip=%s port=%s startTid=%d count=%d intervalSec=%d connectDelayMs=%d version=%s",
					ip, port, startTid, count, intervalSec, connectDelayMs, version.isEmpty() ? "default" : version));

			// 获取共享 EventLoopGroup（所有客户端复用）
			EventLoopGroup sharedGroup = TcpClient.getSharedGroup();

			// 注册关闭钩子（Ctrl+C 时优雅退出）
			Runtime.getRuntime().addShutdownHook(new Thread(() -> {
				System.out.println("\n[BATCH] 正在关闭所有连接...");
				for (JT808ClientContext ctx : batchContexts) {
					try {
						ctx.tcpClose();
					} catch (Exception ignored) {
					}
				}
				TcpClient.shutdownSharedGroup();
				System.out.println("[BATCH] 所有连接已关闭。");
			}));

			// 统计：启动线程数、在线数、失败数
			AtomicInteger onlineCount = new AtomicInteger(0);
			AtomicInteger failCount = new AtomicInteger(0);

			// 启动统计线程（每 10 秒输出一次）
			Thread statsThread = new Thread(() -> {
				SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
				while (true) {
					try {
						Thread.sleep(10000);
						long totalSent = 0;
						int online = 0;
						for (JT808ClientContext ctx : batchContexts) {
							if (ctx.isConnected()) {
								online++;
							}
							totalSent += ctx.getSentCount();
						}
						System.out.println(String.format("[STATS] %s online=%d/%d sent=%d fail=%d",
								sdf.format(new Date()), online, batchContexts.size(), totalSent, failCount.get()));
					} catch (InterruptedException e) {
						break;
					}
				}
			}, "batch-stats");
			statsThread.setDaemon(true);
			statsThread.start();

			// 分批启动客户端
			for (int i = 0; i < count; i++) {
				long tidNum = startTid + i;
				String tid = String.valueOf(tidNum);
				TcpClientTabBridge ui = new TcpClientTabBridge();
				JT808ClientContext ctx = new JT808ClientContext(ui);
				ctx.setTrafficLogEnabled(false);
				ctx.setStatusLogEnabled(false);
				ctx.setSharedGroup(sharedGroup);
				ctx.setTid(tid, version);
				ctx.tcp(ip, port);
				ctx.start0x0200();
				batchContexts.add(ctx);

				if (connectDelayMs > 0) {
					Thread.sleep(connectDelayMs);
				}
				if ((i + 1) % 100 == 0 || i + 1 == count) {
					System.out.println(String.format("[BATCH] started %d/%d", i + 1, count));
				}
			}

			System.out.println("[BATCH] all clients started, press Ctrl+C to stop.");
			new CountDownLatch(1).await();
		} catch (Exception e) {
			e.printStackTrace();
			printBatchUsage();
		}
	}

	private static void printBatchUsage() {
		System.out.println("批量模式用法:");
		System.out.println(
				"  java -cp <classpath> com.lingx.jt808.App --batch <ip> <port> <startTid> <count> [intervalSec] [connectDelayMs] [version]");
		System.out.println("示例:");
		System.out.println("  ... App --batch 121.40.187.223 8800 130000000000 500 30 20 jt808-2011");
	}
}
