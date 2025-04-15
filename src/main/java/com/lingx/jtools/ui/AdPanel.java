package com.lingx.jtools.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.font.TextAttribute;
import java.util.HashMap;
import java.util.Map;

import javax.swing.JPanel;
import javax.swing.JTextArea;

public class AdPanel extends JPanel{
	public AdPanel() {
		this.setBackground(Color.decode("#dfe9f6"));
		this.setLayout(new FlexLayout(32,5,10));
		JTextArea textarea=new JTextArea("基于部标JT808、JT809、JT1078、苏标主动安全的车辆动态监控系统商业开源\r\n\r\n低价出售全套源码，联系QQ：283853318，手机：15060620800（微信同号）\r\n\r\n演示地址\r\nhttp://gps.lingx.com/\r\n账号:admin\r\n密码:123456\r\n\r\n终端设备接入\r\nIP：47.100.112.218\r\n端口：8808");
		textarea.setEditable(false);
		textarea.setBackground(Color.decode("#dfe9f6"));
		Map<TextAttribute, Object> fontAttr = new HashMap<>();
		fontAttr.put(TextAttribute.FAMILY, "微软雅黑"); // 字体名称
		fontAttr.put(TextAttribute.SIZE, 14); // 字体大小
		Font font = new Font(fontAttr);
		textarea.setFont(font);
		this.add(textarea,"flex:12;height:500px");
	}
}
