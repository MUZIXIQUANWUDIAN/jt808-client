package com.lingx.jtools.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.lingx.gps.netty.nojt808.client.NoJt808TcpClient;
import com.lingx.jt808.utils.Utils;

public class NoJT808ClientPanel extends JPanel {

	private static JButton startButton, stopButton;
	private final TcpClientTabBridge tcpTabUi = new TcpClientTabBridge();
	private JTextArea textArea;
	private JRadioButton hex;
	private JRadioButton string;
	private NoJt808TcpClient client;

	public NoJT808ClientPanel() {
		this.setBackground(Color.decode("#dfe9f6"));
		this.setLayout(new FlexLayout(32, 5, 10));

		ImageIcon icon1 = new ImageIcon(JttoolsFrame.class.getResource("/images/play_green.png"));
		ImageIcon icon2 = new ImageIcon(JttoolsFrame.class.getResource("/images/stop_red.png"));

		startButton = new JButton("连接");
		startButton.setIcon(icon1);
		stopButton = new JButton("停止");
		stopButton.setIcon(icon2);

		MJTextField text1 = new MJTextField(PropUtils.getProp("tcp.client.ip"));
		MJTextField text2 = new MJTextField(PropUtils.getProp("tcp.client.port"));
		this.add(new JLabel("服务器IP:", JLabel.RIGHT), "flex:1");
		this.add(text1, "flex:2");
		this.add(new JLabel("端口:", JLabel.RIGHT), "flex:1;width:40px;");
		this.add(text2, "flex:1;width:50px;");
		this.add(startButton, "flex:1;width:80px;");
		this.add(stopButton, "flex:1;width:80px;");
		hex = new JRadioButton("hex");
		string = new JRadioButton("string");
		ButtonGroup bg = new ButtonGroup();
		bg.add(hex);
		bg.add(string);
		hex.setSelected(true);
		this.add(new JLabel("显示方式:", JLabel.RIGHT), "flex:1;width:100px;");
		this.add(hex, "flex:1;width:60px;");
		this.add(string, "flex:1;width:60px;wrap;");

		setButtunZt1();
		textArea = new MJTextArea();
		textArea.setEditable(false);
		textArea.setBackground(Color.decode("#ffffff"));
		tcpTabUi.bind(textArea, NoJT808ClientPanel::setButtunZt1);
		JScrollPane scrollPane = new JScrollPane(textArea);
		this.add(scrollPane, "flex:12;height:400px");

		MJTextField text3 = new MJTextField();
		JButton sendButton = new JButton("发送");
		this.add(text3, "flex:11;");
		this.add(sendButton, "flex:1;");

		sendButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				byte data[];
				String text = text3.getText().trim();
				try {
					if (hex.isSelected()) {
						data = Utils.hexToBytes(text);
					} else {
						data = text.getBytes();
					}
					tcpTabUi.appendArrowLog(text);
					if (client != null && client.getChannel() != null) {
						client.getChannel().writeAndFlush(data);
					}
				} catch (Exception e1) {
					JOptionPane.showMessageDialog(null, "发送数据异常");
				}
			}
		});

		startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				PropUtils.setProp("tcp.client.ip", text1.getText().trim());
				PropUtils.setProp("tcp.client.port", text2.getText().trim());
				PropUtils.save();
				client = new NoJt808TcpClient(text1.getText().trim(), Integer.parseInt(text2.getText().trim()),
						tcpTabUi, () -> hex.isSelected());
				new Thread(client).start();
				setButtunZt2();
			}
		});

		stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (client != null && client.getChannel() != null) {
					client.getChannel().close();
				}
				setButtunZt1();
			}
		});
	}

	public static void setButtunZt1() {
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
	}

	public static void setButtunZt2() {
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
	}
}
