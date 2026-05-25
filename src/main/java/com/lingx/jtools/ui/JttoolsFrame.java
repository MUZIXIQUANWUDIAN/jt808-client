package com.lingx.jtools.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Image;
import java.awt.Toolkit;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTabbedPane;
import javax.swing.UIManager;

import com.formdev.flatlaf.FlatLightLaf;

public class JttoolsFrame {

	public static void main(String[] args) {
		FlatLightLaf.setup();UIManager.put("TextComponent.arc",6);
		Image iconImage = Toolkit.getDefaultToolkit().createImage(JttoolsFrame.class.getResource("/images/108_108.png"));
        ImageIcon icon1 = new ImageIcon(JttoolsFrame.class.getResource("/images/play_green.png"));
        ImageIcon icon2 = new ImageIcon(JttoolsFrame.class.getResource("/images/stop_red.png"));
        ImageIcon icon3 = new ImageIcon(JttoolsFrame.class.getResource("/images/disk.png"));
        JFrame frame = new JFrame("部标JT808、JT1078、苏标测试工具 - http://www.lingx.com");
        frame.setIconImage(iconImage);
    	frame.setResizable(true);

        // 设置窗口大小和位置
        frame.setSize(1280, 860);
        frame.setMinimumSize(new Dimension(1100, 760));
        frame.setLocationRelativeTo(null);
        frame.getContentPane().setBackground(Color.decode("#ffffff"));
        //frame.setLayout(new FlexLayout(32,5,10));
        JTabbedPane tabbedPane = new JTabbedPane();
        frame.add(tabbedPane);

        JPanel tab1 = new JT808ClientPanel();
        tabbedPane.addTab("JT808客户端", tab1);

        JPanel tab2 = new JT808ServerPanel();
        tabbedPane.addTab("JT808服务端", tab2);


        JPanel tab3 = new JT808DecodePanel();
        tabbedPane.addTab("JT808报文解析", tab3);

        JPanel tab98 = new NoJT808ClientPanel();
        tabbedPane.addTab("TCP客户端", tab98);
        JPanel tab99 = new NoJT808ServerPanel();
        tabbedPane.addTab("TCP服务端", tab99);
        tabbedPane.addTab("广告信息", new AdPanel());
        //JPanel tab4 = new JPanel();
        //tabbedPane.addTab("JT808平台商业开源", tab4);
        frame.setVisible(true);
        // 关闭窗口并退出程序
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

}
