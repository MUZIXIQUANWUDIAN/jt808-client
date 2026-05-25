package com.lingx.jtools.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.CookieManager;
import java.net.CookieHandler;
import java.net.URL;
import java.util.Base64;
import java.util.Locale;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.lingx.jt808.JT808ClientContext;
import com.lingx.jt808.netty.TcpClient;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.dialog.Dialog0x0100;
import com.lingx.jtools.ui.dialog.Dialog0x0102;
import com.lingx.jtools.ui.dialog.Dialog0x0200;
import com.lingx.jtools.ui.dialog.DialogCustomCommand;

public class JT808ClientPanel extends JPanel{

	private static final int STOP_CHUNK_SIZE = 100;
	private static final int BATCH_PARALLEL_SIZE = 50;

	private static JButton startButton,stopButton;
	private JButton batchStartButton, batchStopButton;
	private JButton batch500StartButton, batch500StopButton;
	private JButton batch1000StartButton, batch1000StopButton;
	private JButton batch3000StartButton, batch3000StopButton;
	private final TcpClientTabBridge tcpTabUi = new TcpClientTabBridge();
	private final JT808ClientContext jt808 = new JT808ClientContext(tcpTabUi);
	private final List<JT808ClientContext> batchContexts = Collections.synchronizedList(new ArrayList<>());
	private volatile boolean batchRunning = false;
	private volatile boolean batchStopping = false;
	private volatile int lastBatchDelayMs = 20;
	private volatile int lastBatchStopDelayMs = 20;
	private volatile int currentBatchIntervalSec = 30;
	private volatile int batchStartedCount = 0;
	private volatile int batchStoppedCount = 0;
	private volatile int batchFailedCount = 0;
	private volatile long batchStartAtMs = 0L;
	private volatile long lastSentSampleAtMs = 0L;
	private volatile long lastSentSampleTotal = 0L;
	private volatile double sendTps = 0d;
	private JTextArea textArea;
	private MJTextField batchIntervalField;
	private JLabel batchStateLabel;
	private JLabel batchOnlineLabel;
	private JLabel batchStartedLabel;
	private JLabel batchStoppedLabel;
	private JLabel batchFailedLabel;
	private JLabel batchTpsLabel;
	private JLabel batchUptimeLabel;
	private JLabel serverDevicesLabel;
	private JLabel serverOnlineLabel;
	private JLabel serverPositionsLabel;
	private JLabel serverCpuLabel;
	private JLabel serverMemLabel;
	private JLabel serverDiskLabel;
	private JLabel serverConnLabel;
	private JLabel serverDockerLabel;
	private JLabel serverBandwidthLabel;
	private volatile long lastNetSampleAtMs = 0L;
	private volatile Double lastRxBytes = null;
	private volatile Double lastTxBytes = null;
	private volatile boolean serverRefreshing = false;
	private static final CookieManager cookieManager = new CookieManager();
	private volatile TraccarAuth cachedTraccarAuth = null;
	private volatile String cachedTraccarBaseUrl = "";

	private static final class TraccarAuth {
		private final String username;
		private final String password;
		private final String basicAuthHeader;

		private TraccarAuth(String username, String password, String basicAuthHeader) {
			this.username = username;
			this.password = password;
			this.basicAuthHeader = basicAuthHeader;
		}
	}

	public JT808ClientContext getJt808Context() {
		return jt808;
	}

