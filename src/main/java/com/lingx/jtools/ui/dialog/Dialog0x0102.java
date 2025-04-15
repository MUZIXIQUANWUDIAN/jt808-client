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

public class Dialog0x0102 extends JDialog{

	public Dialog0x0102() {
		super();
		Dialog0x0102 _this=this;
		this.setTitle("鉴权设置");
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
        MJTextField text1 = new MJTextField(PropUtils.getProp("jt808.0x0102.p1"));
        
		this.add(new JLabel("说明:",JLabel.RIGHT),"flex:2");
		this.add(new JLabel("新参数需要断开重新连接才会生效"),"flex:10;");
		this.add(new JLabel("鉴权码:",JLabel.RIGHT),"flex:2");
		this.add(text1,"flex:8;wrap;");
		

		this.add(new JLabel(),"flex:4;wrap;");
		this.add(new JLabel(),"flex:4;");
		this.add(button,"flex:2");
		this.add(button2,"flex:2");
		button.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					PropUtils.setProp("jt808.0x0102.p1",text1.getText().trim());
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
