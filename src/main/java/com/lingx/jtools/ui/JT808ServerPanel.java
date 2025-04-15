package com.lingx.jtools.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.lingx.gps.netty.NettyServer;

public class JT808ServerPanel  extends JPanel{
	private JButton startButton,stopButton;
	public static JTextArea textArea;
	private NettyServer nettyServer=new NettyServer();
	public JT808ServerPanel() {
		this.setBackground(Color.decode("#dfe9f6"));
		this.setLayout(new FlexLayout(32,5,10));

        ImageIcon icon1 = new ImageIcon(JttoolsFrame.class.getResource("/images/play_green.png"));
        ImageIcon icon2 = new ImageIcon(JttoolsFrame.class.getResource("/images/stop_red.png"));

        startButton = new JButton("启动");
        startButton.setIcon(icon1);
        stopButton = new JButton("停止");
        stopButton.setIcon(icon2);

        MJTextField text2 = new MJTextField("8808");
		this.add(new JLabel("监听端口(TCP):",JLabel.RIGHT),"flex:1;width:100px;");
		this.add(text2,"flex:1;width:50px;");
		setButtunZt1();
		this.add(startButton, "flex:1;width:80px;");
		this.add(stopButton, "flex:1;width:80px;wrap;");
		textArea = new MJTextArea();
		textArea.setEditable(false);
		textArea.setBackground(Color.decode("#ffffff"));
		JScrollPane scrollPane = new JScrollPane(textArea);
		this.add(scrollPane, "flex:12;height:400px");

		JButton btnclear = new JButton("清空数据");
		this.add(btnclear, "flex:1;width:80px");
        startButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nettyServer.setPort(Integer.parseInt(text2.getText()));
				new Thread(nettyServer).start();
				setButtunZt2();
			}});
        

        stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				nettyServer.getChannel().close();
				setButtunZt1();
			}});
        btnclear.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
			}});

	}

	public  void setButtunZt1() {
		startButton.setEnabled(true);
		stopButton.setEnabled(false);
	}
	public  void setButtunZt2() {
		startButton.setEnabled(false);
		stopButton.setEnabled(true);
	}
}
