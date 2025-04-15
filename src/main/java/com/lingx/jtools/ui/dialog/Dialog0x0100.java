package com.lingx.jtools.ui.dialog;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;

import com.lingx.jtools.ui.FlexLayout;
import com.lingx.jtools.ui.JttoolsFrame;
import com.lingx.jtools.ui.MJTextField;
import com.lingx.jtools.ui.PropUtils;

public class Dialog0x0100 extends JDialog{

	public Dialog0x0100() {
		super();
		Dialog0x0100 _this=this;
		this.setTitle("注册设置");
		this.setSize(600,400);
		this.setLocationRelativeTo(null);
		this.getContentPane().setBackground(Color.decode("#dfe9f6"));
		this.setBackground(Color.decode("#dfe9f6"));
		this.setLayout(new FlexLayout(32,5,10));

        ImageIcon icon3 = new ImageIcon(JttoolsFrame.class.getResource("/images/disk.png"));
        ImageIcon icon4 = new ImageIcon(JttoolsFrame.class.getResource("/images/cancel.png"));
        JButton button=new JButton("保存");
        button.setIcon(icon3);
        JButton button2=new JButton("取消");
        button2.setIcon(icon4);
        MJTextField text1 = new MJTextField(PropUtils.getProp("jt808.0x0100.p1"));
        MJTextField text2 = new MJTextField(PropUtils.getProp("jt808.0x0100.p2"));
        MJTextField text3 = new MJTextField(PropUtils.getProp("jt808.0x0100.p3"));
        MJTextField text4 = new MJTextField(PropUtils.getProp("jt808.0x0100.p4"));
        MJTextField text5 = new MJTextField(PropUtils.getProp("jt808.0x0100.p5"));
        MJTextField text6 = new MJTextField(PropUtils.getProp("jt808.0x0100.p6"));
        MJTextField text7 = new MJTextField(PropUtils.getProp("jt808.0x0100.p7"));
        
		this.add(new JLabel("说明:",JLabel.RIGHT),"flex:2");
		this.add(new JLabel("新参数需要断开重新连接才会生效"),"flex:10;");
		this.add(new JLabel("省域ID:",JLabel.RIGHT),"flex:2");
		this.add(text1,"flex:4");
		this.add(new JLabel("市域ID:",JLabel.RIGHT),"flex:2");
		this.add(text2,"flex:4");
		
		this.add(new JLabel("制造商ID:",JLabel.RIGHT),"flex:2");
		this.add(text3,"flex:4");
		this.add(new JLabel("终端型号:",JLabel.RIGHT),"flex:2");
		this.add(text4,"flex:4");
		
		this.add(new JLabel("终端ID:",JLabel.RIGHT),"flex:2");
		this.add(text5,"flex:4");
		this.add(new JLabel("车牌颜色:",JLabel.RIGHT),"flex:2");
		this.add(text6,"flex:4");

		this.add(new JLabel("车牌号码:",JLabel.RIGHT),"flex:2");
		this.add(text7,"flex:4;wrap;");
		

		this.add(new JLabel(),"flex:4;wrap;");
		this.add(new JLabel(),"flex:4;");
		this.add(button,"flex:2");
		this.add(button2,"flex:2");
		button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PropUtils.setProp("jt808.0x0100.p1",text1.getText().trim());
					PropUtils.setProp("jt808.0x0100.p2",text2.getText().trim());
					PropUtils.setProp("jt808.0x0100.p3",text3.getText().trim());
					PropUtils.setProp("jt808.0x0100.p4",text4.getText().trim());
					PropUtils.setProp("jt808.0x0100.p5",text5.getText().trim());
					PropUtils.setProp("jt808.0x0100.p6",text6.getText().trim());
					PropUtils.setProp("jt808.0x0100.p7",text7.getText().trim());
					PropUtils.save();
					_this.dispose();
				}});

		button2.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					_this.dispose();
				}});
		this.setModal(true);
	}
}
