package com.lingx.jtools.ui;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

import com.lingx.jt808.JT808Tools;
import com.lingx.jt808.utils.Utils;
import com.lingx.jtools.ui.dialog.Dialog0x0100;
import com.lingx.jtools.ui.dialog.Dialog0x0102;
import com.lingx.jtools.ui.dialog.Dialog0x0200;

public class JT808ClientPanel extends JPanel{

	private static JButton startButton,stopButton;
	public static JTextArea textArea;
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
		textArea = new MJTextArea();
		textArea.setEditable(false);
		textArea.setBackground(Color.decode("#ffffff"));
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
				JT808Tools.setTid(tid1, version);
				JT808Tools.tcp(text1.getText(), text2.getText());

				setButtunZt2();
			}});
        

        stopButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JT808Tools.tcpClose();
				setButtunZt1();
			}});

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
				JT808Tools.clear();
			}});
        btn1078.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JOptionPane.showMessageDialog(null, "1、支持实时视频，模拟器不需要额外操作；平台上直接点播\r\n2、支持历史回放列表(只传一条记录)\r\n3、支持历史视频回放\r\n4、不支持多媒体文件上传");
			}});
        btnAdas.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JT808Tools.sendAdas();
			}});
        btnDsm.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				JT808Tools.sendDsm();
			}});
        
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