	public JT808ClientPanel() {
		this.setBackground(Color.decode("#dfe9f6"));
		this.setLayout(new FlexLayout(32,5,10));

        ImageIcon icon1 = new ImageIcon(JttoolsFrame.class.getResource("/images/play_green.png"));
        ImageIcon icon2 = new ImageIcon(JttoolsFrame.class.getResource("/images/stop_red.png"));

        startButton = new JButton("启动");
        startButton.setIcon(icon1);
        stopButton = new JButton("停止");
        stopButton.setIcon(icon2);

        MJTextField text1 = new MJTextField(PropUtils.getProp("server.ip"));
        MJTextField text2 = new MJTextField(PropUtils.getProp("server.port"));
        MJTextField text3 = new MJTextField(PropUtils.getProp("device.tid"));
		this.add(new JLabel("服务器IP:",JLabel.RIGHT),"flex:1");
		this.add(text1,"flex:2");
		this.add(new JLabel("端口:",JLabel.RIGHT),"flex:1;width:40px;");
		this.add(text2,"flex:1;width:50px;");
		this.add(new JLabel("设备号:",JLabel.RIGHT),"flex:1;width:50px;");
		this.add(text3,"flex:2;width:100px;");

		JComboBox<String> comboBox = new JComboBox<>();
		// 向下拉列表添加数据
		comboBox.addItem("jt808-2011");
		comboBox.addItem("jt808-2013");
		comboBox.addItem("jt808-2019");
		comboBox.setSelectedItem(PropUtils.getProp("device.version"));
		this.add(comboBox, "flex:1;width:110px;");

		setButtunZt1();
		this.add(startButton, "flex:1;width:80px;");
		this.add(stopButton, "flex:1;width:80px;wrap;");

		MJTextField batchStartTid = new MJTextField("130000000000");
		MJTextField batchCount = new MJTextField("100");
		MJTextField batchInterval = new MJTextField("30");
		MJTextField batchDelay = new MJTextField("20");
		MJTextField batchStopDelay = new MJTextField(String.valueOf(20));
		this.batchIntervalField = batchInterval;
		batchStartButton = new JButton("批量启动");
		batchStopButton = new JButton("批量停止");
		batch500StartButton = new JButton("500批量启动");
		batch500StopButton = new JButton("500批量停止");
		batch1000StartButton = new JButton("1000批量启动");
		batch1000StopButton = new JButton("1000批量停止");
		batch3000StartButton = new JButton("3000批量启动");
		batch3000StopButton = new JButton("3000批量停止");
		batchStopButton.setEnabled(false);
		batch500StopButton.setEnabled(false);
		batch1000StopButton.setEnabled(false);
		batch3000StopButton.setEnabled(false);
		JButton interval3Button = new JButton("3s");
		JButton interval5Button = new JButton("5s");
		JButton interval10Button = new JButton("10s");
		JButton interval30Button = new JButton("30s");
		this.add(new JLabel("批量起始设备号:", JLabel.RIGHT), "flex:2");
		this.add(batchStartTid, "flex:3;width:150px;");
		this.add(new JLabel("数量:", JLabel.RIGHT), "flex:1;width:40px;");
		this.add(batchCount, "flex:1;width:70px;");
		this.add(new JLabel("间隔秒:", JLabel.RIGHT), "flex:1;width:50px;");
		this.add(batchInterval, "flex:1;width:60px;");
		this.add(new JLabel("启动间隔ms:", JLabel.RIGHT), "flex:1;width:70px;");
		this.add(batchDelay, "flex:1;width:70px;");
		this.add(new JLabel("停止间隔ms:", JLabel.RIGHT), "flex:1;width:70px;");
		this.add(batchStopDelay, "flex:1;width:70px;");
		this.add(new JLabel("间隔快捷:", JLabel.RIGHT), "flex:1;width:60px;");
		this.add(interval3Button, "flex:1;width:45px;");
		this.add(interval5Button, "flex:1;width:45px;");
		this.add(interval10Button, "flex:1;width:50px;");
		this.add(interval30Button, "flex:1;width:50px;wrap;");

		this.add(batchStartButton, "flex:1;width:80px;");
		this.add(batchStopButton, "flex:1;width:80px;");
		this.add(batch500StartButton, "flex:1;width:95px;");
		this.add(batch500StopButton, "flex:1;width:95px;");
		this.add(batch1000StartButton, "flex:1;width:105px;");
		this.add(batch1000StopButton, "flex:1;width:105px;");
		this.add(batch3000StartButton, "flex:1;width:110px;");
		this.add(batch3000StopButton, "flex:1;width:110px;wrap;");

		batchStateLabel = new JLabel("状态: 空闲");
		batchOnlineLabel = new JLabel("在线连接: 0");
		batchStartedLabel = new JLabel("已启动: 0");
		batchStoppedLabel = new JLabel("已停止: 0");
		batchFailedLabel = new JLabel("失败: 0");
		batchTpsLabel = new JLabel("发送TPS: 0.0");
		batchUptimeLabel = new JLabel("运行时长: 00:00:00");
		this.add(batchStateLabel, "flex:2");
		this.add(batchOnlineLabel, "flex:2");
		this.add(batchStartedLabel, "flex:2");
		this.add(batchStoppedLabel, "flex:2");
		this.add(batchFailedLabel, "flex:2");
		this.add(batchTpsLabel, "flex:2");
		this.add(batchUptimeLabel, "flex:2;wrap;");

		serverDevicesLabel = new JLabel("注册设备: -");
		serverOnlineLabel = new JLabel("在线设备: -");
		serverPositionsLabel = new JLabel("位置上报: -");
		this.add(new JLabel("Traccar:", JLabel.RIGHT), "flex:1");
		this.add(serverDevicesLabel, "flex:2");
		this.add(serverOnlineLabel, "flex:2");
		this.add(serverPositionsLabel, "flex:2;wrap;");

		serverCpuLabel = new JLabel("CPU: -");
		serverMemLabel = new JLabel("内存: -");
		serverDiskLabel = new JLabel("磁盘: -");
		serverConnLabel = new JLabel("TCP: -");
		serverDockerLabel = new JLabel("Docker: -");
		serverBandwidthLabel = new JLabel("带宽: -");
		this.add(new JLabel("服务器资源:", JLabel.RIGHT), "flex:1");
		this.add(serverCpuLabel, "flex:2");
		this.add(serverMemLabel, "flex:2");
		this.add(serverDockerLabel, "flex:2;wrap;");

		this.add(new JLabel("", JLabel.RIGHT), "flex:1");
		this.add(serverDiskLabel, "flex:2");
		this.add(serverConnLabel, "flex:2");
		this.add(serverBandwidthLabel, "flex:2;wrap;");

		textArea = new MJTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(false);
		textArea.setBackground(Color.decode("#ffffff"));
			((javax.swing.text.DefaultCaret)textArea.getCaret()).setUpdatePolicy(javax.swing.text.DefaultCaret.NEVER_UPDATE);
		tcpTabUi.bind(textArea, JT808ClientPanel::setButtunZt1);
		JScrollPane scrollPane = new JScrollPane(textArea);
		this.add(scrollPane, "flex:12;height:400px");

		JButton btnclear = new JButton("清空数据");
		this.add(btnclear, "flex:1;width:80px");
		
		JButton btn0x0100 = new JButton("注册设置");
		this.add(btn0x0100, "flex:1;width:80px");

		JButton btn0x0102 = new JButton("鉴权设置");
		this.add(btn0x0102, "flex:1;width:80px");

		JButton btn0x0200 = new JButton("位置设置");
		this.add(btn0x0200, "flex:1;width:80px");

		JButton btn1078 = new JButton("视频说明");
		this.add(btn1078, "flex:1;width:80px");
		
		JButton btnAdas = new JButton("触发ADAS报警");
		this.add(btnAdas, "flex:1;width:110px");

		JButton btnDsm = new JButton("触发DSM报警");
		this.add(btnDsm, "flex:1;width:110px");
		JButton btnCustomCommand = new JButton("自定义指令");
		this.add(btnCustomCommand, "flex:1;width:95px");
		
        startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String tid1=text3.getText().trim();
				if(!Utils.isNumber(tid1)) {
					JOptionPane.showMessageDialog(null, "设备号必须为数字");
					return ;
				}
				String version=comboBox.getSelectedItem().toString();
				if(version.contains("2019")) {
					if(tid1.length()>20) {
						JOptionPane.showMessageDialog(null, "当版本为2019时，设备号长度不大于20。");
						return ;
					}
				}else {
					if(tid1.length()>12) {
						JOptionPane.showMessageDialog(null, "当版本为2011或2013时，设备号长度不大于12。");
						return ;
					}
				}
				PropUtils.setProp("server.ip",text1.getText().trim());
				PropUtils.setProp("server.port",text2.getText().trim());
				PropUtils.setProp("device.tid",text3.getText().trim());
				PropUtils.setProp("device.version",comboBox.getSelectedItem().toString());
				PropUtils.save();
				jt808.setTid(tid1, version);
				jt808.tcp(text1.getText(), text2.getText());

				setButtunZt2();
			}});
        

        stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jt808.tcpClose();
				setButtunZt1();
			}});

		batchStartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tryStartBatch(
						text1.getText().trim(),
						text2.getText().trim(),
						comboBox.getSelectedItem().toString(),
						batchStartTid.getText().trim(),
						batchCount.getText().trim(),
						batchInterval.getText().trim(),
						batchDelay.getText().trim(),
						batchStopDelay.getText().trim());
			}
		});

		batchStopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopBatch();
			}
		});

		batch500StartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tryStartBatch(
						text1.getText().trim(),
						text2.getText().trim(),
						comboBox.getSelectedItem().toString(),
						batchStartTid.getText().trim(),
						"500",
						batchInterval.getText().trim(),
						batchDelay.getText().trim(),
						batchStopDelay.getText().trim());
			}
		});

		batch500StopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopBatch();
			}
		});

		batch1000StartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tryStartBatch(
						text1.getText().trim(),
						text2.getText().trim(),
						comboBox.getSelectedItem().toString(),
						batchStartTid.getText().trim(),
						"1000",
						batchInterval.getText().trim(),
						batchDelay.getText().trim(),
						batchStopDelay.getText().trim());
			}
		});

		batch1000StopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopBatch();
			}
		});

		batch3000StartButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				tryStartBatch(
						text1.getText().trim(),
						text2.getText().trim(),
						comboBox.getSelectedItem().toString(),
						batchStartTid.getText().trim(),
						"3000",
						batchInterval.getText().trim(),
						batchDelay.getText().trim(),
						batchStopDelay.getText().trim());
			}
		});

		batch3000StopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				stopBatch();
			}
		});

		interval3Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				batchIntervalField.setText("3");
			}
		});
		interval5Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				batchIntervalField.setText("5");
			}
		});
		interval10Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				batchIntervalField.setText("10");
			}
		});
		interval30Button.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				batchIntervalField.setText("30");
			}
		});

        btn0x0100.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Dialog0x0100 dialog=new Dialog0x0100();
				dialog.setVisible(true);
			}});
        btn0x0102.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Dialog0x0102 dialog=new Dialog0x0102();
				dialog.setVisible(true);
			}});
        btn0x0200.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				Dialog0x0200 dialog=new Dialog0x0200();
				dialog.setVisible(true);
			}});
        btnclear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jt808.clear();
			}});
        btn1078.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "1、支持实时视频，模拟器不需要额外操作；平台上直接点播\r\n2、支持历史回放列表(只传一条记录)\r\n3、支持历史视频回放\r\n4、不支持多媒体文件上传");
			}});
        btnAdas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jt808.sendAdas();
			}});
        btnDsm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				jt808.sendDsm();
			}});
		btnCustomCommand.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				DialogCustomCommand dialog = new DialogCustomCommand(text1.getText().trim(), text3.getText().trim());
				dialog.setVisible(true);
			}});

		javax.swing.Timer statTimer = new javax.swing.Timer(1000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshBatchStats();
			}
		});
		statTimer.start();

		javax.swing.Timer serverStatusTimer = new javax.swing.Timer(3000, new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				refreshServerStatus(text1.getText().trim());
			}
		});
		serverStatusTimer.start();

	}

	public static void setButtunZt1() {
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
	}
	public static void setButtunZt2() {
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
	}

	private void tryStartBatch(
			String ip, String port, String version, String startTidText,
			String countText, String intervalText, String delayText, String stopDelayText) {
		if (batchRunning || batchStopping) {
			JOptionPane.showMessageDialog(null, "批量任务正在运行或停止中");
			return;
		}
		if (!Utils.isNumber(countText) || !Utils.isNumber(intervalText) || !Utils.isNumber(delayText) || !Utils.isNumber(stopDelayText)) {
			JOptionPane.showMessageDialog(null, "数量、间隔秒、启动/停止间隔ms必须为数字");
			return;
		}
		if (!Utils.isNumber(startTidText)) {
			JOptionPane.showMessageDialog(null, "批量起始设备号必须为数字");
			return;
		}
		int count = Integer.parseInt(countText);
		int intervalSec = Integer.parseInt(intervalText);
		int delayMs = Integer.parseInt(delayText);
		int stopDelayMs = Integer.parseInt(stopDelayText);
		if (count <= 0 || intervalSec <= 0 || delayMs < 0 || stopDelayMs < 0) {
			JOptionPane.showMessageDialog(null, "数量和间隔秒必须大于0，启动/停止间隔ms不能小于0");
			return;
		}
		PropUtils.setProp("server.ip", ip);
		PropUtils.setProp("server.port", port);
		PropUtils.setProp("device.version", version);
		PropUtils.save();
		startBatch(ip, port, version, startTidText, count, intervalSec, delayMs, stopDelayMs);
	}

	private void startBatch(String ip, String port, String version, String startTidText, int count, int intervalSec, int delayMs, int stopDelayMs) {
		batchRunning = true;
		batchStopping = false;
		batchStartedCount = 0;
		batchStoppedCount = 0;
		batchFailedCount = 0;
		batchStartAtMs = System.currentTimeMillis();
		lastSentSampleAtMs = 0L;
		lastSentSampleTotal = 0L;
		sendTps = 0d;
		currentBatchIntervalSec = intervalSec;
		lastBatchDelayMs = delayMs;
		lastBatchStopDelayMs = stopDelayMs;
		synchronized (batchContexts) {
			batchContexts.clear();
		}
		setBatchButtons(false);
		tcpTabUi.appendArrowLog(String.format("批量启动开始: ip=%s port=%s startTid=%s count=%d interval=%ds delay=%dms version=%s",
				ip, port, startTidText, count, intervalSec, delayMs, version));
		new Thread(() -> {
			int started = 0;
			int failed = 0;
			try {
				PropUtils.setProp("jt808.0x0200.interval", String.valueOf(intervalSec));
				BigInteger start = new BigInteger(startTidText);
				int maxLen = version.contains("2019") ? 20 : 12;
				int batchSize = BATCH_PARALLEL_SIZE;
				for (int batchStart = 0; batchStart < count; batchStart += batchSize) {
					if (!batchRunning) {
						break;
					}
					int batchEnd = Math.min(batchStart + batchSize, count);
					List<JT808ClientContext> batchResults = Collections.synchronizedList(new ArrayList<>());
					List<Thread> threads = new ArrayList<>();
					for (int i = batchStart; i < batchEnd; i++) {
						String tid = start.add(BigInteger.valueOf(i)).toString();
						if (tid.length() > maxLen) {
							tcpTabUi.appendArrowLog("批量停止: 设备号超出当前协议长度限制, tid=" + tid);
							break;
						}
						Thread t = new Thread(() -> {
							try {
								TcpClientTabBridge batchUi = new TcpClientTabBridge();
								JT808ClientContext ctx = new JT808ClientContext(batchUi);
								ctx.setTrafficLogEnabled(false);
								ctx.setStatusLogEnabled(false);
								ctx.setSharedGroup(TcpClient.getSharedGroup());
								ctx.setTid(tid, version);
								ctx.tcp(ip, port);
								ctx.start0x0200();
								batchResults.add(ctx);
							} catch (Exception ex) {
								tcpTabUi.appendArrowLog("批量启动失败 tid=" + tid + ", err=" + ex.getMessage());
							}
						});
						threads.add(t);
						t.start();
					}
					for (Thread t : threads) {
						t.join();
					}
					int batchOk = batchResults.size();
					int batchFail = (batchEnd - batchStart) - batchOk;
					started += batchOk;
					failed += batchFail;
					batchStartedCount = started;
					batchFailedCount = failed;
					batchContexts.addAll(batchResults);
					tcpTabUi.appendArrowLog("批量已启动: " + started + "/" + count + " (本批 " + batchOk + " 成功)");
					if (delayMs > 0 && batchEnd < count && batchRunning) {
						Thread.sleep(delayMs);
					}
				}
				batchFailedCount = failed;
				tcpTabUi.appendArrowLog("批量启动完成, 成功启动 " + started + " 台, 失败 " + failed + " 台");
			} catch (Exception ex) {
				tcpTabUi.appendArrowLog("批量启动异常: " + ex.getMessage());
			} finally {
				if (!batchRunning) {
					setBatchButtons(true);
				} else {
					batchRunning = started > 0;
					setBatchButtons(!batchRunning);
				}
			}
		}, "jt808-batch-starter").start();
	}

	private void stopBatch() {
		if (batchStopping) {
			return;
		}
		batchRunning = false;
		batchStopping = true;
		new Thread(() -> {
			List<JT808ClientContext> contexts;
			synchronized (batchContexts) {
				contexts = new ArrayList<>(batchContexts);
				batchContexts.clear();
			}
			int total = contexts.size();
			int batchSize = BATCH_PARALLEL_SIZE;
			int closed = 0;
			for (int batchStart = 0; batchStart < total; batchStart += batchSize) {
				int batchEnd = Math.min(batchStart + batchSize, total);
				List<Thread> threads = new ArrayList<>();
				for (int i = batchStart; i < batchEnd; i++) {
					JT808ClientContext ctx = contexts.get(i);
					Thread t = new Thread(() -> {
						try {
							ctx.tcpClose();
						} catch (Exception ignored) {
						}
					});
					threads.add(t);
					t.start();
				}
				for (Thread t : threads) {
					try { t.join(5000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
				}
				closed = batchEnd;
				batchStoppedCount = closed;
				if (total > STOP_CHUNK_SIZE) {
					tcpTabUi.appendArrowLog("批量停止进度: " + closed + "/" + total);
				}
			}
			tcpTabUi.appendArrowLog("批量已停止, 关闭连接数: " + closed);
			batchStopping = false;
			setBatchButtons(true);
		}, "jt808-batch-stopper").start();
	}
	private void setBatchButtons(boolean canStart) {
		SwingUtilities.invokeLater(() -> {
			if (batchStartButton != null) {
				batchStartButton.setEnabled(canStart);
			}
			if (batchStopButton != null) {
				batchStopButton.setEnabled(!canStart);
			}
			if (batch500StartButton != null) {
				batch500StartButton.setEnabled(canStart);
			}
			if (batch500StopButton != null) {
				batch500StopButton.setEnabled(!canStart);
			}
			if (batch1000StartButton != null) {
				batch1000StartButton.setEnabled(canStart);
			}
			if (batch1000StopButton != null) {
				batch1000StopButton.setEnabled(!canStart);
			}
			if (batch3000StartButton != null) {
				batch3000StartButton.setEnabled(canStart);
			}
			if (batch3000StopButton != null) {
				batch3000StopButton.setEnabled(!canStart);
			}
		});
	}

	private void refreshBatchStats() {
		List<JT808ClientContext> contexts;
		synchronized (batchContexts) {
			contexts = new ArrayList<>(batchContexts);
		}
		int online = 0;
		long sentTotal = 0L;
		for (JT808ClientContext ctx : contexts) {
			if (ctx.isConnected()) {
				online++;
			}
			sentTotal += ctx.getSentCount();
		}
		// 单机连接也计入在线数和发送统计，避免“在线连接:0”的误导。
		boolean singleOnline = jt808 != null && jt808.isConnected();
		if (singleOnline) {
			online++;
			sentTotal += jt808.getSentCount();
		}

		long now = System.currentTimeMillis();
		if (lastSentSampleAtMs > 0 && now > lastSentSampleAtMs) {
			double seconds = (now - lastSentSampleAtMs) / 1000.0;
			if (seconds > 0.1) {
				sendTps = Math.max(0d, (sentTotal - lastSentSampleTotal) / seconds);
			}
		}
		lastSentSampleAtMs = now;
		lastSentSampleTotal = sentTotal;

		double expectedTps = 0d;
		if (currentBatchIntervalSec > 0 && online > 0) {
			expectedTps = ((double) online) / currentBatchIntervalSec;
		}
		long uptime = batchStartAtMs > 0 ? Math.max(0L, (now - batchStartAtMs) / 1000L) : 0L;
		String state;
		if (batchStopping) {
			state = "停止中";
		} else if (batchRunning) {
			state = "运行中";
		} else if (singleOnline) {
			state = "单机在线";
		} else {
			state = "空闲";
		}
		batchStateLabel.setText("状态: " + state);
		batchOnlineLabel.setText("在线连接: " + online);
		batchStartedLabel.setText("已启动: " + batchStartedCount);
		batchStoppedLabel.setText("已停止: " + batchStoppedCount);
		batchFailedLabel.setText("失败: " + batchFailedCount);
		batchTpsLabel.setText(String.format(Locale.ROOT, "发送TPS: %.1f (预估 %.1f)", sendTps, expectedTps));
		batchUptimeLabel.setText("运行时长: " + formatDuration(uptime));
	}

	private static String formatDuration(long seconds) {
		long h = seconds / 3600;
		long m = (seconds % 3600) / 60;
		long s = seconds % 60;
		return String.format(Locale.ROOT, "%02d:%02d:%02d", h, m, s);
	}

	private void refreshServerStatus(String serverIp) {
		if (serverIp == null || serverIp.isEmpty()) {
			SwingUtilities.invokeLater(() -> {
				if (serverDevicesLabel != null) serverDevicesLabel.setText("注册设备: -");
				if (serverOnlineLabel != null) serverOnlineLabel.setText("在线设备: -");
				if (serverPositionsLabel != null) serverPositionsLabel.setText("位置上报: -");
				if (serverCpuLabel != null) serverCpuLabel.setText("CPU: -");
				if (serverMemLabel != null) serverMemLabel.setText("内存: -");
				if (serverDiskLabel != null) serverDiskLabel.setText("磁盘: -");
				if (serverConnLabel != null) serverConnLabel.setText("TCP: -");
				if (serverDockerLabel != null) serverDockerLabel.setText("Docker: -");
				if (serverBandwidthLabel != null) serverBandwidthLabel.setText("带宽: -");
			});
			return;
		}
		if (serverRefreshing) {
			return;
		}
		serverRefreshing = true;
		new Thread(() -> {
			String baseUrl = "http://" + serverIp + ":8082";
			TraccarAuth auth = null;
			String serverJson = null;
			try {
				auth = resolveTraccarAuth(baseUrl);
				String authHeader = auth != null ? auth.basicAuthHeader : null;
				serverJson = httpGetApiWithFallback(baseUrl, "/api/server", authHeader);
			} catch (Exception ignored) {
			}

			try {
				String authHeader = auth != null ? auth.basicAuthHeader : null;
				String devicesJson = httpGetApiWithFallback(baseUrl, "/api/devices", authHeader);
				int totalDevices = countOccurrences(devicesJson, "\"uniqueId\"");
				String compact = devicesJson.replace(" ", "");
				int onlineDevices = countOccurrences(compact, "\"status\":\"online\"");
				final int td = totalDevices, od = onlineDevices;
				SwingUtilities.invokeLater(() -> {
					serverDevicesLabel.setText("注册设备: " + td);
					serverOnlineLabel.setText("在线设备: " + od);
				});
			} catch (Exception e) {
				final String reason = shortReason(e);
				SwingUtilities.invokeLater(() -> {
					if (serverDevicesLabel != null) serverDevicesLabel.setText("注册设备: 失败(" + reason + ")");
					if (serverOnlineLabel != null) serverOnlineLabel.setText("在线设备: 失败");
				});
			}

			try {
				String authHeader = auth != null ? auth.basicAuthHeader : null;
				String positionsJson = httpGetApiWithFallback(baseUrl, "/api/positions", authHeader);
				int totalPositions = countOccurrences(positionsJson, "\"deviceId\"");
				final int tp = totalPositions;
				SwingUtilities.invokeLater(() -> serverPositionsLabel.setText("位置上报: " + tp));
			} catch (Exception e) {
				final String reason = shortReason(e);
				SwingUtilities.invokeLater(() -> {
					if (serverPositionsLabel != null) serverPositionsLabel.setText("位置上报: 失败(" + reason + ")");
				});
			}

			try {
				String statusJson = fetchStatusJson(serverIp, baseUrl, auth);
				String cpu = extractJsonValue(statusJson, "cpu_percent");
				String memUsed = extractJsonValue(statusJson, "mem_used_mb");
				String memTotal = extractJsonValue(statusJson, "mem_total_mb");
				String memPct = extractJsonValue(statusJson, "mem_percent");
				String diskPct = extractJsonValue(statusJson, "disk_percent");
				String diskUsed = extractJsonValue(statusJson, "disk_used");
				String diskTotal = extractJsonValue(statusJson, "disk_total");
				String tcp = extractJsonValue(statusJson, "tcp_connections");

				SwingUtilities.invokeLater(() -> {
					serverCpuLabel.setText("CPU: " + cpu + "%");
					serverMemLabel.setText("内存: " + memUsed + "/" + memTotal + "MB (" + memPct + "%)");
					serverDiskLabel.setText("磁盘: " + diskUsed + "/" + diskTotal + " (" + diskPct + "%)");
					serverConnLabel.setText("TCP连接: " + tcp);
					String dockerCpu = extractJsonValue(statusJson, "docker_cpu");
					String dockerMem = extractJsonValue(statusJson, "docker_mem");
					serverDockerLabel.setText("Docker: " + dockerCpu + " / " + dockerMem);
					serverBandwidthLabel.setText("带宽: " + detectBandwidth(statusJson));
				});
			} catch (Exception e) {
				final String serverJsonFinal = serverJson;
				final String reason = shortReason(e);
				SwingUtilities.invokeLater(() -> {
					if (serverCpuLabel != null) serverCpuLabel.setText("CPU: 状态接口不可达");
					if (serverMemLabel != null) serverMemLabel.setText("内存: -");
					if (serverDiskLabel != null) {
						String[] disk = extractDiskFromServerJson(serverJsonFinal);
						if (disk != null) {
							serverDiskLabel.setText("磁盘: " + disk[0] + "/" + disk[1] + " (" + disk[2] + "%)");
						} else {
							serverDiskLabel.setText("磁盘: -");
						}
					}
					if (serverConnLabel != null) serverConnLabel.setText("TCP: -");
					if (serverDockerLabel != null) {
						String version = extractJsonValue(serverJsonFinal == null ? "" : serverJsonFinal, "version");
						serverDockerLabel.setText("Traccar: " + ("-".equals(version) ? "-" : version));
					}
					if (serverBandwidthLabel != null) serverBandwidthLabel.setText("带宽: - (" + reason + ")");
				});
			} finally {
				serverRefreshing = false;
			}
		}, "jt808-server-status").start();
	}

	private TraccarAuth resolveTraccarAuth(String baseUrl) throws Exception {
		TraccarAuth cached = cachedTraccarAuth;
		if (cached != null && baseUrl.equals(cachedTraccarBaseUrl)) {
			httpGet(baseUrl + "/api/server", cached.basicAuthHeader);
			return cached;
		}

		for (String[] candidate : buildCredentialCandidates()) {
			String username = candidate[0];
			String password = candidate[1];
			String authHeader = "Basic " + Base64.getEncoder()
					.encodeToString((username + ":" + password).getBytes("UTF-8"));
			try {
				httpGet(baseUrl + "/api/server", authHeader);
				TraccarAuth auth = new TraccarAuth(username, password, authHeader);
				cachedTraccarAuth = auth;
				cachedTraccarBaseUrl = baseUrl;
				PropUtils.setProp("traccar.user", username);
				PropUtils.setProp("traccar.password", password);
				return auth;
			} catch (Exception ignored) {
			}
		}

		CookieHandler.setDefault(cookieManager);
		loginTraccar(baseUrl);
		httpGet(baseUrl + "/api/server");
		TraccarAuth auth = new TraccarAuth("", "", null);
		cachedTraccarAuth = auth;
		cachedTraccarBaseUrl = baseUrl;
		return auth;
	}

	private static List<String[]> buildCredentialCandidates() {
		List<String[]> list = new ArrayList<>();
		Set<String> dedup = new LinkedHashSet<>();
		addCredential(dedup, PropUtils.getProp("traccar.user"), PropUtils.getProp("traccar.password"));
		addCredential(dedup, PropUtils.getProp("server.user"), PropUtils.getProp("server.password"));
		addCredential(dedup, "admin@admin.com", "admin");
		addCredential(dedup, "admin", "admin");
		for (String item : dedup) {
			int p = item.indexOf('\n');
			if (p > 0) {
				list.add(new String[] { item.substring(0, p), item.substring(p + 1) });
			}
		}
		return list;
	}

	private static void addCredential(Set<String> target, String username, String password) {
		String u = username == null ? "" : username.trim();
		String p = password == null ? "" : password.trim();
		if (!u.isEmpty() && !p.isEmpty()) {
			target.add(u + "\n" + p);
		}
	}

	private String fetchStatusJson(String serverIp, String baseUrl, TraccarAuth auth) throws Exception {
		String configuredPort = PropUtils.getProp("status.port", "8090");
		int statusPort = parseInt(configuredPort, 8090);
		List<String> urls = new ArrayList<>();
		String customStatusUrl = PropUtils.getProp("status.url", "").trim();
		if (!customStatusUrl.isEmpty()) {
			urls.add(customStatusUrl);
		}
		urls.add("http://" + serverIp + ":" + statusPort + "/status");
		if (statusPort != 8082) {
			urls.add("http://" + serverIp + ":8082/status");
		}
		String authHeader = auth != null ? auth.basicAuthHeader : null;
		Exception last = null;
		for (String url : urls) {
			try {
				String body = httpGet(url, authHeader);
				if (looksLikeStatusJson(body)) {
					return body;
				}
			} catch (Exception e) {
				last = e;
			}
		}
		if (last != null) {
			throw last;
		}
		throw new Exception("状态接口不可用");
	}

	private String httpGetApiWithFallback(String baseUrl, String apiPath, String preferredAuthHeader) throws Exception {
		Exception last = null;
		if (preferredAuthHeader != null && !preferredAuthHeader.isEmpty()) {
			try {
				return httpGet(baseUrl + apiPath, preferredAuthHeader);
			} catch (Exception e) {
				last = e;
			}
		}
		for (String[] candidate : buildCredentialCandidates()) {
			String authHeader = "Basic " + Base64.getEncoder()
					.encodeToString((candidate[0] + ":" + candidate[1]).getBytes("UTF-8"));
			try {
				return httpGet(baseUrl + apiPath, authHeader);
			} catch (Exception e) {
				last = e;
			}
		}
		try {
			CookieHandler.setDefault(cookieManager);
			loginTraccar(baseUrl);
			return httpGet(baseUrl + apiPath);
		} catch (Exception e) {
			last = e;
		}
		if (last != null) {
			throw last;
		}
		throw new Exception("请求失败");
	}

	private static boolean looksLikeStatusJson(String body) {
		if (body == null) {
			return false;
		}
		String text = body.trim();
		if (!text.startsWith("{")) {
			return false;
		}
		return text.contains("\"cpu_percent\"")
				|| text.contains("\"mem_total_mb\"")
				|| text.contains("\"disk_total\"")
				|| text.contains("\"tcp_connections\"");
	}

	private static String shortReason(Exception e) {
		if (e == null || e.getMessage() == null || e.getMessage().trim().isEmpty()) {
			return "unknown";
		}
		String msg = e.getMessage().trim();
		return msg.length() > 28 ? msg.substring(0, 28) + "..." : msg;
	}

	private static String[] extractDiskFromServerJson(String serverJson) {
		if (serverJson == null || serverJson.isEmpty()) {
			return null;
		}
		String key = "\"storageSpace\":";
		int idx = serverJson.indexOf(key);
		if (idx < 0) {
			return null;
		}
		int lb = serverJson.indexOf('[', idx + key.length());
		int rb = serverJson.indexOf(']', lb + 1);
		if (lb < 0 || rb < 0) {
			return null;
		}
		String[] parts = serverJson.substring(lb + 1, rb).split(",");
		if (parts.length < 2) {
			return null;
		}
		Long free = parseLongSafe(parts[0]);
		Long total = parseLongSafe(parts[1]);
		if (free == null || total == null || total <= 0 || free < 0 || free > total) {
			return null;
		}
		long used = total - free;
		double pct = used * 100.0 / total;
		return new String[] { formatBytes(used), formatBytes(total), String.format(Locale.ROOT, "%.1f", pct) };
	}

	private static Long parseLongSafe(String text) {
		if (text == null) {
			return null;
		}
		try {
			return Long.parseLong(text.trim());
		} catch (Exception e) {
			return null;
		}
	}

	private static String formatBytes(long bytes) {
		final String[] units = { "B", "KB", "MB", "GB", "TB", "PB" };
		double value = bytes;
		int unit = 0;
		while (value >= 1024.0 && unit < units.length - 1) {
			value /= 1024.0;
			unit++;
		}
		return String.format(Locale.ROOT, "%.1f%s", value, units[unit]);
	}

	private static int parseInt(String value, int defaultValue) {
		if (value == null) {
			return defaultValue;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (Exception e) {
			return defaultValue;
		}
	}

	private String detectBandwidth(String statusJson) {
		Double downMbps = findFirstNumber(statusJson,
				"download_mbps", "down_mbps", "rx_mbps", "network_down_mbps", "net_down_mbps");
		Double upMbps = findFirstNumber(statusJson,
				"upload_mbps", "up_mbps", "tx_mbps", "network_up_mbps", "net_up_mbps");
		if (downMbps != null && upMbps != null) {
			return String.format(Locale.ROOT, "↓%.2f Mbps ↑%.2f Mbps", downMbps, upMbps);
		}

		Double rxBytes = findFirstNumber(statusJson,
				"net_rx_bytes", "rx_bytes", "download_bytes", "network_rx_bytes", "docker_rx_bytes");
		Double txBytes = findFirstNumber(statusJson,
				"net_tx_bytes", "tx_bytes", "upload_bytes", "network_tx_bytes", "docker_tx_bytes");
		long now = System.currentTimeMillis();
		if (rxBytes != null && txBytes != null) {
			if (lastNetSampleAtMs > 0 && lastRxBytes != null && lastTxBytes != null && now > lastNetSampleAtMs) {
				double seconds = (now - lastNetSampleAtMs) / 1000.0;
				if (seconds > 0.1) {
					double rxBps = Math.max(0d, (rxBytes - lastRxBytes) / seconds);
					double txBps = Math.max(0d, (txBytes - lastTxBytes) / seconds);
					double down = rxBps * 8d / 1_000_000d;
					double up = txBps * 8d / 1_000_000d;
					lastRxBytes = rxBytes;
					lastTxBytes = txBytes;
					lastNetSampleAtMs = now;
					return String.format(Locale.ROOT, "↓%.2f Mbps ↑%.2f Mbps", down, up);
				}
			}
			lastRxBytes = rxBytes;
			lastTxBytes = txBytes;
			lastNetSampleAtMs = now;
			return "采样中...";
		}
		return "-";
	}

	private static Double findFirstNumber(String json, String... keys) {
		for (String key : keys) {
			String value = extractJsonValue(json, key);
			if (value == null || value.isEmpty() || "-".equals(value)) {
				continue;
			}
			try {
				return Double.parseDouble(value);
			} catch (Exception ignored) {
			}
		}
		return null;
	}

	private static String extractJsonValue(String json, String key) {
		String pattern = "\"" + key + "\":";
		int idx = json.indexOf(pattern);
		if (idx < 0) return "-";
		int start = idx + pattern.length();
		while (start < json.length() && json.charAt(start) == ' ') start++;
		if (start >= json.length()) return "-";
		if (json.charAt(start) == '"') {
			int end = json.indexOf('"', start + 1);
			if (end < 0) return "-";
			return json.substring(start + 1, end);
		}
		int end = start;
		while (end < json.length() && (Character.isDigit(json.charAt(end)) || json.charAt(end) == '.' || json.charAt(end) == '-')) {
			end++;
		}
		return json.substring(start, end);
	}


	private static String httpGet(String urlStr) throws Exception {
		return httpGet(urlStr, null);
	}

	private static String httpGet(String urlStr, String authorizationHeader) throws Exception {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("GET");
			if (authorizationHeader != null && !authorizationHeader.isEmpty()) {
				conn.setRequestProperty("Authorization", authorizationHeader);
			}
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(5000);
			int code = conn.getResponseCode();
			if (code != 200) {
				throw new Exception("HTTP " + code);
			}
			BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"));
			StringBuilder sb = new StringBuilder();
			String line;
			while ((line = reader.readLine()) != null) {
				sb.append(line);
			}
			reader.close();
			return sb.toString();
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	private static void loginTraccar(String baseUrl) throws Exception {
		int code = httpPost(baseUrl + "/api/session", "email=admin%40admin.com&password=admin");
		if (code >= 200 && code < 300) {
			return;
		}
		code = httpPost(baseUrl + "/api/session", "email=admin&password=admin");
		if (code < 200 || code >= 300) {
			throw new Exception("登录失败 HTTP " + code);
		}
	}

	private static int httpPost(String urlStr, String body) throws Exception {
		HttpURLConnection conn = null;
		try {
			URL url = new URL(urlStr);
			conn = (HttpURLConnection) url.openConnection();
			conn.setRequestMethod("POST");
			conn.setDoOutput(true);
			conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
			conn.setConnectTimeout(3000);
			conn.setReadTimeout(5000);
			conn.getOutputStream().write(body.getBytes("UTF-8"));
			conn.getOutputStream().flush();
			return conn.getResponseCode();
		} finally {
			if (conn != null) conn.disconnect();
		}
	}

	private static int countOccurrences(String text, String pattern) {
		if (text == null || text.isEmpty()) return 0;
		int count = 0;
		int idx = 0;
		while ((idx = text.indexOf(pattern, idx)) != -1) {
			count++;
			idx += pattern.length();
		}
		return count;
	}
}
